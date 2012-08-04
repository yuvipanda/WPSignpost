package in.yuvi.wpsignpost;

import java.util.ArrayList;

import in.yuvi.wpsignpost.api.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;


public class MainActivity extends Activity {

	SignpostAPI api;
	
	private class FetchIssuesTask extends AsyncTask<Object, Object, ArrayList<Issue>> {

		@Override
		protected ArrayList<Issue> doInBackground(Object... params) {
			   try {
					for(Issue i: api.getAllIssues()) {
						Log.d("API", Long.toString(i.id));
						for(Post p: i.fetchPosts()) {
							Log.d("API", p.title);
						}
					}
				} catch (Exception e) {
					// Fuck Java
					e.printStackTrace();
				}
			   return null;
		}

		
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = new SignpostAPI("http://yuvi.in/signpost");
        FetchIssuesTask t = new FetchIssuesTask();
        t.execute();     
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
