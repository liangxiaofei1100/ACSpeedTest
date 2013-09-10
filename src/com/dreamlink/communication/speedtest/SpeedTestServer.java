package com.dreamlink.communication.speedtest;

import java.util.Timer;
import java.util.TimerTask;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.CommunicationManager;
import com.dreamlink.communication.lib.CommunicationManager.OnCommunicationListener;
import com.dreamlink.communication.lib.CommunicationManager.OnConnectionChangeListener;
import com.dreamlink.communication.lib.util.Notice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * see {@code SpeedTestActivity}.
 * 
 */
public class SpeedTestServer extends Activity implements
		OnCommunicationListener, OnConnectionChangeListener {
	private static final String TAG = "SpeedTestServer";
	private Notice mNotice;
	private TextView mSpeedTextView;
	private EditText mSizeEditText;
	private Button mStartButton;
	private boolean mIsStarted;
	private boolean mStop;

	private long mStartTime = 0;
	private long mTotalSize = 0;

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
		mNotice = new Notice(this);

		// Connect to Communication Service.
		mCommunicationManager = new CommunicationManager(
				getApplicationContext());
		mCommunicationManager.connectCommunicatonService(this, this, mAppID);

		mStop = false;
		mIsStarted = false;
	}

	private void initView() {
		mSpeedTextView = (TextView) findViewById(R.id.tvSpeedTestSpeed);
		mSizeEditText = (EditText) findViewById(R.id.etSpeedTest);
		mStartButton = (Button) findViewById(R.id.btnSpeedTestStart);

		mStartButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String sizeString = mSizeEditText.getText().toString();
				if (mIsStarted) {
					mNotice.showToast("Test is already started");
					return;
				}
				if (!TextUtils.isEmpty(sizeString)) {
					int size = Integer.valueOf(sizeString);
					mIsStarted = true;
					startTest(size);
				}
			}
		});
	}

	private void startTest(int size) {
		int userNumber = mCommunicationManager.getAllUser().size();

		if (userNumber > 0) {
			Log.d(TAG, "start Test");
			TestThread testThread = new TestThread(size);
			testThread.start();

			mShowSpeedTimer = new Timer();
			mShowSpeedTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					mHandler.sendEmptyMessage(0);
				}
			}, 1000, 1000);
		} else {
			Log.d(TAG, "No connection!");
			mNotice.showToast("No connection!");
		}
	}

	private class TestThread extends Thread {
		private int mSize = 0;
		private byte[] mData;

		public TestThread(int size) {
			mSize = size;
			mData = new byte[size];
		}

		@Override
		public void run() {
			Log.d(TAG, "start Test, run");

			mStartTime = System.currentTimeMillis();
			while (!mStop) {
				if (mCommunicationManager.sendMessageToAll(mData)) {
					mTotalSize += mSize;
				} else {
					Log.e(TAG, "sendMessageToAll error.");
					break;
				}
			}
		}
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mSpeedTextView.setText(SpeedTestActivity.getSpeedText(mTotalSize,
					mStartTime));
		};
	};

	@Override
	protected void onDestroy() {
		mStop = true;
		if (mShowSpeedTimer != null) {
			mShowSpeedTimer.cancel();
			mShowSpeedTimer = null;
		}
		mCommunicationManager.disconnectCommunicationService();
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
		Log.d(TAG, "onReceiveMessage:" + String.valueOf(msg));
		Message message = mHandler.obtainMessage();
		message.obj = new String(msg);
		mHandler.sendMessage(message);
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
