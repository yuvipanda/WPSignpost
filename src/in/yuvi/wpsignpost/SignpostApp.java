package in.yuvi.wpsignpost;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.*;

import in.yuvi.wpsignpost.api.*;
import android.app.*;

public class SignpostApp extends Application {
	public SignpostCache cache;
	
	@Override
	public void onCreate() {
		super.onCreate();
		cache = new SignpostCache();
	}

}
