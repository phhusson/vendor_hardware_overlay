package me.phh.qti.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.media.AudioSystem;
import android.os.IBinder;

import vendor.qti.hardware.radio.am.V1_0.IQcRilAudioCallback;
import vendor.qti.hardware.radio.am.V1_0.IQcRilAudio;

public class Service extends android.app.Service {
	@Override public void onCreate() {
		new java.lang.Thread() {
			@Override
			public void run() {
				android.util.Log.d("PHH", "Hello");
				try {
					service = IQcRilAudio.getService("slot1");
					service.setCallback(cb);

					service = IQcRilAudio.getService("slot2");
					service.setCallback(cb);
				} catch(Exception e) {
					android.util.Log.d("PHH", "Failed setting callback", e);
				}
			}
		}.start();
	}

	IQcRilAudio service;
	IQcRilAudioCallback cb = new IQcRilAudioCallback.Stub() {
		@Override
		public String getParameters(String parameter) {
			android.util.Log.d("PHH", "Got getParameters " + parameter);
			try {
				return AudioSystem.getParameters(parameter);
			} catch(Exception e) { 
				android.util.Log.d("PHH", "Failed getting parameters");
			}
			return "";
		}

		@Override
		public int setParameters(String parameters) {
			android.util.Log.d("PHH", "Got setParameters " + parameters);
			try {
				AudioSystem.setParameters(parameters);
			} catch(Exception e) { 
				android.util.Log.d("PHH", "Failed setting parameters");
			}
			return 0;
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
