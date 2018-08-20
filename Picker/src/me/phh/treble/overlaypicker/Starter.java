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

	private void handleSamsung(Context ctxt) {
		if(vendorFp == null) return;

		if(vendorFp.contains("starlte") ||
		   vendorFp.contains("star2lte") ||
		   vendorFp.contains("starqlte") ||
		   vendorFp.contains("star2qlte")) {
			setOverlayEnabled("me.phh.treble.overlay.samsung.s9.systemui", true);
		}

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
		enableLte(ctxt);
		handleSamsung(ctxt);

		setOverlayEnabled("me.phh.treble.overlay.systemui.falselocks", true);
	}
}
