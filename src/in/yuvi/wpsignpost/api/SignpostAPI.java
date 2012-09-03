package in.yuvi.wpsignpost.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.http.client.HttpClient;
import org.json.simple.*;
import org.json.simple.parser.*;

import android.net.http.AndroidHttpClient;
import android.util.Log;

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
	
	public ArrayList<Issue> getIssues(int offset) throws Exception {
		String dataString = Http.get(host + "/issues/" + offset).use(client).asString();
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
	
	public Issue getIssue(String permalink) throws Exception {
		String dataString = Http.get(host + "/issue/permalink/" + permalink).use(client).asString();
		return makeIssue(json.parse(dataString));
	}
	
	public Post getPost(String permalink) throws Exception {
		String dataString = Http.get(host + "/post/permalink/" + permalink).use(client).asString();
		return Post.fromJSON((JSONObject) json.parse(dataString));
	}
	
	public void registerDevice(String regID) throws Exception {
	    Http.post(host + "/device/register").use(client).data("regID", regID).asString();
	}
	public void deregisterDevice(String regID) throws Exception {
	    Http.post(host + "/device/deregister").use(client).data("regID", regID).asString();
	}
		
}