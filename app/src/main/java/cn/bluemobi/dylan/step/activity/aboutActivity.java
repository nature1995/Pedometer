package cn.bluemobi.dylan.step.activity;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toolbar;


import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;
import cn.bluemobi.dylan.step.R;

public class aboutActivity extends AppCompatActivity {
    private RelativeLayout relativeLayout;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Element adsElement = new Element();
        adsElement.setTitle("Advertise with us");

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.about_icon_github)
                .setDescription("Pedometer with GPS Location by Android")
                .addItem(new Element().setTitle("Version 1.0"))
                .addGroup("Contact us")
                .addItem(new Element().setTitle("Ziran Gong: zgong5@stevens.edu"))
                .addItem(new Element().setTitle("Xiao Liu: xliu99@stevens.edu"))
                .addItem(new Element().setTitle("Xinyu Liu: xliu82@stevens.edu"))
                .addItem(new Element().setTitle("Haotian Mo: hmo2@stevens.edu"))
                .addWebsite("www.ranxiaolang.com")//website
                .addGitHub("nature1995")//github
                .addGroup("Feature")
                .addItem(new Element().setTitle("1. Set Plan: Set up and save the walking plan\r\n" +
                                                "2. View History: View the history of plans\r\n" +
                                                "3. TestVibrate: Test the vibration of reminder\r\n" +
                                                "4. Location: Check the location of users\r\n" +
                                                "5. Weather: Search weather and set weather email alert"))

                .create();

        setContentView(aboutPage);

    }

}

