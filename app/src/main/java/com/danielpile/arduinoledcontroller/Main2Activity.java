package com.danielpile.arduinoledcontroller;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rtugeek.android.colorseekbar.ColorSeekBar;

import org.w3c.dom.Text;

import java.io.IOException;

public class Main2Activity extends AppCompatActivity {

    LinearLayout colorPane;
    ColorSeekBar colorSeekBar;
    MainActivity mainActivity;
    TextView textView;
    TextView txt_red;
    TextView txt_green;
    TextView txt_blue;
    Button btn_color_white;
    Button btn_color_red;
    Button btn_color_green;
    Button btn_color_blue;
    TextView txt_debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        colorPane = (LinearLayout)findViewById(R.id.colorPane);
        colorSeekBar = (ColorSeekBar) findViewById(R.id.colorSlider);
        textView = (TextView)findViewById(R.id.colorText);
        txt_red = (TextView)findViewById(R.id.txt_red);
        txt_green = (TextView)findViewById(R.id.txt_green);
        txt_blue = (TextView)findViewById(R.id.txt_blue);
        btn_color_white = (Button)findViewById(R.id.btn_color_white);
        btn_color_red = (Button)findViewById(R.id.btn_color_red);
        btn_color_green = (Button)findViewById(R.id.btn_color_green);
        btn_color_blue = (Button)findViewById(R.id.btn_color_blue);
        txt_debug = (TextView)findViewById(R.id.txt_debug);

        colorSeekBar.setMaxValue(100);
        colorSeekBar.setColors(R.array.material_colors); // material_colors is defalut included in res/color,just use it.
        colorSeekBar.setColorBarValue(10); //0 - maxValue
        colorSeekBar.setAlphaBarValue(10); //0-255
        colorSeekBar.setShowAlphaBar(true);
        colorSeekBar.setBarHeight(5); //5dpi
        colorSeekBar.setThumbHeight(30); //30dpi
        colorSeekBar.setBarMargin(10); //set the margin between colorBar and alphaBar 10dpi
        textView.setTextColor(colorSeekBar.getColor());
        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
                String command = "";
                textView.setTextColor(color);
                String r = String.valueOf(Color.red(color));
                String g = String.valueOf(Color.green(color));
                String b = String.valueOf(Color.blue(color));
                txt_red.setText(r);
                txt_green.setText(g);
                txt_blue.setText(b);
                try
                {
                    command = new StringBuilder("kSetLEDs,s,").append(r).append(",").append(g).append(",").append(b).toString();
                }
                catch (Exception e)
                {
                    txt_debug.setText("Exception Occurred building string: " + e.getMessage());
                    e.printStackTrace();
                }

                try
                {
                    sendCommmand(command);
                }
                catch (Exception e)
                {
                    txt_debug.setText("Exception Occurred sending command: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        btn_color_white.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String command = "kSetLEDs,s,255,255,255,25";
                sendCommmand(command);
            }});

        btn_color_red.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String command = "kSetLEDs,s,255,0,0,25";
                sendCommmand(command);
            }});

        btn_color_green.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String command = "kSetLEDs,s,0,255,0,25";
                sendCommmand(command);
            }});

        btn_color_blue.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String command = "kSetLEDs,s,0,0,255,25";
                sendCommmand(command);
            }});
    }

    private void sendCommmand(String command) {
        if (mainActivity.myThreadConnected != null)
        {
            try {
                command = new StringBuilder("cmdMessenger.sendCmd(").append(command).append(");").toString();
                byte[] bytesToSend = command.getBytes();
                mainActivity.myThreadConnected.write(bytesToSend);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


}
