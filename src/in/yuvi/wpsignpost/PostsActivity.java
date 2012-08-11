package in.yuvi.wpsignpost;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import in.yuvi.wpsignpost.api.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
			}
			return issue;
		}

		@Override
		protected void onPostExecute(Issue result) {
			super.onPostExecute(result);
			showIssue(result);
		}
				
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

		private int getRandomGrey() {
			Random r = new Random();
			int val = r.nextInt(128);
			return Color.rgb(val, val, val);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Post p = issue.posts.get(position);
			TextView tv;
			if(convertView == null) {
				tv = new TextView(context);
				tv.setGravity(Gravity.BOTTOM);
				tv.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.FILL_PARENT, 256));
				tv.setTextSize(14);
				tv.setTypeface(Typeface.SERIF);
				tv.setPadding(8, 8, 8, 8);
				tv.setTextColor(0xFFFFFFFF);
				tv.setBackgroundColor(getRandomGrey());
			} else {
				tv = (TextView) convertView;
			}

			tv.setText(p.title);
			return tv;
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
				Intent i = new Intent(this, IssueListActivity.class);
				startActivity(i);
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
