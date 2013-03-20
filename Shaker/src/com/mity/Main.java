package com.mity;

import java.util.ArrayList;
import java.util.Random;

import com.mity.R;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.method.Touch;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.SystemClock;


public class Main extends Activity implements SensorEventListener {
	private float prevX, prevY, prevZ;
	private float maxX, maxY, maxZ;
	private float dX, dY, dZ;
	//float t = (float)0.1;
	float g = (float)9.81;
	float result = (float)0.0;
	private boolean initialized;
	private SensorManager sensorManager;
    private Sensor accelerometer;
    private final float noise = (float) 2.0;
    private TextView touching, touchTime, bestRes, res;
    private boolean grip = false;
    private long beginTime, endTime, elapsedTime;
    private ArrayList<Float> yAccel, xAccel;
    private Random generator = new Random(SystemClock.uptimeMillis());
	 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initialized = false;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_FASTEST);
		touching = (TextView)findViewById(R.id.touching);
		touchTime = (TextView)findViewById(R.id.time);
		res= (TextView)findViewById(R.id.result);
		bestRes = (TextView)findViewById(R.id.debug);
		touching.setText(Boolean.toString(false));
		touchTime.setText(Long.toString((long) 0));
		res.setText(Float.toString(result));
		bestRes.setText(Float.toString(result));
		yAccel = new ArrayList<Float>();
		xAccel = new ArrayList<Float>();
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}
	
	private float averageAcceleration(ArrayList<Float> list) {
		float sum = (float) 0.0;
		for (int i = 0; i < list.size(); i++) {
			sum += list.get(i);
		}
		if (list.isEmpty() || list.size() == 1) {
			return (float) 0;
		} else {
			return sum / (float)(list.size() - 1);
		}
	}

	private float throwPhone(float x, float y, float z) {
		System.out.println("x  = " + x + ", y = " + y);
		float time = (float) 2 * (((float)elapsedTime/(float)1000.0) * y)/g;
		System.out.println("Time = " + time);
		float distance = (float)0.5 * x * time;
		System.out.println("Distance = " + distance);
		time = (float) 1.8 * ((float)elapsedTime/(float)1000.0) * (g + y);
		System.out.println("Remaining time = " + time);
		float retVal = distance + x * time;
		System.out.println("Returned = " + retVal);
		float variance = generator.nextFloat();
		if (generator.nextBoolean()) {
			variance = -variance;
		}
		if (distance == (float )0.0) {
			return distance;
		} else {
			return distance + variance;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				grip = true;
				yAccel.clear();
				beginTime = SystemClock.elapsedRealtime();
				touching.setText(Boolean.toString(grip));
				touchTime.setText(Long.toString((long)0));
				return true;
			case MotionEvent.ACTION_UP:
				grip = false;
				touching.setText(Boolean.toString(grip));
				endTime = SystemClock.elapsedRealtime();
				elapsedTime = endTime - beginTime;
				touchTime.setText(Float.toString((float)elapsedTime/(float)1000.0));
				float temp;
				float avY, avX;
				avX = averageAcceleration(xAccel);
				avY = averageAcceleration(yAccel);
				System.out.println("List of X: " + xAccel);
				System.out.println("List of Y: " + yAccel);
				System.out.println("avX = " + avX);
				System.out.println("avY = " + avY);
				temp = throwPhone(avX, avY, dZ);
				if (temp > result) {
					result = temp;
					bestRes.setText(Float.toString(result));
				} 
				res.setText(Float.toString(temp));
				return true;
			default:
				return false;
			}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		TextView tvX= (TextView)findViewById(R.id.x_axis);
		TextView tvY= (TextView)findViewById(R.id.y_axis);
		TextView tvZ= (TextView)findViewById(R.id.z_axis);
		TextView tvX_max= (TextView)findViewById(R.id.x_max);
		TextView tvY_max= (TextView)findViewById(R.id.y_max);
		TextView tvZ_max= (TextView)findViewById(R.id.z_max);

		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!initialized) {
			prevX = x;
			prevY = y;
			prevZ = z;
			maxX = x;
			maxY = y;
			maxZ = z;
			tvX.setText("0.0");
			tvY.setText("0.0");
			tvZ.setText("0.0");
			tvX_max.setText(Float.toString(x));
			tvY_max.setText(Float.toString(y));
			tvZ_max.setText(Float.toString(z));		
			initialized = true;
		} else {
			if (grip) {
				dX = Math.abs(prevX - x);
				dY = Math.abs(prevY - y);
				dZ = Math.abs(prevZ - z);
				if (dX < noise) {
					dX = (float)0.0;
					xAccel.clear();
				}
				if (dY < noise) { 
					dY = (float)0.0;
					yAccel.clear();
				}
				if (dZ < noise) dZ = (float)0.0;
				prevX = x;
				prevY = y;
				prevZ = z;

				tvX.setText(Float.toString(dX));
				tvY.setText(Float.toString(dY));
				tvZ.setText(Float.toString(dZ));
				yAccel.add(dY);
				xAccel.add(dX);
				if (prevX > maxX) {
					maxX = prevX;
					tvX_max.setText(Float.toString(prevX));
				}
				if (prevY > maxY) {
					maxY = prevY;
					tvY_max.setText(Float.toString(prevY));
				}
				if (prevZ > maxZ) {
					maxZ = prevZ;
					tvZ_max.setText(Float.toString(prevZ));
				}

			}
		}
	}
}