package cn.bluemobi.dylan.step.activity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import java.io.IOException;
import java.util.List;

public class Getposition {
    private static  Boolean power_saving =false;
    public static Location getLocation(Context context) {
        String locationProvider;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getAllProviders();
        if (providers == null) return null;
            if (providers.contains(LocationManager.GPS_PROVIDER)) {
                locationProvider = LocationManager.GPS_PROVIDER;
            } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                locationProvider = LocationManager.NETWORK_PROVIDER;
            } else {
                locationProvider = LocationManager.PASSIVE_PROVIDER;
            }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            //if have location before
            return location;
        } else
        {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            //move 10 m or time passed 5min refresh location
            locationManager.requestLocationUpdates(locationProvider, 300000, 10, locationListener);
            location = locationManager.getLastKnownLocation(locationProvider);

            if (location != null) {
                // if get location
                return  location;
            }
        }
        return null;
    }
    public static List<Address> getcity(Context context){
        Location location = getLocation(context);
        List<Address> addresses = null;
        if (location!= null){
            Geocoder geocoder = new Geocoder(context);
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1);
//                String my_address=""+addresses.get(0).getLocality()+","+addresses.get(0).getCountryCode();
                return addresses;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}