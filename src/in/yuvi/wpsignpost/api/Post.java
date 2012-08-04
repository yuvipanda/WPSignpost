package in.yuvi.wpsignpost.api;

import org.json.simple.JSONObject;

public class Post {
	public String title;
	public String content;
	public String permalink;
	public String author_name;
	public String author_link;
	public Issue issue;
	
	public static Post fromJSON(JSONObject postData) {
		Post p = new Post();
		p.title = (String)postData.get("title");
		p.content = (String)postData.get("content");
		p.permalink = (String)postData.get("permalink");
		p.author_name = (String)postData.get("author_name");
		p.author_link = (String)postData.get("author_link");

		return p;
	}


}
