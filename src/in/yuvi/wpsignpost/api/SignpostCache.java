package in.yuvi.wpsignpost.api;

import java.io.IOException;
import java.net.*;
import java.util.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

public class SignpostCache {
	
	private SignpostAPI api;
	
	private class ImagesCache extends LruCache<String, Bitmap> {

		public ImagesCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected Bitmap create(String urlString) {
			try {
				URL url;
				try {
					url = new URL(urlString);
				} catch (MalformedURLException e) {
					// Really, nothing we can do here, is there?
					return null;
				}
				
				HttpURLConnection conn;
				try {
					conn = (HttpURLConnection) url.openConnection();
				} catch (IOException e) {
					// No connection, I suppose? Fall back!
					e.printStackTrace();
					return null;
				}
				try {
					return BitmapFactory.decodeStream(conn.getInputStream());
				} catch (IOException e) {
					// I suppose this is both a failed connection or a malformed image
					// Again, nothing we can do, really.
					e.printStackTrace();
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	private class PostsCache extends LruCache<String, Post> {

		public PostsCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected Post create(String permalink) {
			try {
				return api.getPost(permalink);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	private class IssuesCache extends LruCache<String, Issue> {

		public IssuesCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected Issue create(String permalink) {
			try {
				return api.getIssue(permalink);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	private PostsCache posts;
	private IssuesCache issues;
	private ImagesCache images;
	
	public SignpostCache(SignpostAPI api) {
	    this.api = api;
		posts = new PostsCache(256);
		issues = new IssuesCache(256);
		images = new ImagesCache(256);
	}
	
	public Bitmap getImage(String url) throws Exception {
		return images.get(url);
	}
	
	public Issue getLatestIssue() throws Exception {
		Issue issue = api.getLatestIssue();
		return issues.get(issue.permalink);
	}
	
	public Issue getIssue(String permalink) throws Exception {
		return issues.get(permalink);
	}
	
	public Post getPost(String permalink) throws Exception {
		return posts.get(permalink);
	}
	
	public ArrayList<Issue> getIssuesList(int offset) throws Exception {
		return api.getIssues(offset);
	}
}
