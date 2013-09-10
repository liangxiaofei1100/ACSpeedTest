package com.dreamlink.communication.speedtest;

import java.util.Timer;
import java.util.TimerTask;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.CommunicationManager;
import com.dreamlink.communication.lib.CommunicationManager.OnCommunicationListener;
import com.dreamlink.communication.lib.CommunicationManager.OnConnectionChangeListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * see {@code SpeedTestActivity}.
 * 
 */
public class SpeedTestClient extends Activity implements
		OnCommunicationListener, OnConnectionChangeListener {
	@SuppressWarnings("unused")
	private static final String TAG = "SpeedTestClient";
	private TextView mSpeedTextView;
	private EditText mSizeEditText;
	private Button mStartButton;
	private long mStartTime;
	private long mTotalSize;
	/** Show speed ever 1 second. */
	private Timer mShowSpeedTimer;

	/** Speed test app id */
	private int mAppID = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mAppID = intent.getIntExtra(SpeedTestActivity.EXTRA_APP_ID, 0);

		setContentView(R.layout.test_speed);
		initView();

		// Connect to Communication Service.
		mCommunicationManager = new CommunicationManager(
				getApplicationContext());
		mCommunicationManager.connectCommunicatonService(this, this, mAppID);
	}

	private void initView() {
		mSpeedTextView = (TextView) findViewById(R.id.tvSpeedTestSpeed);
		mSizeEditText = (EditText) findViewById(R.id.etSpeedTest);
		mSizeEditText.setVisibility(View.GONE);
		mStartButton = (Button) findViewById(R.id.btnSpeedTestStart);
		mStartButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mStartTime = System.currentTimeMillis();

				// Show speed ever 1 second.
				mShowSpeedTimer = new Timer();
				mShowSpeedTimer.schedule(new TimerTask() {

					@Override
					public void run() {
						mHandler.sendEmptyMessage(0);
					}
				}, 1000, 1000);
			}
		});
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mSpeedTextView.setText(SpeedTestActivity.getSpeedText(mTotalSize,
					mStartTime));
		};
	};

	@Override
	protected void onDestroy() {
		mCommunicationManager.disconnectCommunicationService();
		if (mShowSpeedTimer != null) {
			mShowSpeedTimer.cancel();
			mShowSpeedTimer = null;
		}
		super.onDestroy();
	}

	// Communication Service begin
	private CommunicationManager mCommunicationManager;

	@Override
	public void onCommunicationDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCommunicationConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReceiveMessage(byte[] msg, User sendUser) {
		mTotalSize += msg.length;
	}

	@Override
	public void onUserConnected(User user) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserDisconnected(User user) {
		// TODO Auto-generated method stub

	}
	// Communication Service end
}
