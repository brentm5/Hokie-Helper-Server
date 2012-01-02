package org.vt.hokiehelper;



import java.util.logging.Logger;
import java.io.IOException;
import javax.servlet.http.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mortbay.log.Log;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class MapsDatabaseUpdateServlet extends HttpServlet {
	
	//private static final double databaseVersion_ = 1.00;
	private static final Logger log = Logger.getLogger(MapsDatabaseUpdateServlet.class.getName());
	private DatastoreService datastore_ = DatastoreServiceFactory.getDatastoreService();
	private static DatabaseVersion databaseVersion_;
	
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		databaseVersion_ = new DatabaseVersion("map");
		if(req.getParameter("q") != null && req.getParameter("q").equals("version")) {
			JSONObject obj = new JSONObject();
			obj.put("version", databaseVersion_);
			//Log.info("Sent Maps Database with version: " + databaseVersion_);
			resp.getWriter().println(obj);
		}else if(req.getParameter("q") != null && req.getParameter("q").equals("database")) {
			try {
	        	Query q = new Query("Location");
	        	PreparedQuery pq = datastore_.prepare(q);
	        	resp.setContentType("text/plain");
	        	JSONArray locations = new JSONArray();
	        	int id = 0;
	        	for (Entity result : pq.asIterable()) {
	        			JSONObject obj = new JSONObject();
	        			obj.put("_id", id);
	        			obj.put("name", result.getProperty("name"));
	        			obj.put("lat", result.getProperty("lat"));
	        			obj.put("long", result.getProperty("long"));
	        			obj.put("url", result.getProperty("url"));
	        			obj.put("keywords", result.getProperty("keywords"));
	        			locations.add(obj);
	        			id++;
	        		}
	        	//log.info("Sent Maps Database with " + locations.size() + " locations.");
	        	resp.getWriter().println(locations);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.severe("An error occured " + e.toString());
			}
	   }else {
			resp.getWriter().println("hmmmm?");
	   }
	}
}
