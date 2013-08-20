package com.dreamlink.communication.speedtest;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dreamlink.aidl.Communication;
import com.dreamlink.aidl.OnCommunicationListenerExternal;
import com.dreamlink.aidl.User;
import com.dreamlink.communication.api.CommunicateService;
import com.dreamlink.communication.api.Notice;

/**
 * see {@code SpeedTest}.
 * 
 */
public class SpeedTestServer extends Activity {
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
		mAppID = intent.getIntExtra(
				AndroidCommunicationSpeedTestActivity.EXTRA_APP_ID, 0);

		setContentView(R.layout.test_speed);
		initView();
		mNotice = new Notice(this);

		// Connect to Communication Service.
		connectCommunicationService();

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
		int userNumber = 0;
		try {
			userNumber = mCommunication.getAllUser().size();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

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
				try {
					mCommunication.sendMessage(mData, mAppID, null);
					mTotalSize += mSize;
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mSpeedTextView.setText(AndroidCommunicationSpeedTestActivity
					.getSpeedText(mTotalSize, mStartTime));
		};
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mStop = true;
		if (mShowSpeedTimer != null) {
			mShowSpeedTimer.cancel();
			mShowSpeedTimer = null;
		}
	}

	// Communication Service begin
	private Communication mCommunication;

	private boolean connectCommunicationService() {
		ServiceConnection connection = new ServiceConnection() {

			public void onServiceDisconnected(ComponentName name) {
				try {
					mCommunication
							.unRegistListenr(mCommunicationListenerExternal);
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				mCommunication = Communication.Stub.asInterface(service);
				try {
					mCommunication.registListenr(
							mCommunicationListenerExternal, mAppID);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		};
		Intent communicateIntent = new Intent(
				CommunicateService.ACTION_COMMUNICATE_SERVICE);
		return bindService(communicateIntent, connection,
				Context.BIND_AUTO_CREATE);
	}

	private OnCommunicationListenerExternal mCommunicationListenerExternal = new OnCommunicationListenerExternal.Stub() {

		@Override
		public void onUserDisconnected(User user) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserConnected(User user) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReceiveMessage(byte[] msg, User sendUser)
				throws RemoteException {
			Log.d(TAG, "onReceiveMessage:" + String.valueOf(msg));
			Message message = mHandler.obtainMessage();
			message.obj = new String(msg);
			mHandler.sendMessage(message);

		}
	};
	// Communication Service end
}
