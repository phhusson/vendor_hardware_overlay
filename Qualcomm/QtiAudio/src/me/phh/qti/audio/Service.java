package me.phh.qti.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.IBinder;
import android.os.UEventObserver;
import android.os.SystemProperties;

public class Service extends android.app.Service {
	AudioManager audioManager;
	@Override public void onCreate() {
		String fp = SystemProperties.get("ro.vendor.build.fingerprint", "nothing");
		audioManager = getSystemService(AudioManager.class);

		new java.lang.Thread() {
			@Override
			public void run() {
				android.util.Log.d("PHH", "Hello");
				try {
					vendor.qti.hardware.radio.am.V1_0.IQcRilAudio service;
					service = vendor.qti.hardware.radio.am.V1_0.IQcRilAudio.getService("slot1");
					service.setCallback(cb);
				} catch(Exception e) {}
				try {
					vendor.qti.hardware.radio.am.V1_0.IQcRilAudio service;
					service = vendor.qti.hardware.radio.am.V1_0.IQcRilAudio.getService("slot2");
					service.setCallback(cb);
				} catch(Exception e) {}
				try {
					vendor.qti.qcril.am.V1_0.IQcRilAudio service;
					service = vendor.qti.qcril.am.V1_0.IQcRilAudio.getService("slot1");
					service.setCallback(cb2);
				} catch(Exception e) {}
				try {
					vendor.qti.qcril.am.V1_0.IQcRilAudio service;
					service = vendor.qti.qcril.am.V1_0.IQcRilAudio.getService("slot2");
					service.setCallback(cb2);
				} catch(Exception e) {}
                if(fp.contains("OnePlus6")) {
                    try {
                        (new UEventObserver() {
                            @Override
                            public void onUEvent(UEventObserver.UEvent event) {
                                try {
                                    android.util.Log.v("PHH", "USB UEVENT: " + event.toString());
                                    String state = event.get("STATE");

                                    boolean ringing = state.contains("USB=0");
                                    boolean silent = state.contains("(null)=0");
                                    boolean vibrate = state.contains("USB_HOST=0");
                                    android.util.Log.v("PHH", "Got ringing = " + ringing + ", silent = " + silent + ", vibrate = " + vibrate);
                                    if(ringing && !silent && !vibrate)
                                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                    if(silent && !ringing && !vibrate)
                                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                    if(vibrate && !silent && !ringing)
                                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                } catch(Exception e) {
                                    android.util.Log.d("PHH", "Failed parsing uevent", e);
                                }

                            }
                        }).startObserving("DEVPATH=/devices/platform/soc/soc:tri_state_key");
                    } catch(Exception e) {
                        android.util.Log.d("PHH", "Failed setting UEventObserver", e);
                    }
                }

            }
        }.start();
    }

    vendor.qti.hardware.radio.am.V1_0.IQcRilAudioCallback cb = new vendor.qti.hardware.radio.am.V1_0.IQcRilAudioCallback.Stub() {
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

    vendor.qti.qcril.am.V1_0.IQcRilAudioCallback cb2 = new vendor.qti.qcril.am.V1_0.IQcRilAudioCallback.Stub() {
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
