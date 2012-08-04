package in.yuvi.wpsignpost;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import in.yuvi.wpsignpost.api.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;


public class PostsActivity extends Activity {

	SignpostAPI api;

	private Issue issue;
	
	private class FetchIssuesTask extends AsyncTask<Object, Object, Issue> {

		@Override
		protected Issue doInBackground(Object... params) {
			   try {
				   issue = api.getLatestIssue();
				   issue.fetchPosts(api);
				} catch (Exception e) {
					e.printStackTrace();
				}
			   return issue;
		}

		@Override
		protected void onPostExecute(Issue result) {
			super.onPostExecute(result);
		}
				
	}
	
	private class PostsAdaptor extends BaseAdapter {
		private Context context;
		public PostsAdaptor(Context c) {
			this.context = c;
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Post p = issue.posts.get(position);
			TextView tv;
			if(convertView == null) {
				tv = new TextView(context);
				//tv.setLayoutParams(new GridView.LayoutParams(85, 85));
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
        api = ((SignpostApp)getApplicationContext()).signpostAPI;
        FetchIssuesTask t = new FetchIssuesTask();
        t.execute();  
        try {
			t.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        GridView grid = (GridView) findViewById(R.id.articles_grid);
        grid.setAdapter(new PostsAdaptor(this));
        grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Post p = (Post)parent.getItemAtPosition(position);
				Intent i = new Intent(parent.getContext(), PostActivity.class);
				i.putExtra("post", p);
				startActivity(i);
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
