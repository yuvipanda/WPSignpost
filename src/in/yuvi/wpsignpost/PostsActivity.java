package in.yuvi.wpsignpost;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.http.client.methods.HttpUriRequest;

import in.yuvi.wpsignpost.api.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.*;
import com.actionbarsherlock.app.*;
import android.support.v4.widget.*;
import android.support.v4.view.*;

public class PostsActivity extends SherlockActivity {

	private String currentPermalink = null;
	private SignpostApp app;
	private GridView grid;
	
	private class FetchIssuesTask extends AsyncTask<String, Object, Issue> {
		@Override
		protected Issue doInBackground(String... params) {
			Issue issue = null;
			try {
				if(params.length > 0 && params[0] != null) {
					String permalink = params[0];
					issue = app.cache.getIssue(permalink);
				} else {
					issue = app.cache.getLatestIssue();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return issue;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			View loadingView = findViewById(R.id.issueLoadingAnimation);
			loadingView.setVisibility(View.VISIBLE);
			grid.setVisibility(View.GONE);
			View errorView = findViewById(R.id.issueError);
			errorView.setVisibility(View.GONE);
		}

		@Override
		protected void onPostExecute(Issue result) {
			super.onPostExecute(result);
			if(result != null) {
				showIssue(result);
			} else {
				showError();
			}
		}
				
	}
	
	private void showError() {
		grid.setVisibility(View.GONE);
		View loadingView = findViewById(R.id.issueLoadingAnimation);
		loadingView.setVisibility(View.GONE);
		View errorView = findViewById(R.id.issueError);
		errorView.setVisibility(View.VISIBLE);
		
		Button retryButton = (Button)findViewById(R.id.issueRetryButton);
		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FetchIssuesTask t = new FetchIssuesTask();
				t.execute(currentPermalink);  
			}
		});
	}
	
	private void showIssue(Issue issue) {
		grid.setAdapter(new PostsAdaptor(this, issue));
		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Post p = (Post) parent.getItemAtPosition(position);
				Intent i = new Intent(parent.getContext(),
						PostActivity.class);
				i.putExtra(Intent.EXTRA_TEXT, p.permalink);
				startActivity(i);
			}
		});
		
		getActionBar().setSubtitle(issue.toString());
		
		currentPermalink = issue.permalink;
		
		View loadingView = findViewById(R.id.issueLoadingAnimation);
		loadingView.setVisibility(View.GONE);
		grid.setVisibility(View.VISIBLE);
	}
	
	private class PostsAdaptor extends BaseAdapter {
		private Context context;
		private Issue issue;
		public PostsAdaptor(Context c, Issue issue) {
			this.context = c;
			this.issue = issue;
		}
		
		@Override
		public int getCount() {
			return issue.posts.size();
		}

		@Override
		public Object getItem(int position) {
			return issue.posts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		private class FetchImageTask extends AsyncTask<String, Object, Bitmap> {

			private ImageView imageView;
			private View issueView;
			private String url;
			public FetchImageTask(ImageView imageView, View issueView, String url) {
				this.imageView = imageView;
				this.url = url;
				this.issueView = issueView;
			}
			
			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				if(result != null) {
					if (imageView.getTag() != null && imageView.getTag().equals(url)) {
						imageView.setImageBitmap(result);
						imageView.setVisibility(View.VISIBLE);
					}
				} 
				issueView.findViewById(R.id.imageLoadingAnimation).setVisibility(View.GONE);
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				imageView.setBackgroundResource(0);
				issueView.findViewById(R.id.imageLoadingAnimation).setVisibility(View.VISIBLE);
			}

			@Override
			protected Bitmap doInBackground(String... params) {
				try {
					return app.cache.getImage(this.url);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
			
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Post p = issue.posts.get(position);
			FrameLayout issueView;
			ImageView image;
			TextView title, category;
			if(convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				issueView = new FrameLayout(context);
				layoutInflater.inflate(R.layout.issue_post_display, issueView);
			} else {
				issueView = (FrameLayout)convertView;
			}
			
			image = (ImageView)issueView.findViewById(R.id.post_display_image);
			title = (TextView)issueView.findViewById(R.id.post_display_title);
			category = (TextView)issueView.findViewById(R.id.post_display_category);
			image.setVisibility(View.GONE);
			issueView.findViewById(R.id.imageLoadingAnimation).setVisibility(View.VISIBLE);
			image.setTag(p.image_url);
			
			title.setText(p.title);
			category.setText(p.category);
			if(p.image_url != null) {
				FetchImageTask imageFetch = new FetchImageTask(image, issueView, p.image_url);
				imageFetch.execute(p.image_url);
			} else {
					issueView.findViewById(R.id.imageLoadingAnimation).setVisibility(View.GONE);
					image.setImageResource(R.drawable.image_placeholder);
					image.setVisibility(View.VISIBLE);
			}
			
			return issueView;
		}
		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = ((SignpostApp)getApplicationContext());
        grid = (GridView) findViewById(R.id.articles_grid);
        
        String permalink = null;

        if(savedInstanceState != null) {
        	permalink = savedInstanceState.getString("permalink");
        }
        
        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)) {
        	permalink = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        
        FetchIssuesTask t = new FetchIssuesTask();
        t.execute(permalink);  
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_archives:
				Intent issueListIntent = new Intent(this, IssueListActivity.class);
				startActivity(issueListIntent);
				return true;
			case R.id.menu_about:
				Intent aboutIntent = new Intent(this, AboutActivity.class);
				startActivity(aboutIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(currentPermalink != null) {
			outState.putString("permalink", currentPermalink);
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
