package com.mity;

import java.util.ArrayList;
import java.util.Random;

import com.mity.R;

import android.app.Activity;
import android.content.Context;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.method.Touch;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.SystemClock;

public class Main extends Activity implements SensorEventListener {
	private float x, y, z, prevX, prevY, prevZ, maxX, maxY, maxZ, linX, linY,
			linZ;
	private float dX, dY, dZ;
	float g = (float) 9.81;
	float result = (float) 0.0;
	private SensorManager sensorManager;
	private Sensor accelerometer, orientation, linearAccelerometer;
	private final float noise = (float) 0.001;
	private TextView touching, touchTime, bestRes, res, tvX, tvY, tvZ, tvX_max,
			tvY_max, tvZ_max, mainView, header;
	private FrameLayout textBody;
	private LinearLayout headerBody;
	private ImageView logo, birdInNest;
	private RelativeLayout startScreenBody, throwScreenBody;
	private boolean grip = false;
	private long beginTime, endTime, elapsedTime;
	private ArrayList<Float> yAccel, xAccel, flushX, flushY, zAccel, flushZ;
	private ArrayList<Long> yTimes, xTimes, flushXTimes, flushYTimes, zTimes,
			flushZTimes;
	private Random generator = new Random(SystemClock.uptimeMillis());
	private MediaPlayer mp;
	
