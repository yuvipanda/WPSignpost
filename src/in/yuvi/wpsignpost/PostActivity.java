package in.yuvi.wpsignpost;

import in.yuvi.wpsignpost.api.Post;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.*;
import android.widget.ShareActionProvider;
import android.support.v4.app.NavUtils;

public class PostActivity extends Activity {
	private WebView webview;
	private Post post;
	
	private class PostWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
	     getMenuInflater().inflate(R.menu.activity_post, menu);
	        ShareActionProvider prov = (ShareActionProvider) menu.findItem(R.id.menu_share).getActionProvider();
	        Intent shareIntent = new Intent(Intent.ACTION_SEND);
	        shareIntent.setAction(Intent.ACTION_SEND);
	        shareIntent.setType("text/plain");
	        shareIntent.putExtra(Intent.EXTRA_TEXT, post.permalink);
	        shareIntent.putExtra(Intent.EXTRA_TITLE, post.title);
	        
	        prov.setShareIntent(shareIntent);

	        return true;
	    }
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        webview.setWebViewClient(new PostWebViewClient());
        setContentView(webview);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        Intent intent = getIntent();
        post = (Post)intent.getExtras().get("post");
        webview.loadDataWithBaseURL("http://en.wikipedia.org", post.content, "text/html", "utf-8", null);
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
