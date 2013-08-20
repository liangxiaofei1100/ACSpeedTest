package com.dreamlink.communication.speedtest;

import java.text.DecimalFormat;

import com.dreamlink.communication.api.AppUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class AndroidCommunicationSpeedTestActivity extends Activity {
	public static final String EXTRA_APP_ID = "app_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	private void launchActivity(boolean isServer) {
		Intent intent = new Intent();

		int appID = AppUtil.getAppID(this);
		intent.putExtra(EXTRA_APP_ID, appID);

		if (isServer) {
			intent.setClass(this, SpeedTestServer.class);
		} else {
			intent.setClass(this, SpeedTestClient.class);
		}
		startActivity(intent);
	}

	public static String sizeFormat(double size) {
		if (size > 1024 * 1024) {
			Double dsize = (double) (size / (1024 * 1024));
			return new DecimalFormat("#.00").format(dsize) + "MB";
		} else if (size > 1024) {
			Double dsize = (double) size / (1024);
			return new DecimalFormat("#.00").format(dsize) + "KB";
		} else {
			return String.valueOf((int) size) + " Bytes";
		}
	}

	public static String getSpeedText(long totalSize, long startTime) {
		long currentTime = System.currentTimeMillis();
		long speed = totalSize / ((currentTime - startTime) / 1000);
		return "Total Size: " + sizeFormat(totalSize) + "Speed: "
				+ sizeFormat(speed) + "/S";
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_client:
			launchActivity(false);
			finish();
			break;
		case R.id.menu_server:
			launchActivity(true);
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}