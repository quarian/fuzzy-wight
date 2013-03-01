package com.mity;

import com.mity.R;

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
	
	private float prevX, prevY, prevZ;
	private float maxX, maxY, maxZ;
	float t = (float)0.1;
	float g = (float)9.81;
	float result = (float)0.0;
	private boolean initialized;
	private SensorManager sensorManager;
    private Sensor accelerometer;
    private final float noise = (float) 2.0;
	 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initialized = false;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_FASTEST);
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

	private float throw_phone(float x, float y, float z) {
		float time = (float)2 * ((t * y)/g);
		float distance = (float)0.5 * x * time;
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
		TextView bestRes = (TextView)findViewById(R.id.debug);
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
			res.setText(Float.toString(result));
			bestRes.setText(Float.toString(result));
			initialized = true;
		} else {
			float dX = Math.abs(prevX - x);
			float dY = Math.abs(prevY - y);
			float dZ = Math.abs(prevZ - z);
			if (dX < noise) dX = (float)0.0;
			if (dY < noise) dY = (float)0.0;
			if (dZ < noise) dZ = (float)0.0;
			prevX = x;
			prevY = y;
			prevZ = z;
			float temp;
			temp = throw_phone(dX, dY, dZ);
			tvX.setText(Float.toString(dX));
			tvY.setText(Float.toString(dY));
			tvZ.setText(Float.toString(dZ));
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
			if (temp > result) {
				result = temp;
				bestRes.setText(Float.toString(result));
			} 
			res.setText(Float.toString(temp));
		}
	}
}