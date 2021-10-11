package com.example.cardapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashCreenActivity extends AppCompatActivity {

    ImageView splashImg;
    SharedPreferences loggedInPrefs;
    boolean hasAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_creen);

        splashImg = (ImageView) findViewById(R.id.splashImage);

        startActivity(new Intent(SplashCreenActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

        Animation moveright = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bus_moving_right);

        splashImg.setAnimation(moveright);
    }

}
