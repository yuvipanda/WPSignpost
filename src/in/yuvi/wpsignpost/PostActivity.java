package in.yuvi.wpsignpost;

import in.yuvi.wpsignpost.api.Post;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.*;
import android.support.v4.app.NavUtils;

public class PostActivity extends Activity {
	private WebView webview;
	
	private class PostWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        webview.setWebViewClient(new PostWebViewClient());
        setContentView(webview);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        Post p = (Post)intent.getExtras().get("post");
        webview.loadDataWithBaseURL("http://en.wikipedia.org", p.content, "text/html", "utf-8", null);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
