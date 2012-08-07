package in.yuvi.wpsignpost.api;

import java.util.*;
import android.support.v4.util.LruCache;

public class SignpostCache {
	
	private SignpostAPI api;
	
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
	
	public SignpostCache() {
		api = new SignpostAPI("http://yuvi.in/signpost");
		posts = new PostsCache(256);
		issues = new IssuesCache(256);
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
