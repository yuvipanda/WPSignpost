package in.yuvi.wpsignpost.api;

import java.util.ArrayList;

import java.util.Date;

import org.json.simple.JSONObject;
import java.io.*;

public class Issue implements Serializable {
	public Date date;
	public long id;
	
	SignpostAPI api;
	
	public ArrayList<Post> posts;
	
	public static Issue fromJSON(JSONObject issueData) {
		Issue issue = new Issue();
		Double timestamp = (Double)issueData.get("date") * 1000;
		issue.date = new Date(timestamp.longValue());
		issue.id = (Long)issueData.get("id");
		return issue;
	}
	
	public ArrayList<Post> fetchPosts(SignpostAPI api) throws Exception {
		if( posts == null ) {
			posts = api.getPosts(this.id);
		}
		return posts;
	}
}
