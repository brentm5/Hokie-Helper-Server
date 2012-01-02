package org.vt.hokiehelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@SuppressWarnings("serial")
public class FootballScheduleServlet extends HttpServlet {

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		URL scheduleUrl = new URL("http://www.hokiesports.com/football/schedule/");
		URLConnection conn = scheduleUrl.openConnection();
		conn.setDoOutput(true);

		//Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		StringBuffer buff = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			buff.append(line+"\n");
//			resp.getWriter().println(line); //temp
		}
//		resp.getWriter().println("\n\n\n"); //temp

		String responseText = buff.toString();
		Document doc = Jsoup.parse(responseText);
		rd.close();

		Element schedule = doc.getElementsByClass("schedule").get(1);
		JSONArray games = new JSONArray();
		String rClass;		
		String special = null;
		for(Element row : schedule.getElementsByTag("tr")) {
			JSONObject game = new JSONObject();
			rClass = row.className();
			if(rClass.equals("evenrow") || rClass.equals("oddrow")) {
				Elements info = row.getElementsByTag("td");
				game.put("date", info.get(0).text());
				game.put("rank", info.get(1).text());
				// "*" in the name denotes an ACC opponent
				game.put("opponent", info.get(2).text().replace("*", "(ACC)")); 
				game.put("info", info.get(3).text());
				game.put("tv", info.get(4).text());
				game.put("loc", info.get(5).text());
				if(special != null) {
					game.put("special", special);
					special = null;
				}
				games.add(game);
			}
			if(row.id().equals("TopTR")) {
				// The row following this row is a special event game, for example a white out game
				// Not sure if we need this information, but putting this here just in case we decide to use it
				special = row.text();
			}
		}

		resp.getWriter().println(games.toJSONString());

	}

}

