package com.mity;

import java.util.ArrayList;
import java.util.Random;

import com.mity.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.SystemClock;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class Main extends Activity implements SensorEventListener {

	/*
	 * A bunch of variables to be used across the methods.
	 */

	private float x, y, z, prevX, prevY, prevZ, maxX, maxY, maxZ;
	private float dX, dY, dZ;
	float g = (float) 9.81;
	float result = (float) 0.0;
	private SensorManager sensorManager;
	private Sensor accelerometer, orientation, linearAccelerometer;
	private final float noise = (float) 0.001;
	private TextView res;
	private FrameLayout textBody;
	private LinearLayout headerBody;
	private ImageView logo;
	private boolean grip = false;
	private long beginTime, endTime, elapsedTime;
	private ArrayList<Float> yAccel, xAccel, flushX, flushY, zAccel, flushZ;
	private ArrayList<Long> yTimes, xTimes, flushXTimes, flushYTimes, zTimes,
			flushZTimes;
	private Random generator = new Random(SystemClock.uptimeMillis());
	private MediaPlayer mp;

	private AnimationDrawable birdAnimation;
	private Animation slideDown, slideRight;
	private boolean startScreen = true;
	private boolean measure = false;
	private boolean thrown = false;
	private MotionEvent upEvent;
	float xContainer = (float) 0.0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, orientation,
				SensorManager.SENSOR_DELAY_GAME);
		linearAccelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		sensorManager.registerListener(this, linearAccelerometer,
				SensorManager.SENSOR_DELAY_GAME);

		yAccel = new ArrayList<Float>();
		xAccel = new ArrayList<Float>();
		zAccel = new ArrayList<Float>();

		flushX = new ArrayList<Float>();
		flushY = new ArrayList<Float>();
		flushZ = new ArrayList<Float>();

		yTimes = new ArrayList<Long>();
		xTimes = new ArrayList<Long>();
		zTimes = new ArrayList<Long>();

		flushXTimes = new ArrayList<Long>();
		flushYTimes = new ArrayList<Long>();
		flushZTimes = new ArrayList<Long>();

		textBody = (FrameLayout) findViewById(R.id.body);
		headerBody = (LinearLayout) findViewById(R.id.linearLayout1);
		logo = (ImageView) findViewById(R.id.imageView1);

		mp = MediaPlayer.create(getBaseContext(), R.raw.aamunkoi);
		mp.setLooping(true);
		mp.start();

	}

	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, orientation,
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, linearAccelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		mp.start();
	}

	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
		mp.stop();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	/**
	 * A method for numerical integration, returns a velocity when given an
	 * input arrays of accelerations and corresponding timestamps
	 **/

	private float integrate(ArrayList<Float> accel, ArrayList<Long> times) {
		float sum = (float) 0;
		float avrg;
		if (!accel.isEmpty() && accel.size() > 1) {
			for (int i = 0; i < accel.size() - 1; i++) {
				avrg = (accel.get(i) + accel.get(i + 1)) / (float) 2;
				sum += avrg
						* (float) (((float) (times.get(i + 1) - times.get(i))) / (float) 1000.0);
			}
		} else if (accel.size() == 1) {
			sum = (float) ((float) elapsedTime / (float) 1000) * (float) 0.5
					* accel.get(0);
		}
		return sum;
	}

	/**
	 * Calculates the mean of an array, not used in the program but was used
	 * during testing to validate integration results. Could be used for
	 * something in the future.
	 **/

	private float averageAcceleration(ArrayList<Float> list) {
		float sum = (float) 0.0;
		for (int i = 0; i < list.size(); i++) {
			sum += list.get(i);
		}
		if (list.isEmpty() || list.size() == 1) {
			return (float) 0;
		} else {
			return sum / (float) (list.size());
		}
	}

	/**
	 * Selects the list with more samples or combines the lists using
	 * Pythagorean theorem if they are of the same size
	 **/

	private Tuple<ArrayList<Float>, ArrayList<Long>> combineXZ(
			Tuple<ArrayList<Float>, ArrayList<Long>> xTuple,
			Tuple<ArrayList<Float>, ArrayList<Long>> zTuple) {
		if (xTuple.x.size() <= 1) {
			return zTuple;
		} else if (zTuple.x.size() <= 1) {
			return xTuple;
		} else if (xTuple.x.size() == zTuple.x.size()) {
			Tuple<ArrayList<Float>, ArrayList<Long>> retVal = new Tuple<ArrayList<Float>, ArrayList<Long>>(
					new ArrayList<Float>(), new ArrayList<Long>());
			for (int i = 0; i < xTuple.x.size(); i++) {
				retVal.x.add((float) Math.sqrt((zTuple.x.get(i) * zTuple.x
						.get(i)) + (xTuple.x.get(i) * xTuple.x.get(i))));
				retVal.y.add(xTuple.y.get(i));
			}
			return retVal;
		} else if (xTuple.x.size() > zTuple.x.size()) {
			return xTuple;
		} else {
			return zTuple;
		}
	}

	/** The main method that calculates the throwing distance. **/

	private float throwPhone(float xAcc, float yAcc, float zAcc, float vx,
			float vy) {
		float time = ((float) (2 * 2)) * (vy) / g;
		float distance = time * vx;
		time = (float) 1.8
				/ (((float) elapsedTime / (float) 1000.0) * (g + yAcc));
		float retVal = distance + time * vx;
		float variance = generator.nextFloat() * (float) 0.001;
		if (generator.nextBoolean()) {
			variance = -variance;
		}
		if (distance == (float) 0.0) {
			return retVal;
		} else {
			if (retVal + variance < (float) 0) {
				return retVal;
			}
			return retVal + variance;
		}
	}

	/** Gathering of the samples. **/

	@SuppressWarnings("unchecked")
	private void sample(float delta, ArrayList<Float> samples,
			ArrayList<Long> temporalSamples, long time,
			ArrayList<Float> flushSamples, ArrayList<Long> flushTimes,
			float newSample) {
		if (Math.abs(delta) > noise) {
			if (delta <= (float) 0.0) {
				delta = (float) 0.0;
				if (samples.size() >= 1) {
					// Not type safe, but I'm pretty sure the types are correct.
					flushSamples = (ArrayList<Float>) samples.clone();
					flushTimes = (ArrayList<Long>) temporalSamples.clone();
				}
				samples.clear();
				temporalSamples.clear();
			} else {
				samples.add(Math.abs(newSample) + delta);
				temporalSamples.add(time);
			}
		}
	}

	/** Touch event handling. Not clean, but works. **/

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (thrown) {
				setContentView(R.layout.start);
				thrown = false;
				measure = false;
			} else {
				grip = true;
				yAccel.clear();
				xAccel.clear();
				xAccel.add((float) 0.0);
				yAccel.add((float) 0.0);
				beginTime = SystemClock.elapsedRealtime();
				xTimes.add(beginTime);
				yTimes.add(beginTime);
			}
			return true;
		case MotionEvent.ACTION_UP:
			upEvent = event;
			if (startScreen && upEvent.getX() > (xContainer + (float) 200)) {
				xContainer = (float) 0.0;
				slideDown = AnimationUtils.loadAnimation(this,
						R.anim.slide_out_bottom);
				textBody.startAnimation(slideDown);
				textBody.setVisibility(View.INVISIBLE);
				slideRight = AnimationUtils.loadAnimation(this,
						R.anim.slide_out_side);
				headerBody.startAnimation(slideRight);
				logo.startAnimation(slideRight);
				logo.setVisibility(View.INVISIBLE);
				headerBody.setVisibility(View.INVISIBLE);
				startScreen = false;

			} else if (startScreen
					&& upEvent.getX() < (xContainer - (float) 200)) {
				xContainer = (float) 0.0;
				TextView container = (TextView) findViewById(R.id.textView2);
				container
						.setText("Programming lead:\nMiro Nurmela\n\nArtistic lead:\nPäivi Tynninen\n\nSwipe right to get throwing.");
			} else if (!startScreen && !measure) {
				measure = true;
			} else if (!startScreen && measure && !thrown) {
				grip = false;
				endTime = SystemClock.elapsedRealtime();
				elapsedTime = endTime - beginTime;
				float temp = (float) 0.0;
				float avY, avX, intX, intY;
				if (xAccel.size() <= 1 && flushX != null) {
					xAccel = flushX;
					xTimes = flushXTimes;
				}
				if (xAccel.isEmpty()) {
					xAccel.add(Math.abs(x));
				}
				if (yAccel.size() <= 1 && flushY != null) {
					yAccel = flushY;
					yTimes = flushYTimes;
				}
				if (yAccel.isEmpty()) {
					yAccel.add(Math.abs(y));
				}
				if (zAccel.size() <= 1 && flushY != null) {
					zAccel = flushZ;
					zTimes = flushZTimes;
				}
				if (zAccel.isEmpty()) {
					zAccel.add(Math.abs(z));
				}
				Tuple<ArrayList<Float>, ArrayList<Long>> xTuple = new Tuple<ArrayList<Float>, ArrayList<Long>>(
						xAccel, xTimes);
				new Tuple<ArrayList<Float>, ArrayList<Long>>(zAccel, zTimes);
				Tuple<ArrayList<Float>, ArrayList<Long>> yTuple = new Tuple<ArrayList<Float>, ArrayList<Long>>(
						yAccel, yTimes);
				xTuple = combineXZ(xTuple, yTuple);
				avX = averageAcceleration(xAccel);
				avY = averageAcceleration(yAccel);
				intX = integrate(xTuple.x, xTuple.y);
				intY = integrate(zAccel, zTimes);
				temp = throwPhone(avX, avY, dZ, intX, intY);
				if (temp > result) {
					result = temp;
				}
				yAccel.clear();
				xAccel.clear();
				zAccel.clear();
				xTimes.clear();
				yTimes.clear();
				zTimes.clear();
				if (temp > (float) 0.5) {
					setContentView(R.layout.end_success);
					res = (TextView) findViewById(R.id.result);
					ImageView birdImage = (ImageView) findViewById(R.id.flight);
					birdImage.setBackgroundResource(R.drawable.bird_flight);
					birdAnimation = (AnimationDrawable) birdImage
							.getBackground();
					birdAnimation.start();
					res.setText("Congratulations! The bird flew "
							+ Float.toString(temp) + "meters.");
				} else {
					setContentView(R.layout.end_fail);
					res = (TextView) findViewById(R.id.result);
					res.setText("Oops! The bird only flew "
							+ Float.toString(temp)
							+ "meters. Tap to try again.");
				}
				thrown = true;
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (xContainer == (float) 0.0) {
				xContainer = event.getX();
			}
		default:
			return false;
		}
	}

	/** Sensor listener, feeds sensory data to the sample-method **/

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor source = event.sensor;
		long time = SystemClock.elapsedRealtime();
		prevX = prevY = prevZ = (float) 0.0;
		if (source.equals(accelerometer)) {
		} else if (source.equals(linearAccelerometer)) {
			x = event.values[0];
			y = event.values[1];
			z = event.values[2];
			if (grip) {
				dX = x - prevX;
				dY = y - prevY;
				dZ = z - prevZ;
				sample(dX, xAccel, xTimes, time, flushX, flushXTimes, x);

				sample(dY, yAccel, yTimes, time, flushY, flushYTimes, y);

				sample(dZ, zAccel, zTimes, time, flushZ, flushZTimes, z);

				if (x > maxX) {
					maxX = z;
				}
				if (y > maxY) {
					maxY = z;
				}
				if (z > maxZ) {
					maxZ = z;
				}
				prevX = x;
				prevY = y;
				prevZ = z;
			}
		}
	}
}