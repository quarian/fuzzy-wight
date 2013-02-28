package com.authorwjf;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Main extends Activity implements SensorEventListener {
	
	private float mLastX, mLastY, mLastZ;
	private float maxX, maxY, maxZ;
	float t = (float)0.1;
	float g = (float)9.81;
	float result = (float)0.0;
	private boolean mInitialized;
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 2.0;
	 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	private float throw_phone(float x, float y, float z) {
		float time = 2 * ((t * y)/g);
		float distance = 1/2 * x * time * time;
		return distance;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		TextView tvX= (TextView)findViewById(R.id.x_axis);
		TextView tvY= (TextView)findViewById(R.id.y_axis);
		TextView tvZ= (TextView)findViewById(R.id.z_axis);
		TextView tvX_max= (TextView)findViewById(R.id.x_max);
		TextView tvY_max= (TextView)findViewById(R.id.y_max);
		TextView tvZ_max= (TextView)findViewById(R.id.z_max);
		TextView res= (TextView)findViewById(R.id.result);
		ImageView iv = (ImageView)findViewById(R.id.image);
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			maxX = x;
			maxY = y;
			maxZ = z;
			result = throw_phone(x, y, z);
			tvX.setText("0.0");
			tvY.setText("0.0");
			tvZ.setText("0.0");
			tvX_max.setText(Float.toString(x));
			tvY_max.setText(Float.toString(y));
			tvZ_max.setText(Float.toString(z));
			res.setText("0.0");
			mInitialized = true;
		} else {
			float deltaX = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);
			float deltaZ = Math.abs(mLastZ - z);
			if (deltaX < NOISE) deltaX = (float)0.0;
			if (deltaY < NOISE) deltaY = (float)0.0;
			if (deltaZ < NOISE) deltaZ = (float)0.0;
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			float temp;
			temp = throw_phone(mLastX, mLastY, mLastZ);
			tvX.setText(Float.toString(deltaX));
			tvY.setText(Float.toString(deltaY));
			tvZ.setText(Float.toString(deltaZ));
			if (mLastX > maxX) {
				maxX = mLastX;
				tvX.setText(Float.toString(mLastX));
			}
			if (mLastY > maxY) {
				maxY = mLastY;
				tvX.setText(Float.toString(mLastY));
			}
			if (mLastZ > maxZ) {
				maxZ = mLastZ;
				tvX.setText(Float.toString(mLastZ));
			}
			if (temp > result) {
				result = temp;
				res.setText(Float.toString(result));
			}		
			iv.setVisibility(View.VISIBLE);
			if (deltaX > deltaY) {
				iv.setImageResource(R.drawable.horizontal);
			} else if (deltaY > deltaX) {
				iv.setImageResource(R.drawable.vertical);
			} else {
				iv.setVisibility(View.INVISIBLE);
			}
		}
	}
}