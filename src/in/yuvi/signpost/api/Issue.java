package in.yuvi.signpost.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.*;

public class Issue {
	public Date date;
	public long id;
	public String permalink;
		
	public ArrayList<Post> posts;
	
	public static Issue fromJSON(JSONObject issueData) {
		Issue issue = new Issue();
		Double timestamp = (Double)issueData.get("date") * 1000;
		issue.date = new Date(timestamp.longValue());
		issue.id = (Long)issueData.get("id");
		issue.permalink = (String)issueData.get("permalink");
		if(issueData.containsKey("posts")) {
			issue.posts = new ArrayList<Post>();
			JSONArray posts = (JSONArray)issueData.get("posts");
			for(Object p : posts) {
				issue.posts.add(Post.fromJSON((JSONObject)p));
			}
		}
		return issue;
	}
	
	public static String makePermalink(Date date) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		return "en.wikipedia.org/wiki/Wikipedia:Wikipedia_Signpost/Archives/" + dateFormatter.format(date);
	}

	@Override
	public String toString() {
		return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(this.date);
	}
}
