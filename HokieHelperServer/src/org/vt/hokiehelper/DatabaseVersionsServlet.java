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

public class DatabaseVersionsServlet extends HttpServlet {
	
	private DatastoreService datastore_ = DatastoreServiceFactory.getDatastoreService();

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // We have one entity group per Guestbook with all Greetings residing
        // in the same entity group as the Guestbook to which they belong.
        // This lets us run an ancestor query to retrieve all Greetings for a
        // given Guestbook. However, the write rate to each Guestbook should be
        // limited to ~1/second.
		resp.setContentType("text/plain");
		//resp.getWriter().println("start");
		if(req.getParameter("q") != null && req.getParameter("q").equals("updateversion")){
			try {
				String name = req.getParameter("name");
				double version = Double.parseDouble(req.getParameter("ver").toString());
				Query q = new Query("Version");
				q.addFilter("name", Query.FilterOperator.EQUAL, name);
				PreparedQuery pq = datastore_.prepare(q);
				boolean found = false;
				for (Entity result : pq.asIterable()) {
					  String foundName = (String) result.getProperty("name");
					  Double foundVersion = (Double) result.getProperty("version");
						if(foundName.equals(name)){
							result.setProperty("version", version);
							datastore_.put(result);
							found = true;
							resp.getWriter().println("Success");
							break;
						}
				}
				if(!found){
					Key versionKey = KeyFactory.createKey("Version", "Database");
					Entity versionEnt = new Entity("Version", versionKey);
					versionEnt.setProperty("name", name);
					versionEnt.setProperty("version", version);
					datastore_.put(versionEnt);
					//resp.getWriter().println("Not Found Added Key");
				}
				q = new Query("Version");
				pq = datastore_.prepare(q);
				for (Entity result : pq.asIterable()) {
					  String foundName = (String) result.getProperty("name");
					  Double foundVersion = (Double) result.getProperty("version");
					  resp.getWriter().println(foundName + " - " + foundVersion);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Didnt work" + e.toString());
			}
		}

    }
}
