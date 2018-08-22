package com.example.mx.particleprogressbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.mx.widget.ParticleProgressBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RelativeLayout rl = (RelativeLayout) findViewById(R.id.rl);
        final ParticleProgressBar pb = (ParticleProgressBar)findViewById(R.id.progress);
        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.start(rl, new ParticleProgressBar.OnProgressListener() {
                    @Override
                    public void onStep1() {

                    }

                    @Override
                    public void onFinish() {

                    }
                });
            }
        });

    }
}
