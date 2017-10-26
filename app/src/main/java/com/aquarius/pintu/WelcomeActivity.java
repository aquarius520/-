package com.aquarius.pintu;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by aquarius on 2017/10/25.
 */
public class WelcomeActivity extends AppCompatActivity {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_welcome);


        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }, 1500);
    }
}
