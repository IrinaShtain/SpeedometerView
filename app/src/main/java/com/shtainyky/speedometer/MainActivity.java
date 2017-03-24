package com.shtainyky.speedometer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SpeedometerView.OnSpeedChangedListener {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SpeedometerView speedometerView = (SpeedometerView) findViewById(R.id.speedometer);
        speedometerView.setOnSpeedChangedListener(this);
        Button buttonGas = (Button) findViewById(R.id.buttonGas);
        Button buttonStop = (Button) findViewById(R.id.btStop);
        Button pressBrake = (Button) findViewById(R.id.btRelax);
        Button pressBrakeStop = (Button) findViewById(R.id.btBrakeStop);
        textView = (TextView) findViewById(R.id.speed);

        buttonGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.pressGas();
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.releaseGas();
            }
        });
        pressBrake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.pressBrake();
            }
        });

        pressBrakeStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedometerView.releaseBrake();
            }
        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i <= 100; i += 10) {
//                    speedometerView.setCurrentSpeed(i);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                speedometerView.setRadiusSpeedArrow(500);
//            }
//        }).start();


    }

    @Override
    public void onSpeedChanged(int value) {
        Log.d("myLog", " value = " + value);
        textView.setText(String.valueOf(value));
    }
}
