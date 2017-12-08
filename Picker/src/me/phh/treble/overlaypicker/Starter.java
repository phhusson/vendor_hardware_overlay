import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.om.IOverlayManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class Starter extends BroadcastReceiver {
	private final static String TAG = "OverlayPicker";
	private IOverlayManager om;
	private void setOverlayEnabled(String s, boolean enabled) {
		try {
			om.setEnabled("me.phh.treble.overlay.navbar", true, 0);
		} catch(RemoteException e) {
			Log.d(TAG, "Failed to set overlay", e);
		}
	}
	private void handleHtc(Context ctxt) {
		//HTC U11+
		String fp = SystemProperties.get("ro.vendor.build.fingerprint");
		if(fp == null) return;

		if(fp.contains("ocmdugl"))
			setOverlayEnabled("me.phh.treble.overlay.navbar", true);
	}

	private void handleMsm8937(Context ctxt) {
		Log.d("OverlayPicker", "Testing msm8937");
		if("msm8937".equals(SystemProperties.get("ro.board.platform"))) {
			Log.d("OverlayPicker", "Enabling nightmode");
			setOverlayEnabled("me.phh.treble.overlay.nightmode", true);
		}
	}

	@Override
	public void onReceive(Context ctxt, Intent intent) {
		om = IOverlayManager.Stub.asInterface(
				ServiceManager.getService(Context.OVERLAY_SERVICE));

		handleHtc(ctxt);
		handleMsm8937(ctxt);
	}
}
