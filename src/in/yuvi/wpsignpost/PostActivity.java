package in.yuvi.wpsignpost;

import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import in.yuvi.wpsignpost.api.Issue;
import in.yuvi.wpsignpost.api.Post;
import in.yuvi.wpsignpost.api.SignpostAPI;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.*;
import android.widget.Button;
import android.support.v4.app.NavUtils;


import com.actionbarsherlock.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.*;
import com.actionbarsherlock.app.*;
import android.support.v4.widget.*;
import android.support.v4.view.*;

public class PostActivity extends SherlockActivity {
	private WebView webview;
	private ShareActionProvider shareProvider;
	private SignpostApp app;
	private String currentPermalink;
	
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
				p = app.cache.getPost(permalink);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			return p;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			View loadingView = findViewById(R.id.postLoadingAnimation);
			loadingView.setVisibility(View.VISIBLE);
			webview.setVisibility(View.GONE);
			View errorView = findViewById(R.id.postError);
			errorView.setVisibility(View.GONE);
		}
		
		@Override
		protected void onPostExecute(Post result) {
			super.onPostExecute(result);
			if(result != null) {
				displayPost(result);
			} else {
				showError();
			}
		}	
	}
	
	private void showError() {
		webview.setVisibility(View.GONE);
		View loadingView = findViewById(R.id.postLoadingAnimation);
		loadingView.setVisibility(View.GONE);
		View errorView = findViewById(R.id.postError);
		errorView.setVisibility(View.VISIBLE);
		
		Button retryButton = (Button)findViewById(R.id.postRetryButton);
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FetchPostTask t = new FetchPostTask();
				t.execute(currentPermalink); 
			}
		});
	}
	
	private void displayPost(Post p) {
		setupShareFunction(p);
		String prefix = "";
		String postfix = "";
		prefix += getString(R.string.post_css);
		prefix += String.format(getString(R.string.post_title_html), "//" + p.permalink, p.title);
		if(p.author_name != null && p.author_name.length() != 0) {
			prefix += String.format(getString(R.string.post_author_html),
					p.author_link, p.author_name);
		}
		String article_title = p.permalink.replace("en.wikipedia.org/wiki/", "");
		postfix += String.format(getString(R.string.post_footer_html), "/w/index.php?title=" + article_title + "&action=history");
		String content = prefix + p.content + postfix;
		webview.loadDataWithBaseURL("http://en.wikipedia.org", content,
				"text/html", "utf-8", null);
		
		View loadingView = findViewById(R.id.postLoadingAnimation);
		loadingView.setVisibility(View.GONE);
		webview.setVisibility(View.VISIBLE);
		
	}
	
	private void setupShareFunction(Post post) {
		if(shareProvider == null) {
			// Recycled view! The sharing intents have already been setup
			// But is sortof mostly a hack. I need to figure out why menu is null
			return;
		}
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, "https://" + post.permalink);
		shareIntent.putExtra(Intent.EXTRA_TITLE, post.title);
		
		shareProvider = (ShareActionProvider) menu.findItem(
				R.id.menu_share).getActionProvider();
		shareProvider.setShareIntent(shareIntent);
	}
	
	private Menu menu;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_post, menu);
		this.menu = menu;
		shareProvider = (ShareActionProvider) menu.findItem(
				R.id.menu_share).getActionProvider();
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
        app = ((SignpostApp)getApplicationContext());
        
        setContentView(R.layout.activity_post);

		webview = (WebView)findViewById(R.id.postWebview);
		webview.setWebViewClient(new PostWebViewClient());
		if(Build.VERSION.SDK_INT >= 11) {
			// No sane way to give people touchZoom without the retarded -/+ controls on pre-honeycomb Android
			webview.getSettings().setBuiltInZoomControls(true);
			webview.getSettings().setDisplayZoomControls(false);
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		String permalink = (String) intent.getExtras().get(Intent.EXTRA_TEXT);
		showPostForPermalink(permalink);
		currentPermalink = permalink;
	}

	private void showPostForPermalink(String permalink) {
		FetchPostTask task = new FetchPostTask();
		task.execute(permalink);
		
		this.setTitle(Post.parseCategory(permalink));
		this.getSupportActionBar().setSubtitle(SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(Post.parsePublishedDate(permalink)));
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Because we might be in an error page, and current post might be null, so not using cache
			String issuePermalink = Issue.makePermalink(Post.parsePublishedDate(currentPermalink));
			Intent intent = new Intent(this, PostsActivity.class);
			intent.putExtra(Intent.EXTRA_TEXT, issuePermalink);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
