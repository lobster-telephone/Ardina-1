package com.myardina.buckeyes.myardina;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by sachinda on 10/11/16.
 */

public class SplashActivity extends AppCompatActivity {
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set content view AFTER ABOVE sequence (to avoid crash)
        this.setContentView(R.layout.activity_splash);

        TextView txt = (TextView) findViewById(R.id.textView);

        Thread splashTimer = new Thread() {
            public void run() {
                try {
                    int splashTime = 0;
                    while (splashTime < 3000) {
                        splashTime = splashTime + 1000;
                    }
                    intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } catch (IllegalThreadStateException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }
        };
        splashTimer.start();
    }
}
