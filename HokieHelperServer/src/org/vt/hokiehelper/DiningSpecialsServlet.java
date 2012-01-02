package org.vt.hokiehelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@SuppressWarnings("serial")
public class DiningSpecialsServlet extends HttpServlet {
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		ArrayList<URL> specialsSites = new ArrayList<URL>();
		ArrayList<String> names = new ArrayList<String>();
		
        // Add URLs to URL ArrayList
        // Owens & Hokie Grill
		specialsSites.add(
				new URL("http://foodpro.studentprograms.vt.edu/FoodPro_2.3/shortmenu.asp?sName=Virginia+Tech+Dining+Services&locationNum=09&locationName=OWENS+%26+HOKIE+GRILL&naFlag=1"));
		names.add("owens");
        // D2
		specialsSites.add(
				new URL("http://foodpro.studentprograms.vt.edu/FoodPro_2.3/shortmenu.asp?sName=Virginia+Tech+Dining+Services&locationNum=15&locationName=D2+%26+DXPRESS&naFlag=1"));
		names.add("d2");
        // Deet's
		specialsSites.add(
				new URL("http://foodpro.studentprograms.vt.edu/FoodPro_2.3/shortmenu.asp?sName=Virginia+Tech+Dining+Services&locationNum=07&locationName=DEET%27S+PLACE+&naFlag=1"));
		names.add("deets");
		// Shultz and Express
		specialsSites.add(
				new URL("http://foodpro.studentprograms.vt.edu/FoodPro_2.3/shortmenu.asp?sName=Virginia+Tech+Dining+Services&locationNum=14&locationName=SHULTZ+%26+EXPRESS&naFlag=1"));
		names.add("shultz");
		// Vet Med
		specialsSites.add(
				new URL("http://foodpro.studentprograms.vt.edu/FoodPro_2.3/shortmenu.asp?sName=Virginia+Tech+Dining+Services&locationNum=19&locationName=VET+MED&naFlag=1"));
		names.add("vetmed");
        // West End
		specialsSites.add(
				new URL("http://foodpro.studentprograms.vt.edu/FoodPro_2.3/shortmenu.asp?sName=Virginia+Tech+Dining+Services&locationNum=16&locationName=WEST+END+MARKET&naFlag=1"));
		names.add("westend");
		
		JSONObject allSpecials = new JSONObject();
		int counter = 0;
		// Look into doing this in parallel
		for(URL site : specialsSites) {
			URLConnection conn = site.openConnection();
			conn.setDoOutput(true);

			//Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuffer buff = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				buff.append(line+"\n");
//				resp.getWriter().println(line); //temp
			}
//			resp.getWriter().println("\n\n\n"); //temp

			String responseText = buff.toString();
			Document doc = Jsoup.parse(responseText);
			rd.close();
			
			JSONObject curSpecials = new JSONObject();
			
			for(Element listing : doc.getElementsByClass("meal_listing")) {
				String listing_title = listing.getElementsByTag("h2").first().text();
				//String category = null;
				JSONArray listingSpecials = new JSONArray();
				for(Element div : listing.getElementsByTag("div")) {
					String divClass = div.className();
					if(divClass.equals("shortmenucats")) {
						//category = div.text();
					}
					else if(divClass.equals("meal_listing_item")) {
						//assert(category != null); //temp
						listingSpecials.add(div.text());						
					}
				}
				curSpecials.put(listing_title, listingSpecials);
			}
			String hallName = names.get(counter);
			counter++;
			allSpecials.put(hallName, curSpecials);
		}
		resp.getWriter().println("[" + allSpecials.toString() + "]");
	}
}
