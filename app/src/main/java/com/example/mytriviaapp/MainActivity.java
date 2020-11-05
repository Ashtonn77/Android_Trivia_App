package com.example.mytriviaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytriviaapp.data.AnswerListAsyncResponse;
import com.example.mytriviaapp.data.QuestionBank;
import com.example.mytriviaapp.model.Question;
import com.example.mytriviaapp.utils.Prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String MESSAGE_ID = "score_preference";
    private TextView questionTextView;
    private TextView questionCounterText;
    private Button trueButton;
    private Button falseButton;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private int currentQuestionIndex;
    private int score = 0;
    private int high_score;

    //score variables
    private TextView highScore;
    private TextView currentScore;


    Prefs prefs;

    List<Question> questionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        questionTextView = findViewById(R.id.question_text_view);
        questionCounterText = findViewById(R.id.counter_text);
        trueButton = findViewById(R.id.true_button);
        falseButton = findViewById(R.id.false_button);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);

        //score
        highScore = findViewById(R.id.high_score_value);
        currentScore = findViewById(R.id.current_score_value);

        prefs = new Prefs(MainActivity.this);

        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);


            questionList = new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {
                //shuffles array elements
                Collections.shuffle(questionArrayList);
                updateQuestion(questionArrayList, currentQuestionIndex);

            }
        });

        highScore.setText(Integer.toString(prefs.getHighScore()));

    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.prev_button:

                go_to_previous_question();

                break;

            case R.id.next_button:

                go_to_next_question();

                break;

            case R.id.true_button:
                checkAnswer(true, currentQuestionIndex);
                updateQuestion(questionList, currentQuestionIndex);
                break;

            case R.id.false_button:
                checkAnswer(false, currentQuestionIndex);
                updateQuestion(questionList, currentQuestionIndex);
                break;


        }

    }

    void go_to_next_question()
    {
        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
        updateQuestion(questionList, currentQuestionIndex);
    }

    void go_to_previous_question()
    {
        currentQuestionIndex = currentQuestionIndex == 0 ? questionList.size() -1: currentQuestionIndex - 1;
        updateQuestion(questionList, currentQuestionIndex);
    }

    private void checkAnswer(boolean userChoice, int index) {

        boolean answer = questionList.get(index).getAnswerTrue();
        int toastMsgId = 0;

        if(answer == userChoice)
        {
            fadeView();
            updateScore(true);
           // prefs.saveHighScore(score);
            scaleView();
            go_to_next_question();
            toastMsgId = R.string.correct_answer;

        }
        else {
            shakeAnimation();
            updateScore(false);
            go_to_next_question();
            toastMsgId = R.string.incorrect_answer;
        }

        Toast.makeText(MainActivity.this, toastMsgId, Toast.LENGTH_SHORT).show();

    }

    private void updateQuestion(List<Question> questionList, int index) {
        questionTextView.setText(questionList.get(index).getQuestion());
        questionCounterText.setText(index + " / " + questionList.size());
    }


    private void scaleView()
    {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f);
        scaleAnimation.setDuration(350);
        scaleAnimation.setRepeatCount(1);
        scaleAnimation.setRepeatMode(Animation.REVERSE);

        currentScore.setAnimation(scaleAnimation);
    }

    private void fadeView()
    {
        CardView cardView = findViewById(R.id.cardView);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.3f);

        alphaAnimation.setDuration(550);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);


        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.GREEN);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



    }

    private void shakeAnimation()
    {
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);
        CardView cardView = findViewById(R.id.cardView);
        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.RED);
                questionTextView.setTextColor(Color.WHITE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                questionTextView.setTextColor(Color.BLACK);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void updateScore(boolean answer)
    {
       score = answer ? score + 10 : score - 5;
        score = (score <= 0) ? 0 : score;
       currentScore.setText(Integer.toString(score));

       if(score > prefs.getHighScore()){
           highScore.setText(Integer.toString(score));
       }

    }

    @Override
    protected void onPause() {
        prefs.saveHighScore(score);
        super.onPause();
    }
}