package me.phh.treble.overlaypicker;

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

	private String platform = SystemProperties.get("ro.board.platform");
	private String vendorFp = SystemProperties.get("ro.vendor.build.fingerprint");
	private String productBoard = SystemProperties.get("ro.product.board");

	private void setOverlayEnabled(String o, boolean enabled) {
		try {
			om.setEnabled(o, true, 0);
		} catch(RemoteException e) {
			Log.d(TAG, "Failed to set overlay", e);
		}
	}
	private void handleHtc(Context ctxt) {
		//HTC U11+
		if(vendorFp == null) return;

		if(vendorFp.contains("htc_ocm"))
			setOverlayEnabled("me.phh.treble.overlay.navbar", true);
	}

	private void handleNightmode(Context ctxt) {
		if("msm8998".equals(platform)) {
			Log.d("OverlayPicker", "Enabling nightmode");
			setOverlayEnabled("me.phh.treble.overlay.nightmode", true);
		}
	}

	private void handleEssentialPh1(Context ctxt) {
		if("Mata".equals(productBoard))
			setOverlayEnabled("me.phh.treble.overlay.essential_ph1", true);
	}

	private void enableLte(Context ctxt) {
		//TODO: List here all non-LTE platforms
		if(!"mt6580".equals(platform))
			setOverlayEnabled("me.phh.treble.overlay.telephony.lte", true);
	}

	@Override
	public void onReceive(Context ctxt, Intent intent) {
		om = IOverlayManager.Stub.asInterface(
				ServiceManager.getService(Context.OVERLAY_SERVICE));

		handleHtc(ctxt);
		handleNightmode(ctxt);
		handleEssentialPh1(ctxt);
		enableLte(ctxt);
	}
}
