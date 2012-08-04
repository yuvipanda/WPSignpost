package in.yuvi.wpsignpost.api;

import java.util.ArrayList;
import java.util.Date;

import org.apache.http.client.HttpClient;
import org.json.simple.*;
import org.json.simple.parser.*;

import android.net.http.AndroidHttpClient;

import de.mastacode.http.Http;

public class SignpostAPI {
	private String host;
	private JSONParser json;
	private HttpClient client;
	
	public SignpostAPI(String host) {
		this.host = host;
		this.json = new JSONParser();
		this.client = AndroidHttpClient.newInstance("Android WP Signpost/0.9");
	}
	
	private Issue makeIssue(Object json) {
		JSONObject issueData = (JSONObject)json;
		Issue issue = Issue.fromJSON(issueData);
		return issue;
	}
	
	public ArrayList<Issue> getAllIssues() throws Exception {
		String dataString = Http.get(host + "/issues").use(client).asString();
		JSONArray issues = (JSONArray)json.parse(dataString);
 		ArrayList<Issue> returnData = new ArrayList<Issue>();
		for(Object issue : issues) {
			returnData.add(makeIssue(issue));
		}
		
		return returnData;
	}
	
	public Issue getLatestIssue() throws Exception {
		String dataString = Http.get(host + "/issue/latest").use(client).asString();
		return makeIssue(json.parse(dataString));
	}
	
	ArrayList<Post> getPosts(long issueId) throws Exception {
		String dataString = Http.get(host + "/posts/" + issueId).use(client).asString();
		
		ArrayList<Post> posts = new ArrayList<Post>();
		JSONArray postsData = (JSONArray)json.parse(dataString);
		for(Object postData : postsData) {
			Post p = Post.fromJSON((JSONObject)postData);
			posts.add(p);
		}
		
		return posts;
	}
	
	public Post getPost(long id) throws Exception {
		String dataString = Http.get(host + "/post/" + id).use(client).asString();
		return Post.fromJSON((JSONObject) json.parse(dataString));
	}
		
}