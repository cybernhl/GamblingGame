package com.ssaurel.dicer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

	public static final Random RANDOM = new Random();
	private Button rollDices;
	private ImageView imageView1, imageView2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		rollDices = (Button) findViewById(R.id.rollDices);
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		imageView2 = (ImageView) findViewById(R.id.imageView2);

		rollDices.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final Animation anim1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
				final Animation anim2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
				final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						int value = randomDiceValue();
						int res = getResources().getIdentifier("dice_" + value, "drawable", "com.ssaurel.dicer");

						if (animation == anim1) {
							imageView1.setImageResource(res);
						} else if (animation == anim2) {
							imageView2.setImageResource(res);
						}
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}
				};

				anim1.setAnimationListener(animationListener);
				anim2.setAnimationListener(animationListener);

				imageView1.startAnimation(anim1);
				imageView2.startAnimation(anim2);
			}
		});
	}

	public static int randomDiceValue() {
		return RANDOM.nextInt(6) + 1;
	}
}