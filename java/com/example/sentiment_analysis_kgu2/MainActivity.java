package com.example.sentiment_analysis_kgu2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.sdk.core.service.exception.ServiceResponseException;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.EmotionOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import com.ibm.watson.natural_language_understanding.v1.model.SentimentOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText enterTextEditText;
    ConstraintLayout layout;
    TextView displayText;
    Button displayButton, breakdownButton;
    ImageView emojiImage, micIcon;


    private static final int RECOGNIZER_RESULT = 1;
    private final String API_KEY = "FptYRdUEoLZLvIdaZX9bKk1rjcpJlESAG02tq_eWSllF";
    private final String SERVICE_URL = "https://api.us-south.natural-language-understanding.watson.cloud.ibm.com/instances/10fbaca6-9a7e-4229-b9ff-bb235f8bba02";

    String sentiment;
    String inputText;
    AnalysisResults results;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enterTextEditText = findViewById(R.id.textToEvaluateEditText);
        layout = findViewById(R.id.layoutView);
        displayText = findViewById(R.id.sentimentAnalysisTextview);
        displayButton = findViewById(R.id.analyzeButton);
        breakdownButton = findViewById(R.id.inDepthButtonID);
        emojiImage = findViewById(R.id.emojiImageView);
        emojiImage.setVisibility(View.INVISIBLE);
        micIcon = findViewById(R.id.updated_mic_icon);
    }

    private class AskWatsonTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayText.setText("Running Analysis...");
                }
            });
            try {
                results = doAnalysis();
                sentiment = "Sentiment is " + results.getSentiment().getDocument().getLabel();
            } catch (ServiceResponseException e){
                System.out.println(e.getMessage());
                System.out.println(e.getStatusCode());

                if(e.getStatusCode() == 422){
                    sentiment = "Not Enough Text, Try Again";
                }
                if(e.getStatusCode() == 400){
                    sentiment = "Invalid Text, Try Again";
                }
            }

            return sentiment;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(results);
            displayText.setText(result);

            if(results == null){}
            else if (results.getSentiment().getDocument().getLabel().equals("positive")){
                layout.setBackgroundResource(R.drawable.gradient_positive);
                emojiImage.setImageResource(R.drawable.smiley);
                breakdownButton.setVisibility(View.VISIBLE);
                emojiImage.setVisibility(View.VISIBLE);
                transferData();
            } else if (results.getSentiment().getDocument().getLabel().equals("negative")){
                emojiImage.setImageResource(R.drawable.frowney);
                breakdownButton.setVisibility(View.VISIBLE);
                emojiImage.setVisibility(View.VISIBLE);
                layout.setBackgroundResource(R.drawable.gradient_negative);
                transferData();
            } else{
                emojiImage.setImageResource(R.drawable.neutral);
                breakdownButton.setVisibility(View.VISIBLE);
                emojiImage.setVisibility(View.VISIBLE);
                layout.setBackgroundResource(R.drawable.gradient_neutral);
                transferData();
            }
        }
    }

    public void transferData(){
        intent = new Intent(this, Main2Activity.class);

        double sentimentValue = results.getSentiment().getDocument().getScore();
        String sentimentLabel = results.getSentiment().getDocument().getLabel();
        String analyzedText = inputText;

        intent.putExtra("value", sentimentValue);
        intent.putExtra("text", analyzedText);
        intent.putExtra("label", sentimentLabel);

        double emotionJoy = results.getEmotion().getDocument().getEmotion().getJoy();
        double emotionAnger = results.getEmotion().getDocument().getEmotion().getAnger();
        double emotionDisgust = results.getEmotion().getDocument().getEmotion().getDisgust();
        double emotionFear = results.getEmotion().getDocument().getEmotion().getFear();
        double emotionSadness = results.getEmotion().getDocument().getEmotion().getSadness();
        intent.putExtra("joy", emotionJoy);
        intent.putExtra("anger", emotionAnger);
        intent.putExtra("disgust", emotionDisgust);
        intent.putExtra("fear", emotionFear);
        intent.putExtra("sadness", emotionSadness);
    }

    public void onAnalyzeButtonClicked(View view){
        breakdownButton.setVisibility(View.INVISIBLE);
        inputText = enterTextEditText.getText().toString();
        enterTextEditText.clearFocus();
        AskWatsonTask task  = new AskWatsonTask();
        task.execute(new String[]{});
    }

    public AnalysisResults doAnalysis(){
        IamAuthenticator authenticator = new IamAuthenticator(API_KEY);
        NaturalLanguageUnderstanding watson = new NaturalLanguageUnderstanding("2020-08-01", authenticator);
        watson.setServiceUrl(SERVICE_URL);

        SentimentOptions sentiment = new SentimentOptions.Builder()
                .build();

        EmotionOptions emotion = new EmotionOptions.Builder()
                .build();

        Features features = new Features.Builder()
                .sentiment(sentiment)
                .emotion(emotion)
                .build();

        AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                .text(inputText)
                .features(features)
                .build();

        AnalysisResults response = watson
                .analyze(parameters)
                .execute()
                .getResult();
        return response;
    }

    public void onMicImageClicked(View view){
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Recording...");
        try{
            startActivityForResult(speechIntent, RECOGNIZER_RESULT);
        } catch(ActivityNotFoundException e){
            Toast.makeText(this, "Device dosen't support speech", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RECOGNIZER_RESULT && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            enterTextEditText.setText(matches.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onBreakdownButtonClicked(View view){
        startActivity(intent);
    }

}
