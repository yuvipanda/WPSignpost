package in.yuvi.signpost;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.*;

import in.yuvi.signpost.api.*;
import android.app.*;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

public class SignpostApp extends Application {
	public SignpostCache cache;
	public SignpostAPI api;
	
	@Override
	public void onCreate() {
		super.onCreate();
		api = new SignpostAPI("http://signpost.yuvi.in");
		cache = new SignpostCache(api);

        try {
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
		  GCMRegistrar.register(this, "617551494731");
		} else {
		  Log.v("SignpostApp", "Already registered");
		}
        } catch(UnsupportedOperationException e) {
            // No GCM support!
            Log.d("SignpostApp", "Enjoy your Google free life, brave sir!");
        }

		
	}

}
