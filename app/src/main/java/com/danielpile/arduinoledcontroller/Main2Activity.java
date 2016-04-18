package com.danielpile.arduinoledcontroller;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rtugeek.android.colorseekbar.ColorSeekBar;

import org.w3c.dom.Text;

public class Main2Activity extends AppCompatActivity {

    LinearLayout colorPane;
    ColorSeekBar colorSeekBar;
    MainActivity mainActivity;
    TextView textView;
    TextView txt_getcolor;
    TextView txt_colorbarvalue;
    TextView txt_newstring;
    TextView txt_red;
    TextView txt_green;
    TextView txt_blue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        colorPane = (LinearLayout)findViewById(R.id.colorPane);
        colorSeekBar = (ColorSeekBar) findViewById(R.id.colorSlider);
        textView = (TextView)findViewById(R.id.colorText);
        /*txt_getcolor = (TextView)findViewById(R.id.txt_getcolor);
        txt_colorbarvalue = (TextView)findViewById(R.id.txt_colorbarvalue);
        txt_newstring = (TextView)findViewById(R.id.txt_newstring);*/
        txt_red = (TextView)findViewById(R.id.txt_red);
        txt_green = (TextView)findViewById(R.id.txt_green);
        txt_blue = (TextView)findViewById(R.id.txt_blue);

        colorSeekBar.setMaxValue(100);
        colorSeekBar.setColors(R.array.material_colors); // material_colors is defalut included in res/color,just use it.
        colorSeekBar.setColorBarValue(10); //0 - maxValue
        colorSeekBar.setAlphaBarValue(10); //0-255
        colorSeekBar.setShowAlphaBar(true);
        colorSeekBar.setBarHeight(5); //5dpi
        colorSeekBar.setThumbHeight(30); //30dpi
        colorSeekBar.setBarMargin(10); //set the margin between colorBar and alphaBar 10dpi
        textView.setTextColor(colorSeekBar.getColor());
        txt_getcolor.setText(String.valueOf(colorSeekBar.getColors()));
        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
                textView.setTextColor(color);
                /*String myColorString = String.valueOf(color);
                String newString = myColorString.replace("-","");
                // edited to support big numbers bigger than 0x80000000
                txt_colorbarvalue.setText(myColorString);
                txt_newstring.setText(newString);
                int newcolor = (int)Long.parseLong(myColorString, 16);
                int r = (newcolor >> 16) & 0xFF;
                int g = (newcolor >> 8) & 0xFF;
                int b = (newcolor >> 0) & 0xFF;*/
                String r = String.valueOf(Color.red(color));
                String g = String.valueOf(Color.green(color));
                String b = String.valueOf(Color.blue(color));
                txt_red.setText(r);
                txt_green.setText(g);
                txt_blue.setText(b);
                //txt_getcolor.setText(String.valueOf(colorSeekBar.getColors()));
                /*if(mainActivity.myThreadConnected!=null){
                    Toast.makeText(getApplicationContext(),
                            colorSeekBar.getColor(),
                            Toast.LENGTH_LONG).show();
                    byte[] bytesToSend = colorSeekBar.getColors().toString().getBytes();
                    mainActivity.myThreadConnected.write(bytesToSend);
                }*/
            }
        });
    }


}