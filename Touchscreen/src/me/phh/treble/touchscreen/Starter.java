package me.phh.treble.touchscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.om.IOverlayManager;
import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.hardware.input.TouchCalibration;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class Starter extends BroadcastReceiver {
    private final static String TAG = "TouchScreen";

    private int getRotation(Context ctxt, Intent intent) {
        int rotation = -1;
        String vndFingerprint = SystemProperties.get("ro.vendor.build.fingerprint", "");
        int hwrotation = SystemProperties.getInt("ro.sf.hwrotation", -1);
        if(vndFingerprint.contains("full_x970_t10") && hwrotation == 270) {
            rotation = 271;
        }
        if(intent.hasExtra("rotation")) {
            rotation = intent.getIntExtra("rotation", rotation);
        }
        return rotation;
    }

    @Override
    public void onReceive(Context ctxt, Intent intent) {
        int rotation = getRotation(ctxt, intent);
        if(rotation == -1) return;
        android.util.Log.d("PHH", "Applying rotation " + rotation);
        InputManager im = ctxt.getSystemService(InputManager.class);
        int[] ids = im.getInputDeviceIds();
        for(int id: ids) {
            InputDevice dev = im.getInputDevice(id);
            String descriptor = dev.getDescriptor();
            android.util.Log.d("PHH", "Checking device " + descriptor + ":" + dev.getSources());
            if( (dev.getSources() & InputDevice.SOURCE_TOUCHSCREEN) == InputDevice.SOURCE_TOUCHSCREEN ) {
                InputDevice.MotionRange rangeX = dev.getMotionRange(MotionEvent.AXIS_X);
                InputDevice.MotionRange rangeY = dev.getMotionRange(MotionEvent.AXIS_Y);

                float dX = rangeX.getMax();
                float dY = rangeY.getMax();
                //TouchCalibration cal = new TouchCalibration(
                //		0.0f, dX/dY, 0.0f,
                //		dY/dX, 0.0f, 0.0f);
                TouchCalibration cal;
                if(rotation == 0) {
                    cal = new TouchCalibration(
                            1.0f, 0.0f, 0.0f,
                            0.0f, 1.0f, 0.0f);
                } else if(rotation == 90) {
                    cal = new TouchCalibration(
                            0.0f, dX/dY, 0.0f,
                            dY/dX, 0.0f, 0.0f);
                } else if(rotation == 91) {
                    cal = new TouchCalibration(
                            0.0f, -dX/dY, dX,
                            dY/dX, 0.0f, 0.0f);
                } else if(rotation == 180) {
                    cal = new TouchCalibration(
                            -1.0f, 0.0f, dX,
                            0.0f, -1.0f, dY);
                } else if(rotation == 270) {
                    cal = new TouchCalibration(
                            0.0f, -dX/dY, dX,
                            -dY/dX, 0.0f, dY);
                } else if(rotation == 271) {
                    cal = new TouchCalibration(
                            0.0f, dX/dY, 0.0f,
                            -dY/dX, 0.0f, dY);
                } else {
                    cal = new TouchCalibration(
                            1.0f, 0.0f, 0.0f,
                            0.0f, 1.0f, 0.0f);
                }

                android.util.Log.d("PHH", "Setting touch calibration " + dY/dX  + ":" + dX/dY);
                im.setTouchCalibration(descriptor, 0, cal);
                im.setTouchCalibration(descriptor, 1, cal);
                im.setTouchCalibration(descriptor, 2, cal);
                im.setTouchCalibration(descriptor, 3, cal);
            }
        }
    }
}
