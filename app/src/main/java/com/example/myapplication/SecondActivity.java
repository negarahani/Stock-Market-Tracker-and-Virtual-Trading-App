package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

public class SecondActivity extends AppCompatActivity {
    private static final int ICON_DELAY = 2000;
    private static final int SPINNER_DELAY = 4000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ImageView iconImageView = findViewById(R.id.iconImageView);
                iconImageView.setVisibility(View.INVISIBLE);

                ProgressBar spinner = findViewById(R.id.spinner);
                spinner.setVisibility(View.VISIBLE);

                //Delay starting main activity
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Starting the main activity after delay
                        startActivity(new Intent(SecondActivity.this, MainActivity.class));
                        finish(); //current activity should be finished
                    }
                }, SPINNER_DELAY);
            }
        }, ICON_DELAY);
    }
}
