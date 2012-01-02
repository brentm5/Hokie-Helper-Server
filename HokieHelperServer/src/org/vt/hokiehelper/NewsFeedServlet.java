package org.vt.hokiehelper;

import it.sauronsoftware.feed4j.FeedIOException;
import it.sauronsoftware.feed4j.FeedParser;
import it.sauronsoftware.feed4j.FeedXMLParseException;
import it.sauronsoftware.feed4j.UnsupportedFeedException;
import it.sauronsoftware.feed4j.bean.Feed;
import it.sauronsoftware.feed4j.bean.FeedHeader;
import it.sauronsoftware.feed4j.bean.FeedItem;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class NewsFeedServlet extends HttpServlet {
	
	private DatastoreService datastore_ = DatastoreServiceFactory.getDatastoreService();
	
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		try {
			URL feedUrl = new URL("http://www.vtnews.vt.edu/articles/index-rss2.xml");
			Feed feed = FeedParser.parse(feedUrl);
			if(deleteAll()){
				int items = feed.getItemCount();
				for (int i = 0; i < items; i++) {
					FeedItem item = feed.getItem(i);
					Key articleKey = KeyFactory.createKey("Article", "Database");
					Entity article = new Entity("Article", articleKey);
					article.setProperty("title", item.getTitle());
					article.setProperty("url", item.getLink().toString());
					article.setProperty("desc", item.getDescriptionAsText());
					datastore_.put(article);
				}
	        	resp.getWriter().println("Success");
			}else {
				throw new Exception("Could not delete all articles");
			}
		} catch (FeedIOException e) {
			e.printStackTrace(resp.getWriter());
			return;
		} catch (FeedXMLParseException e) {
			e.printStackTrace(resp.getWriter());
			return;
		} catch (UnsupportedFeedException e) {
			e.printStackTrace(resp.getWriter());
			return;
		} catch(MalformedURLException e) {
			e.printStackTrace(resp.getWriter());
			return;
		}catch(Exception e) {
			e.printStackTrace(resp.getWriter());
			return;
		}
	}
	
	private boolean deleteAll(){
		try {
        	Query q = new Query("Article");
        	PreparedQuery pq = datastore_.prepare(q);
        	for (Entity result : pq.asIterable()) {
        		datastore_.delete(result.getKey());
        	}
        	return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
}
