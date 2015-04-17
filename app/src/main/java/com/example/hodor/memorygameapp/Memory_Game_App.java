package com.example.hodor.memorygameapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;


public class Memory_Game_App extends ActionBarActivity implements AnimationListener, OnClickListener {

    private static final int NUM_ROWS = 5;
    private static final int NUM_COLS = 4;
    private int whichCard;
    private Integer randomInts[] = new Integer[10];
    private Integer cards[][] = new Integer[20][2];
    private Animation animation1;
    private Animation animation2;
    private Animation animationShake;
    private Animation animationCapture;
    private Resources res;
    private Integer numClicked;
    private Integer clicked1;
    private Integer clicked2;
    private Integer playerScore;
    private Integer correctGuesses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory__game__app);

        //start game
        startNewGame();
    }

    private void startNewGame() {
        numClicked = 0;
        clicked1 = 0;
        clicked2 = 0;
        playerScore = 0;
        correctGuesses = 0;

        // update player score
        ((EditText)findViewById(R.id.playerScoreEditText)).setText(String.valueOf(playerScore));

        animation1 = AnimationUtils.loadAnimation(this, R.anim.to_middle);
        animation1.setAnimationListener(this);
        animation2 = AnimationUtils.loadAnimation(this, R.anim.from_middle);
        animation2.setAnimationListener(this);
        animationShake = AnimationUtils.loadAnimation(this, R.anim.shake_wrong);
        animationCapture = AnimationUtils.loadAnimation(this, R.anim.twist_capture);
        res = getResources();

        //generate 10 random numbers 0-31
        for(int i =0; i<10; i++) {
            Random random = new Random();
            int randomNum = random.nextInt(31);
            //check if random num in randomInt array
            while(Arrays.asList(randomInts).contains(randomNum)) {
                randomNum = random.nextInt(31);
            }
            randomInts[i] = randomNum;
        }

        //generate 20 random pairs of pokemon cards images and values
        int index = 0;
        for(int x=0; x<20; x++){
            for(int y=0; y<2; y++){
                cards[x][y] = randomInts[index];
            }
            if(index>8) {
                index = index-9;
            } else {
                index++;
            }
        }
//
        //populate cards
        populateCards();
    }

    private void populateCards() {
        int counter = 0;

        //get reference to table layout
        TableLayout table = (TableLayout) findViewById(R.id.cardsTableLayout);
        for(int row=0; row < NUM_ROWS; row++) {
            //create new table row
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f));
            //add row to table
            table.addView(tableRow);
            for(int col=0; col < NUM_COLS; col++) {
                //create new image view
                ImageView img = new ImageView(this);
                img.setImageResource(R.drawable.pokemon_card_back);
                img.setPadding(0,0,0,0);
                img.setId(counter);
                img.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f));

                img.setOnClickListener(this);

                //add to table row
                tableRow.addView(img);

                counter ++;
            }
        }
    } // populateCards

    @Override
    public void onClick(View v) {
        gridButtonClicked(v);
        numClicked++;
    }

    private void gridButtonClicked(View v) {
        whichCard = (v.getId());
        ImageView img = (ImageView)findViewById(whichCard);
        img.setEnabled(false);
        (img).clearAnimation();
        (img).setAnimation(animation1);
        (img).startAnimation(animation1);
    }// gridButtonClicked


    @Override
    public void onAnimationEnd(Animation animation) {
        ImageView img = (ImageView)findViewById(whichCard);
        if (animation==animation1) {
            String imgFile = "card_" + cards[whichCard][0];
            int resourceId = res.getIdentifier(imgFile, "drawable", getPackageName());
            img.setImageResource(resourceId);

            //if the user has guessed once, store value of first guess
            if(numClicked == 1) {
                clicked1 = whichCard;
            } else if(numClicked == 2) {
                clicked2 = whichCard;
                numClicked = 0;
                // start animation 2
                img.clearAnimation();
                img.setAnimation(animation2);
                img.startAnimation(animation2);
            }
        } else {
            // if matching pair
            ImageView img1 = (ImageView)findViewById(clicked1);
            ImageView img2 = (ImageView)findViewById(clicked2);
            int img1Val = cards[clicked1][1];
            int img2Val = cards[clicked2][1];

            if(img1Val == img2Val) {
                Toast.makeText(getApplicationContext(), "Gotcha! Pokemon was captured!",Toast.LENGTH_SHORT).show();

                // twist images
                img1.setAnimation(animationCapture);
                img1.startAnimation(animationCapture);
                img2.setAnimation(animationCapture);
                img2.startAnimation(animationCapture);

                //grey out images
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);

                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                img1.setColorFilter(filter);
                img2.setColorFilter(filter);
                playerScore++;
                correctGuesses++;
                // if end of game
                if(correctGuesses == 10) {
                    // alert player of end of game and report their score
                    new AlertDialog.Builder(this)
                            .setTitle("End of Game")
                            .setMessage("You beat the game with a pathetic score of: " + playerScore)
                            .setPositiveButton(R.string.new_game, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with new game
                                    // clear cards layout
                                    ((TableLayout)findViewById(R.id.cardsTableLayout)).removeAllViews();
                                    // start new game
                                    startNewGame();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "not a pair!",Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                // shake images
                img1.setAnimation(animationShake);
                img1.startAnimation(animationShake);
                img2.setAnimation(animationShake);
                img2.startAnimation(animationShake);
                // set images to back of card
                img1.setImageResource(R.drawable.pokemon_card_back);
                img2.setImageResource(R.drawable.pokemon_card_back);
                img1.setEnabled(true);
                img2.setEnabled(true);
                playerScore--;
            }
            // update Player Score
            ((EditText)findViewById(R.id.playerScoreEditText)).setText(String.valueOf(playerScore));
        }
    }
//
    @Override
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub

    }

}