	private AnimationDrawable birdAnimation;
	private Animation slideDown;
	private Animation slideRight, slideInRight;
	private boolean startScreen = true;
	private boolean measure = false;
	private boolean credits = false;
	private boolean thrown = false;
	private MotionEvent downEvent, upEvent;
	float xContainer = (float) 0.0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, orientation,
				SensorManager.SENSOR_DELAY_GAME);
		linearAccelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		sensorManager.registerListener(this, linearAccelerometer,
				SensorManager.SENSOR_DELAY_GAME);
		
		//touching = (TextView) findViewById(R.id.touching);
		//touchTime = (TextView) findViewById(R.id.time);
		//res = (TextView) findViewById(R.id.result);
		//bestRes = (TextView) findViewById(R.id.debug);
		//touching.setText(Boolean.toString(false));
		//touchTime.setText(Long.toString((long) 0));
		//res.setText(Float.toString(result));
		//bestRes.setText(Float.toString(result));

		/*tvX = (TextView) findViewById(R.id.x_axis);
		tvY = (TextView) findViewById(R.id.y_axis);
		tvZ = (TextView) findViewById(R.id.z_axis);
		tvX_max = (TextView) findViewById(R.id.x_max);
		tvY_max = (TextView) findViewById(R.id.y_max);
		tvZ_max = (TextView) findViewById(R.id.z_max);*/
		/*tvX.setText("0.0");
		tvY.setText("0.0");
		tvZ.setText("0.0");
		tvX_max.setText("0.0");
		tvY_max.setText("0.0");
		tvZ_max.setText("0.0");*/

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
		
		
		
		mainView = (TextView) findViewById(R.id.textView2);
		header = (TextView) findViewById(R.id.textView1);
		startScreenBody = (RelativeLayout) findViewById(R.id.header);
		throwScreenBody = (RelativeLayout) findViewById(R.id.throw_screen);
		birdInNest = (ImageView) findViewById(R.id.imageView2);
		
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

	private float integrate(ArrayList<Float> accel, ArrayList<Long> times) {
		float sum = (float) 0;
		float avrg;
		if (!accel.isEmpty() && accel.size() > 1) {
			//System.out.println("Acceleration time = "
			//		+ (times.get(times.size() - 1) - times.get(0)));
			for (int i = 0; i < accel.size() - 1; i++) {
				avrg = (accel.get(i) + accel.get(i + 1)) / (float) 2;
				// avrg = accel.get(i + 1);
				sum += avrg
						* (float) (((float) (times.get(i + 1) - times.get(i))) / (float) 1000.0);
			}
		} else if (accel.size() == 1) {
			sum = (float) ((float) elapsedTime / (float) 1000) * (float) 0.5
					* accel.get(0);
		}
		return sum;
	}

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

	private float throwPhone(float xAcc, float yAcc, float zAcc, float vx,
			float vy) {
		//System.out.println("x  = " + xAcc + ", y = " + yAcc
		//		+ ", elapsed time = " + elapsedTime);
		float time = ((float) (2 * 2)) * (vy) / g;
		//System.out.println("Time = " + time);
		float distance = time * vx;
		//System.out.println("Distance = " + distance);
		time = (float) 1.8
				/ (((float) elapsedTime / (float) 1000.0) * (g + yAcc));
		//System.out.println("Remaining time = " + time + ", extra distance = "
		//		+ (vx * time));
		float retVal = distance + time * vx;
		//System.out.println("Returned = " + retVal);
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

	private void sample(float delta, ArrayList<Float> samples,
			ArrayList<Long> temporalSamples, long time,
			ArrayList<Float> flushSamples, ArrayList<Long> flushTimes,
			float newSample) {
		if (Math.abs(delta) > noise) {
			if (delta <= (float) 0.0) {
				delta = (float) 0.0;
				if (samples.size() >= 1) {
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downEvent = event;
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
				//touching.setText(Boolean.toString(grip));
				//touchTime.setText(Long.toString((long) 0));
			}
			return true;
		case MotionEvent.ACTION_UP:
			upEvent = event;
			//System.out.println("xContainer = " + xContainer);
			//System.out.println("upevent X = " + upEvent.getX());
			//System.out.println("downevent X =" + downEvent.getX());
			if (startScreen && upEvent.getX() > (xContainer + (float) 100)) {
				xContainer = (float) 0.0;
				slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
				textBody.startAnimation(slideDown);
				textBody.setVisibility(View.INVISIBLE);
				slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_side);
				slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
				headerBody.startAnimation(slideRight);
				logo.startAnimation(slideRight);
				logo.setVisibility(View.INVISIBLE);
				headerBody.setVisibility(View.INVISIBLE);
				//startScreenBody.startAnimation(slideRight);
				//startScreenBody.setVisibility(View.INVISIBLE);
				//setContentView(R.layout.start);
				//throwScreenBody.startAnimation(slideInRight);
				startScreen = false;
				
			} else if (startScreen && upEvent.getX() < (xContainer - (float) 100)) {
				xContainer = (float) 0.0;
				System.out.println("Credits!");
			} else if (!startScreen && !measure) {
				measure = true;
			} else if (!startScreen && measure && !thrown) {
				grip = false;
				//System.out
				//		.println("Accelerations on the moment of release (x, y, z): ("
				//				+ x + ", " + y + ", " + z + ")");
				//touching.setText(Boolean.toString(grip));
				endTime = SystemClock.elapsedRealtime();
				elapsedTime = endTime - beginTime;
				//touchTime.setText(Float.toString((float) elapsedTime
				//		/ (float) 1000.0));
				float temp = (float) 0.0;
				float avY,
				avX,
				intX,
				intY;
				//System.out.println("List of flushX: " + flushX);
				//System.out.println("xAccel.size() = " + xAccel.size());
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
				Tuple<ArrayList<Float>, ArrayList<Long>> zTuple = new Tuple<ArrayList<Float>, ArrayList<Long>>(
						zAccel, zTimes);
				Tuple<ArrayList<Float>, ArrayList<Long>> yTuple = new Tuple<ArrayList<Float>, ArrayList<Long>>(
						yAccel, yTimes);
				xTuple = combineXZ(xTuple, yTuple);
				avX = averageAcceleration(xAccel);
				avY = averageAcceleration(yAccel);
				intX = integrate(xTuple.x, xTuple.y);
				intY = integrate(zAccel, zTimes);
				//System.out.println("List of X: " + xAccel);
	
				//System.out.println("List of xTimes: " + xTimes);
				//System.out.println("List of Y: " + yAccel);
				//System.out.println("List of yTimes: " + yTimes);
				//System.out.println("list of Z:" + zAccel);
				//System.out.println("avX = " + avX);
				//System.out.println("avY = " + avY);
				//System.out.println("intX = " + intX);
				//System.out.println("intY = " + intY);
				temp = throwPhone(avX, avY, dZ, intX, intY);
				if (temp > result) {
					result = temp;
					//bestRes.setText(Float.toString(result));
				}
				yAccel.clear();
				xAccel.clear();
				zAccel.clear();
				xTimes.clear();
				yTimes.clear();
				zTimes.clear();
				//res.setText(Float.toString(temp));
				//birdAnimation.start();
				if (temp > (float) 0.5) {
					setContentView(R.layout.end_success);
					res = (TextView) findViewById(R.id.result);
					ImageView birdImage = (ImageView) findViewById(R.id.flight);
					birdImage.setBackgroundResource(R.drawable.bird_flight);
					birdAnimation = (AnimationDrawable) birdImage.getBackground();
					birdAnimation.start();
					res.setText("Congratulations! The bird flew " + Float.toString(temp) + "meters.");
				} else {
					setContentView(R.layout.end_fail);
					res = (TextView) findViewById(R.id.result);
					res.setText("Oops! The bird only flew " + Float.toString(temp) + "meters. Tap to try again.");
				}
				thrown = true;
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (xContainer == (float) 0.0) {
				xContainer = event.getX();
			}
			//System.out.println("Moving");
			//System.out.println("Moving x = " + event.getX());
			
		default:
			return false;
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor source = event.sensor;
		long time = SystemClock.elapsedRealtime();
		prevX = prevY = prevZ = (float) 0.0;
		if (source.equals(accelerometer)) {
			// System.out.println("Got stuff from accelerometer");
			/*
			 * x = event.values[0]; y = event.values[1]; z = event.values[2];
			 * 
			 * if (grip) { dX = x - prevX; dY = y - prevY; dZ = z - prevZ; if
			 * (Math.abs(dX) > noise) { if (dX <= (float) 0.0) { dX = (float)
			 * 0.0; if (xAccel.size() != 1) {
			 * System.out.println("Flushed x-accelerations " + xAccel); flushX =
			 * (ArrayList<Float>) xAccel.clone(); flushXTimes =
			 * (ArrayList<Long>) xTimes.clone();
			 * System.out.println("List of flushX: " + flushX); }
			 * xAccel.clear(); xTimes.clear(); } else { xAccel.add(Math.abs(x) +
			 * dX); xTimes.add(time); } } if (Math.abs(dY) > noise) { if (dY <=
			 * (float) 0.0) { dY = (float) 0.0; if (yAccel.size() != 1) {
			 * System.out.println("Flushed y-accelerations " + yAccel); flushY =
			 * (ArrayList<Float>) yAccel.clone(); flushYTimes =
			 * (ArrayList<Long>) yTimes.clone(); } yAccel.clear();
			 * yTimes.clear(); } else { yAccel.add(Math.abs(y) + dY);
			 * yTimes.add(time); } }
			 * 
			 * if (dZ == (float) 0.0) { dZ = (float) 0.0; }
			 * 
			 * tvX.setText(Float.toString(dX)); tvY.setText(Float.toString(dY));
			 * tvZ.setText(Float.toString(dZ));
			 * 
			 * if (x > maxX) { maxX = z; tvX_max.setText(Float.toString(prevX));
			 * } if (y > maxY) { maxY = z;
			 * tvY_max.setText(Float.toString(prevY)); } if (z > maxZ) { maxZ =
			 * z; tvZ_max.setText(Float.toString(prevZ)); } prevX = x; prevY =
			 * y; prevZ = z; }
			 */
		} else if (source.equals(linearAccelerometer)) {
			// System.out.println("Got stuff from linear accelerometer");
			x = event.values[0];
			y = event.values[1];
			z = event.values[2];
			// System.out.println("linear x = " + linX + ", linear y =" + linY);
			if (grip) {
				dX = x - prevX;
				dY = y - prevY;
				dZ = z - prevZ;
				sample(dX, xAccel, xTimes, time, flushX, flushXTimes, x);

				sample(dY, yAccel, yTimes, time, flushY, flushYTimes, y);

				sample(dZ, zAccel, zTimes, time, flushZ, flushZTimes, z);

				/*tvX.setText(Float.toString(dX));
				tvY.setText(Float.toString(dY));
				tvZ.setText(Float.toString(dZ));*/

				if (x > maxX) {
					maxX = z;
					//tvX_max.setText(Float.toString(prevX));
				}
				if (y > maxY) {
					maxY = z;
					//tvY_max.setText(Float.toString(prevY));
				}
				if (z > maxZ) {
					maxZ = z;
					//tvZ_max.setText(Float.toString(prevZ));
				}
				prevX = x;
				prevY = y;
				prevZ = z;
			}
		}
	}
}