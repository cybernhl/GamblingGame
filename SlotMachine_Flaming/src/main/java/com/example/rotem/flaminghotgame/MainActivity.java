package com.example.rotem.flaminghotgame;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean winFiveInARow[] = getResetWinningsArray();
    private boolean winFourInARow[] = getResetWinningsArray();
    private boolean winThreeInARow[]= getResetWinningsArray();

    private Long startTime = Long.valueOf(0) , endTime = Long.valueOf(0) , DELAY = Long.valueOf(350);
    private int userWinPoints = 0 , winPoints = 0;
    private int spinThreadSleepTime = 7 , winTheardSleepTime = 10;
    private final int USER_PAY_FOR_SPIN = 100 , COLOR_FOR_MINUS = Color.RED , COLOR_FOR_PLUS = Color.GREEN;

    public static boolean running = false , isWinningRunning = false;
    public static boolean isWinning = false , isAutomaticGame = false , isWinView = false;

    private TextView spinButton, winTextView, creditTextView, automaticButton, settingsTextView, collectButton;

    private int userCredits = 2000 , SP_DEFAULT_USER_SCORE = 0;

    private final String SP_USER_SCORE = "LevelPref", SP_INITIALIZED = "InitializeSharedPref", SHARED_PREF_IS_START = "IsStartPref",SHARED_PREF_NAME = "SharedPrefName";
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;

    private WinningView firstWinView;
    private WinningView secondWinView;
    private WinningView thirdWinView;
    private WinningView fourthWinView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinButton = (TextView) findViewById(R.id.spinTextView);
        winTextView = (TextView) findViewById(R.id.winTextView);
        creditTextView = (TextView) findViewById(R.id.creditsSumTextView);
        settingsTextView = (TextView) findViewById(R.id.gameSettingsTextView);
        automaticButton = (TextView) findViewById(R.id.automaticGameTextView);
        collectButton = (TextView) findViewById(R.id.collectTextView);

        firstWinView = (WinningView) findViewById(R.id.first_win_view);
        secondWinView = (WinningView) findViewById(R.id.second_win_view);
        thirdWinView = (WinningView) findViewById(R.id.third_win_view);
        fourthWinView = (WinningView) findViewById(R.id.fourth_win_view);
    }

    /**
     * private method that reset all the components
     */
    private void resetThings()
    {

    }

    /**
     * returns empty boolean array in size MyStatic.BOARD_ROWS
     * @return
     */
    private boolean[] getResetWinningsArray()
    {
        boolean[] arr = new boolean[MyStatic.BOARD_ROWS];

        for (int i = 0; i < MyStatic.BOARD_ROWS; i++)
            arr[i] = false;

        return arr;
    }

    @Override
    protected void onStart() {
        super.onStart();
        resetThings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetThings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        resetThings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        resetThings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetThings();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        resetThings();
    }
}
