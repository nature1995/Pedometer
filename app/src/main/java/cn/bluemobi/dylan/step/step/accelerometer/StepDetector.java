package cn.bluemobi.dylan.step.step.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class StepDetector implements SensorEventListener {

    //Triaxial data
    float[] oriValues = new float[3];
    final int ValueNum = 4;
    //Wave peak difference
    float[] tempValue = new float[ValueNum];
    int tempCount = 0;
    boolean isDirectionUp = false;
    int continueUpCount = 0;
    int continueUpFormerCount = 0;
    boolean lastStatus = false;
    float peakOfWave = 0;
    float valleyOfWave = 0;
    long timeOfThisPeak = 0;
    long timeOfLastPeak = 0;
    long timeOfNow = 0;
    float gravityNew = 0;
    float gravityOld = 0;
    //Dynamic threshold
    final float InitialValue = (float) 1.3;
    //Initial threshold
    float ThreadValue = (float) 2.0;
    //Wave time difference
    int TimeInterval = 250;
    private StepCountListener mStepListeners;

    @Override
    public void onSensorChanged(SensorEvent event) {
        for (int i = 0; i < 3; i++) {
            oriValues[i] = event.values[i];
        }
        gravityNew = (float) Math.sqrt(oriValues[0] * oriValues[0]
                + oriValues[1] * oriValues[1] + oriValues[2] * oriValues[2]);
        detectorNewStep(gravityNew);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

    public void initListener(StepCountListener listener) {
        this.mStepListeners = listener;
    }

    /*
    * 1. If a peak is detected and the time difference and threshold conditions are met, then 1 step is determined
    * 2. Eligible values are added to the calculated dynamic threshold
    * */
    public void detectorNewStep(float values) {
        if (gravityOld == 0) {
            gravityOld = values;
        } else {
            if (detectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();
                if (timeOfNow - timeOfLastPeak >= TimeInterval
                        && (peakOfWave - valleyOfWave >= ThreadValue)) {
                    timeOfThisPeak = timeOfNow;
                    /*
                     * Update UI
                     * 1. Valid count which over 10 steps
                     * 2. Count steps continuously.
                     * */
                    mStepListeners.countStep();
                }
                if (timeOfNow - timeOfLastPeak >= TimeInterval
                        && (peakOfWave - valleyOfWave >= InitialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = peakValleyThread(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = values;
    }

    /*
     * Record peak value
     * 1. isDirectionUp = false
     * 2. lastStatus = true
     * 3. 2 times up action
     * 4. value over 20
     * Record valley value
     * 1. Compare with peak value
     * */
    public boolean detectorPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

        if (!isDirectionUp && lastStatus
                && (continueUpFormerCount >= 2 || oldValue >= 20)) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }

    /*
     * Threshold calculation
     * */
    public float peakValleyThread(float value) {
        float tempThread = ThreadValue;
        if (tempCount < ValueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, ValueNum);
            for (int i = 1; i < ValueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[ValueNum - 1] = value;
        }
        return tempThread;

    }

    /*
    Normalized
     * */
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / ValueNum;
        if (ave >= 8)
            ave = (float) 4.3;
        else if (ave >= 7 && ave < 8)
            ave = (float) 3.3;
        else if (ave >= 4 && ave < 7)
            ave = (float) 2.3;
        else if (ave >= 3 && ave < 4)
            ave = (float) 2.0;
        else {
            ave = (float) 1.3;
        }
        return ave;
    }

}
