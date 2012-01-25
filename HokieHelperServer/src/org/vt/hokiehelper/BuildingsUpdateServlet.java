package org.vt.hokiehelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/**
 * BuildingsUpdateServlet goes to the vt buildings page and goes one by one to each
 * buildings info page and gets the coordinates listed for the building. This should
 * only be run like once a week, just in case VT adds a new building, to update the 
 * database for VT buildings. 
 * 
 * NOTE: Not all building pages have coordinates on them, for example the Center for
 * the Arts page currently has no coordinates.
 * @author andrew
 *
 */

@SuppressWarnings("serial")
public class BuildingsUpdateServlet extends HttpServlet {
	private DatastoreService datastore_ = DatastoreServiceFactory.getDatastoreService();
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		URL hoursUrl = new URL("http://www.vt.edu/about/buildings/");
		URLConnection conn = hoursUrl.openConnection();
		conn.setDoOutput(true);
		String host = "http://www.vt.edu";

		//Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		StringBuffer buff = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			buff.append(line+"\n");
		}

		String responseText = buff.toString();
		Document doc = Jsoup.parse(responseText);
		rd.close();

		HashMap<String, HashMap<String, String>> buildings = new HashMap<String, HashMap<String, String>>();

		Pattern abbrPattern = 
				Pattern.compile(".+Abbreviation: ([A-Z ]+)L.+"); // The capital L is there to prevent Latitude's L being included
		Pattern coordsPattern = 
				Pattern.compile(".+Latitude: ([-]?[0-9]+\\.[0-9]+) \\| Longitude: ([-]?[0-9]+\\.[0-9]+)");

		int total = 0;
		int coordsFound = 0;
		int abbrFound = 0;
		resp.getWriter().println("Deleteing All the Location Entries");
		deleteAll();
		
		for(Element building : doc.getElementsByClass("vt_list_DateAfter")) {
			total++;
			String link = building.getElementsByTag("a").first().attr("href");
			URL buildingURL = new URL(host + link);
			String buildingName = building.text();
			URLConnection buildingConn = buildingURL.openConnection();
			buildingConn.setDoOutput(true);

			rd = new BufferedReader(new InputStreamReader(buildingConn.getInputStream()));
			buff = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				buff.append(line+"\n");
			}

			responseText = buff.toString();
			Document buildingDoc = Jsoup.parse(responseText);
			rd.close();

			Element body = buildingDoc.getElementById("vt_body_col");
			String coordsLine = buildingDoc.select("#vt_body_col > p").last().text();
			Matcher abbrMatcher = abbrPattern.matcher(coordsLine);
			Matcher coordsMatcher = coordsPattern.matcher(coordsLine);
			boolean coordsMatch = coordsMatcher.find();
			boolean abbrMatch = abbrMatcher.find();
			if(coordsMatch || abbrMatch) { // Only add if the coords or abbr are on the page
				HashMap<String, String> info = new  HashMap<String, String>();
				if(abbrMatch) {
					info.put("abbrev", abbrMatcher.group(1).trim());
					abbrFound++;
				}
				else {
					resp.getWriter().println("No abbreviation for " + buildingName);
				}
				if(coordsMatch) {
					info.put("lat",coordsMatcher.group(1));
					info.put("lon",coordsMatcher.group(2));
					coordsFound++;
				}
				else {
					resp.getWriter().println("No coordinates for " + buildingName);
				}
				buildings.put(buildingName, info);
				
			}
			else {
				resp.getWriter().println(buildingName + " didn't match either regex");
			}
		}

		resp.getWriter().println("Abbreviations found: " + abbrFound + "/" + total);
		resp.getWriter().println("Coordinates found: " + coordsFound + "/" + total);
		resp.getWriter().println(buildings.toString());
		
		for (String building : buildings.keySet()) {
			try {
				HashMap<String, String> info = buildings.get(building);
				if(info.containsKey("lat") && info.containsKey("lon")){
					Key locationKey = KeyFactory.createKey("Location", "Database");
					Entity location = new Entity("Location", locationKey);
					location.setProperty("name", building);
					location.setProperty("lat", info.get("lat").toString());
					location.setProperty("long", info.get("lon").toString());
					location.setProperty("url", "");
					String abbrev = info.containsKey("abbrev") ? info.get("abbrev") : "";
					location.setProperty("keywords", building +", "+ abbrev);
					datastore_.put(location);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	private boolean deleteAll(){
		try {
        	Query q = new Query("Location");
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
