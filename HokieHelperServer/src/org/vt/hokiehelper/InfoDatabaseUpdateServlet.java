package org.vt.hokiehelper;

import java.io.IOException;
import javax.servlet.http.*;

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
public class InfoDatabaseUpdateServlet extends HttpServlet {
	
	//private static final double databaseVersion_ = 1.00;
	private DatastoreService datastore_ = DatastoreServiceFactory.getDatastoreService();
	private static DatabaseVersion databaseVersion_;
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		databaseVersion_ = new DatabaseVersion("info");
		resp.setContentType("text/html");
		if(req.getParameter("q") != null && req.getParameter("q").equals("version")) {
			JSONObject obj = new JSONObject();
			obj.put("version", databaseVersion_);
			resp.getWriter().println(obj);
		}else if(req.getParameter("q") != null && req.getParameter("q").equals("database")) {
			try {
	        	Query q = new Query("Info");
	        	PreparedQuery pq = datastore_.prepare(q);
	        	resp.setContentType("text/plain");
	        	JSONArray locations = new JSONArray();
	        	int id = 0;
	        	for (Entity result : pq.asIterable()) {
	        			JSONObject obj = new JSONObject();
	        			obj.put("_id", result.getProperty("order"));
	        			obj.put("name", result.getProperty("name"));
	        			obj.put("desc", result.getProperty("desc"));
	        			obj.put("type", result.getProperty("type"));
	        			obj.put("payload", result.getProperty("payload"));
	        			locations.add(obj);
	        			id++;
	        		}
	        	resp.getWriter().println(locations);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(req.getParameter("q") != null && req.getParameter("q").equals("add")) {
			try {
				String name = req.getParameter("name");
				String desc = req.getParameter("desc");
				String type = req.getParameter("type");
				String payload = req.getParameter("payload");
				Key InfoKey = KeyFactory.createKey("Info", "Database");
				Entity Info = new Entity("Info", InfoKey);
				Info.setProperty("name", name);
				Info.setProperty("desc", desc);
				Info.setProperty("type", type);
				Info.setProperty("order", 0);
				Info.setProperty("payload", payload);
				datastore_.put(Info);
				resp.sendRedirect("/addInfo.html?action=addlocation&status=success");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				resp.sendRedirect("/addInfo.html?action=addlocation&status=failure");
			}

		}else { 
			resp.getWriter().println("hmmmm?");
	
		}

	}
}