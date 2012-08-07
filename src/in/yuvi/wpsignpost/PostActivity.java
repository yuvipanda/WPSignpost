package in.yuvi.wpsignpost;

import java.util.concurrent.ExecutionException;

import in.yuvi.wpsignpost.api.Post;
import in.yuvi.wpsignpost.api.SignpostAPI;
import android.net.Uri;
import android.os.AsyncTask;
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
	private ShareActionProvider shareProvider;
	private SignpostApp app;
	
	private class PostWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}
	}

	private class FetchPostTask extends AsyncTask<String, Object, Post> {

		@Override
		protected Post doInBackground(String... params) {
			String permalink = params[0];
			Post p = null;
			try {
				p = app.getPost(permalink);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return p;
		}

		@Override
		protected void onPostExecute(Post result) {
			super.onPostExecute(result);
			displayPost(result);
		}	
	}
	
	private void displayPost(Post p) {
		setupShareFunction(p);
		String prefix = String.format(getString(R.string.post_prefix),
				p.permalink, p.title);
		String content = prefix + p.content;
		webview.loadDataWithBaseURL("http://en.wikipedia.org", content,
				"text/html", "utf-8", null);
	}
	
	private void setupShareFunction(Post post) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, post.permalink);
		shareIntent.putExtra(Intent.EXTRA_TITLE, post.title);

		shareProvider.setShareIntent(shareIntent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_post, menu);
		shareProvider = (ShareActionProvider) menu.findItem(
				R.id.menu_share).getActionProvider();
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
        app = ((SignpostApp)getApplicationContext());

		webview = new WebView(this);
		webview.setWebViewClient(new PostWebViewClient());
		webview.getSettings().setBuiltInZoomControls(true);
		webview.getSettings().setDisplayZoomControls(false);
		setContentView(webview);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		String permalink = (String) intent.getExtras().get(Intent.EXTRA_TEXT);
		showPostForPermalink(permalink);
		
	}

	private void showPostForPermalink(String permalink) {
		FetchPostTask task = new FetchPostTask();
		task.execute(permalink);
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
