package in.yuvi.wpsignpost.api;
import java.io.*;
import org.json.simple.JSONObject;

public class Post implements Serializable{
	public String title;
	public String content;
	public String permalink;
	public String author_name;
	public String author_link;
	public String image_url;
	public long id;
	public Issue issue;
	
	public static Post fromJSON(JSONObject postData) {
		Post p = new Post();
		p.id = (Long)postData.get("id");
		p.title = (String)postData.get("title");
		p.content = (String)postData.get("content");
		p.permalink = (String)postData.get("permalink");
		p.author_name = (String)postData.get("author_name");
		p.author_link = (String)postData.get("author_link");
		p.image_url = (String)postData.get("image_url");

		return p;
	}

	public String fetchContent(SignpostAPI api) throws Exception {
		if(this.content == null) {
			Post p = api.getPost(this.id);
			this.content = p.content;
		}
		return this.content;
	}

}
