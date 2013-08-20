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
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dreamlink.aidl.Communication;
import com.dreamlink.aidl.OnCommunicationListenerExternal;
import com.dreamlink.aidl.User;
import com.dreamlink.communication.api.CommunicateService;

/**
 * see {@code SpeedTest}.
 * 
 */
public class SpeedTestClient extends Activity {
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
		mAppID = intent.getIntExtra(
				AndroidCommunicationSpeedTestActivity.EXTRA_APP_ID, 0);

		setContentView(R.layout.test_speed);
		initView();

		// Connect to Communication Service.
		connectCommunicationService();
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
			mSpeedTextView.setText(AndroidCommunicationSpeedTestActivity
					.getSpeedText(mTotalSize, mStartTime));
		};
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
			mTotalSize += msg.length;
		}
	};
	// Communication Service end

}
