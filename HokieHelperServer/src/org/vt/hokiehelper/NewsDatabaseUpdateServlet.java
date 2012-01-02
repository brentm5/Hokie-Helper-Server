package org.vt.hokiehelper;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class NewsDatabaseUpdateServlet extends HttpServlet {

	private DatastoreService datastore_ = DatastoreServiceFactory.getDatastoreService();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	
		
		Query q = new Query("Article");
    	PreparedQuery pq = datastore_.prepare(q);
    	JSONArray articles = new JSONArray();
    	int id = 0;
    	for (Entity result : pq.asIterable()) {
    			JSONObject obj = new JSONObject();
    			obj.put("_id", id);
    			obj.put("title", result.getProperty("title"));
    			//obj.put("desc", result.getProperty("desc"));
    			obj.put("url", result.getProperty("url"));
    			articles.add(obj);
    			id++;
    		}
    	resp.getWriter().println(articles);		
	}
	
}
