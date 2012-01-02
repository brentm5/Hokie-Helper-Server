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
public class DiningHoursUpdateServlet extends HttpServlet {
	
	//private static final double databaseVersion_ = 1.00;
	private DatastoreService datastore_ = DatastoreServiceFactory.getDatastoreService();
	private static DatabaseVersion databaseVersion_;
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		if(req.getParameter("q") != null && req.getParameter("q").equals("add")) {
			try {
				String name = "dining_hours";
				String storage = "store";
				
				Key InfoKey = KeyFactory.createKey("Caches", "Database");
				Entity Info = new Entity("Caches", InfoKey);
				Info.setProperty("name", name);
				Info.setProperty("value", storage);
				datastore_.put(Info);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
        	Query q = new Query("Caches");
        	q.addFilter("name", Query.FilterOperator.EQUAL, "dining_hours");
        	PreparedQuery pq = datastore_.prepare(q);
        	if(pq.asIterator().hasNext()) {
        		Entity result = pq.asIterator().next();
        		resp.getWriter().print(result.getProperty("value"));
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}