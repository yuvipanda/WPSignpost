package in.yuvi.wpsignpost;

import android.os.Bundle;
import android.app.Activity;
import android.view.*;
import android.widget.*;
import android.support.v4.app.NavUtils;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.*;

public class AboutActivity extends Activity {
	
	// I hate underlines in the links. Let's remove them the Java way (do we have a choice?)
	private class URLSpanNoUnderline extends URLSpan {
		public URLSpanNoUnderline(String url) {
			super(url);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			ds.setUnderlineText(false);
		}
	}

	private void stripUnderlines(TextView textView) {
		Spannable s = (Spannable) textView.getText();
		URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
		for (URLSpan span : spans) {
			int start = s.getSpanStart(span);
			int end = s.getSpanEnd(span);
			s.removeSpan(span);
			span = new URLSpanNoUnderline(span.getURL());
			s.setSpan(span, start, end, 0);
		}
		textView.setText(s);
	}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ((TextView)findViewById(R.id.aboutContributors)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)findViewById(R.id.aboutLicense)).setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines((TextView)findViewById(R.id.aboutContributors));
        stripUnderlines((TextView)findViewById(R.id.aboutLicense));
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	// No place in 'hierarchy', just emulate 'back' button
            	finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
