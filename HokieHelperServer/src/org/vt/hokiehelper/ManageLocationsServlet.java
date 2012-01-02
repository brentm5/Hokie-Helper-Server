package org.vt.hokiehelper;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class ManageLocationsServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // We have one entity group per Guestbook with all Greetings residing
        // in the same entity group as the Guestbook to which they belong.
        // This lets us run an ancestor query to retrieve all Greetings for a
        // given Guestbook. However, the write rate to each Guestbook should be
        // limited to ~1/second.
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		if(req.getParameter("q") != null && req.getParameter("q").equals("deleteall")){
			try {
	        	Query q = new Query("Location");
	     
	        	PreparedQuery pq = datastore.prepare(q);
	        	for (Entity result : pq.asIterable()) {
	        		datastore.delete(result.getKey());
	        	}
	        	resp.sendRedirect("/addLocation.html?action=deleteall&status=success");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				resp.sendRedirect("/addLocation.html?action=deleteall&status=failure");
			}
			
		}else if(req.getParameter("q") != null && req.getParameter("q").equals("addlocation")){
			try {
				String name = req.getParameter("name");
				double lat = Double.parseDouble(req.getParameter("lat").toString());
				double lon = Double.parseDouble(req.getParameter("long").toString());
				String url = req.getParameter("url");
				String keywords = req.getParameter("keywords");
				Key locationKey = KeyFactory.createKey("Location", "Database");
				Entity location = new Entity("Location", locationKey);
				location.setProperty("name", name);
				location.setProperty("lat", lat);
				location.setProperty("long", lon);
				location.setProperty("url", url);
				location.setProperty("keywords", keywords);
				datastore.put(location);
				resp.sendRedirect("/addLocation.html?action=addlocation&status=success");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				resp.sendRedirect("/addLocation.html?action=addlocation&status=failure");
			}
		}else {
			resp.sendRedirect("/addLocation.html");
		}


    }
}
