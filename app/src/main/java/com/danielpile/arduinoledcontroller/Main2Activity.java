package com.danielpile.arduinoledcontroller;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        colorPane = (LinearLayout)findViewById(R.id.colorPane);
        colorSeekBar = (ColorSeekBar) findViewById(R.id.colorSlider);
        textView = (TextView)findViewById(R.id.colorText);

        colorSeekBar.setMaxValue(100);
        colorSeekBar.setColors(R.array.material_colors); // material_colors is defalut included in res/color,just use it.
        colorSeekBar.setColorBarValue(10); //0 - maxValue
        colorSeekBar.setAlphaBarValue(10); //0-255
        colorSeekBar.setShowAlphaBar(true);
        colorSeekBar.setBarHeight(5); //5dpi
        colorSeekBar.setThumbHeight(30); //30dpi
        colorSeekBar.setBarMargin(10); //set the margin between colorBar and alphaBar 10dpi
        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarValue, int alphaBarValue, int color) {
                textView.setTextColor(color);
                if(mainActivity.myThreadConnected!=null){
                    Toast.makeText(getApplicationContext(),
                            colorSeekBar.getColors().toString(),
                            Toast.LENGTH_LONG).show();
                    byte[] bytesToSend = colorSeekBar.getColors().toString().getBytes();
                    mainActivity.myThreadConnected.write(bytesToSend);
                }
            }
        });
    }


}
