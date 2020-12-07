package com.example.sentiment_analysis_kgu2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.core.ui.Title;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    TextView text, value, label;
    ProgressBar bar;
    AnyChartView chart;
    String[] emotions = {"Joy","Anger", "Disgust", "Fear", "Sadness"};
    double[] emotionValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent intent = getIntent();

        text = findViewById(R.id.textID);
        value = findViewById(R.id.scoreTextID);
        label = findViewById(R.id.labelTextID);
        chart = findViewById(R.id.ChartViewID);
        bar = findViewById(R.id.progressBar);

        text.setText(intent.getStringExtra("text"));
        value.setText("Score: " + intent.getDoubleExtra("value",0.0));
        String labelText = intent.getStringExtra("label");
        label.setText("Label: " + labelText);
        if(labelText.equals("negative")){
            label.setTextColor(Color.parseColor("#CB3439"));
        } else if (labelText.equals("positive")){
            label.setTextColor(Color.parseColor("#04b438"));
        }

        emotionValues = new double[]{intent.getDoubleExtra("joy", 0.0),
                intent.getDoubleExtra("anger", 0.0),
                intent.getDoubleExtra("disgust", 0.0),
                intent.getDoubleExtra("fear", 0.0),
                intent.getDoubleExtra("sadness", 0.0)};


        setUpChart();


    }

    public void setUpChart(){
        chart.setProgressBar(bar);

        chart.setBackgroundColor("#80e2ee");
        Pie pie = AnyChart.pie();
        List<DataEntry> entries = new ArrayList<>();
        for(int i = 0; i < emotions.length; i++){
            entries.add(new ValueDataEntry(emotions[i], emotionValues[i]));
        }
        pie.data(entries);
        pie.title("Emotions Breakdown");
        pie.background().fill("#80e2ee");

        String [] colors = {"#1ae54b", "#b7261b", "#c9b014", "#131002", "#7175b0"};
        pie.palette(colors);

        pie.title().fontColor("#434343");
        pie.title().fontSize("20px");
        pie.title().fontWeight("bold");
        pie.legend().fontColor("#434343");
        pie.legend().fontSize("12px");

        chart.setChart(pie);
    }

}
