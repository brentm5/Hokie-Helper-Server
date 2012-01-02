package org.vt.hokiehelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mortbay.log.Log;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

// The plan is to only run this once a day, to update the hours in the datastore for the day
@SuppressWarnings("serial")
public class DiningHoursServlet extends HttpServlet {
	
	private DatastoreService datastore_ = DatastoreServiceFactory.getDatastoreService();
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		URL hoursUrl = 
				new URL("https://secure.hosting.vt.edu/www.dining.vt.edu/hours/index.php?d=t");
		URLConnection conn = hoursUrl.openConnection();
		conn.setDoOutput(true);
		
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
		
		Elements listElements = doc.getElementsByTag("li");
		int counter = 0;
		
		HashSet<String> halls = DiningUtils.hallsHash;
		String currentHall = null;
		String cur;
		JSONArray currentHours = null;
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
		//df.setTimeZone( TimeZone.getTimeZone("America/New_York"));
		Pattern pattern = Pattern.compile("([a-zA-Z ]+) ([1-9][0-2]?:[0-5][0-5] [ampm]{2})-([1-9][0-2]?:[0-5][0-5] [ampm]{2})");
		
		// Temp data structure, eventually want this to be in the datastore
		JSONObject hallHours = new JSONObject();
		JSONArray returnHours = new JSONArray();
		for(Element e : listElements) {
			cur = e.ownText();
			if(halls.contains(cur)) {
				if(currentHall != null) {
					hallHours.put("hall", currentHall);
					hallHours.put("hours", currentHours);
					returnHours.add(hallHours);
				}
				
				hallHours = new JSONObject();
				currentHall = cur;
				//resp.getWriter().println(cur);
				currentHours = new JSONArray();
			} else {
				try {
					JSONObject hour = new JSONObject();
					Date temp = new Date();
					//resp.getWriter().println(temp);
					boolean nextDay = false;
					GregorianCalendar calendar = new GregorianCalendar();
					Matcher matcher = pattern.matcher(cur);
					matcher.find();
					hour.put("name", matcher.group(1));
					String start = matcher.group(2);
					String end = matcher.group(3);
					//resp.getWriter().println(matcher.group(1));
					//resp.getWriter().println(start + " - " + end);
					int startHour = 0, startMin = 0, endHour = 0, endMin = 0;
					String startTime = null, endTime = null;
					startHour = Integer.parseInt(start.substring(0, start.indexOf(':')));
					startMin = Integer.parseInt(start.substring(start.indexOf(':')+1, start.indexOf(' ',start.indexOf(':'))));
					if(start.contains("pm")){
						startHour+=12;
					}
					temp.setHours(startHour);
					temp.setMinutes(startMin);
					temp.setSeconds(0);
					calendar.setTime(temp);
					startTime = df.format(calendar.getTime());
					endHour = Integer.parseInt(end.substring(0, end.indexOf(':')));
					if(end.contains("pm")){
						endHour+=12;
					}else if(end.contains("am")){
						if (endHour >= 12)
							endHour-=12;
						nextDay = true;
					}
					endMin = Integer.parseInt(end.substring(end.indexOf(':')+1, end.indexOf(' ',start.indexOf(':'))));
					temp.setHours(endHour);
					temp.setMinutes(endMin);
					temp.setSeconds(0);
					calendar.setTime(temp);
					Date end1 = calendar.getTime();
					if(matcher.group(1).contains("Regular Hours")){
						//resp.getWriter().println(cur + " - endHour: " + endHour);
						if(nextDay) {
							end1.setDate(calendar.get(Calendar.DATE)+1);
						}
					}
					endTime = df.format(end1);
					hour.put("start", startTime);
					hour.put("end",  endTime);
					//resp.getWriter().println(startTime + " - " + endTime);
					currentHours.add(hour);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//endTime = Time.valueOf(end);
				//hour.put("start", df.format(start));
				//hour.put("end", df.format(end));
				//hour.put("start", new Time(startHour,startMin,0));
				//hour.put("end",  new Time(endHour,endMin,0));
				//currentHours.add(hour);
			}
		}
			resp.getWriter().println(returnHours);
	}
}
