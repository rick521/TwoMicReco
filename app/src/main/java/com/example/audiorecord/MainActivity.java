package com.example.audiorecord;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity{

    private AudioRecoderUtils audioRecoderUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button =findViewById(R.id.Left);
        Button button1=findViewById(R.id.right);
        Button button2=findViewById(R.id.button);
        Button button3=findViewById(R.id.save);
        audioRecoderUtils=new AudioRecoderUtils();
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioRecoderUtils.StopRecord();
                button2.setText("开始立体录音");
                audioRecoderUtils.SaveRecord(MainActivity.this);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button2.setText("正在立体录音");
                audioRecoderUtils.StartRecord(MainActivity.this);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button2.setText("开始立体录音");
                audioRecoderUtils.StopRecord();
                audioRecoderUtils.PlayRecord(true);

            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button2.setText("开始立体录音");
                audioRecoderUtils.StopRecord();
                audioRecoderUtils.PlayRecord(false);
            }
        });

    }


}