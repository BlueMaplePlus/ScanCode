package com.lenaeon.scancode.zxing.decode;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;

import com.lenaeon.scancode.R;
import com.lenaeon.scancode.zxing.ScanManager;

public class PhotoScanHandler extends Handler{
	public final static int PHOTODECODEERROR=0;
	public final static int PHOTODECODESUCCESS=1;
	ScanManager scanManager;
	public PhotoScanHandler(ScanManager scanManager) {
		this.scanManager=scanManager;
	}
	@Override
	public void handleMessage(Message message) {
		
		switch (message.what) {
		case PHOTODECODEERROR:
			scanManager.handleDecodeError((Exception)message.obj);
			break;
		case PHOTODECODESUCCESS:
			Bundle bundle = message.getData();
            bundle.putInt("type", R.id.decode_photo);
			scanManager.handleDecode((Result) message.obj, bundle);
			break;
		default:
			break;
		}
	}
	
}
