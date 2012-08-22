package in.yuvi.wpsignpost.api;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.FutureTask;

import org.json.simple.JSONObject;

public class Post {
	public String title;
	public String content;
	public String permalink;
	public String author_name;
	public String author_link;
	public String image_url;
	public long id;
	public Date published;
	
	public static Post fromJSON(JSONObject postData) {
		Post p = new Post();
		p.id = (Long)postData.get("id");
		p.title = (String)postData.get("title");
		p.content = (String)postData.get("content");
		p.permalink = (String)postData.get("permalink");
		p.author_name = (String)postData.get("author_name");
		p.author_link = (String)postData.get("author_link");
		// Paying the price for a stupid hack
		p.image_url = (String)postData.get("image_url");
		if(p.image_url != null && !p.image_url.startsWith("http")) {
			p.image_url = "http:" + p.image_url;
		}
		p.published = Post.parsePublishedDate(p.permalink);
		
		return p;
	}
	
	public static Date parsePublishedDate(String permalink) {
		SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
		String dateFragment = permalink.split("/")[3];
		try {
			return dateParser.parse(dateFragment);
		} catch (ParseException e) {
			// This should never happen, and if it does, there are way bigger issues than 
			// a date not being formatted correctly. But, what do *I* know - Java obviously
			// knows something about the workings of this particular system that I do not
			return null;
		}
	}
}
