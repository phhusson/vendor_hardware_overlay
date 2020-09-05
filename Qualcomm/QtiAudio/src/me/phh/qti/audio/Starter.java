package me.phh.qti.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Starter extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctxt, Intent intent) {
		ctxt.startService(new Intent(ctxt, Service.class));
	}
}
