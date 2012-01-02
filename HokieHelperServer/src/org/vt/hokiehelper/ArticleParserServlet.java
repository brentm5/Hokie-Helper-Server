package org.vt.hokiehelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.mail.BodyPart;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@SuppressWarnings("serial")
public class ArticleParserServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setHeader("Content-Type", "text/plain; charset=utf-8");
		URL articleUrl = new URL(req.getParameter("link"));
		
		URLConnection conn = articleUrl.openConnection();
		conn.setDoOutput(true);
		
		//Get the response
		InputStreamReader ird = new InputStreamReader(conn.getInputStream(), "utf-8");
		BufferedReader rd = new BufferedReader(ird);
		String line;
		StringBuffer buff = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			buff.append(line+"\n");
//			resp.getWriter().println(line); //temp
		}
//		resp.getWriter().println("\n\n\n"); //temp
		
		// Convert from unicode to UTF-8
		String responseText = new String(buff.toString().getBytes("UTF-8"), "UTF-8");
		Document doc = Jsoup.parse(responseText);
		rd.close();
		
		JSONObject obj = new JSONObject();
		
		Elements bodyParagraphs =  doc.select("#vt_pr_content_body > *");
		Element author = doc.getElementsByClass("vt_cl_name").first();
		StringBuffer bodyBuffer = new StringBuffer();
		for(Element e : bodyParagraphs) {
			String tag = e.tagName();
			if(!tag.equals("div")) {
				if(tag.equals("ul")) {
					for(Element l : e.children()) {
						bodyBuffer.append("\t* ");
						bodyBuffer.append(l.text() + "\n");
					}
				} else if(tag.equals("ol")) {
					int counter = 1;
					for(Element l : e.children()) {
						bodyBuffer.append("\t" + counter + " ");
						bodyBuffer.append(l.text() + "\n");
						counter++;
					}
					
				} else {
					bodyBuffer.append(e.text() + "\n");
				}
				bodyBuffer.append("\n");
			}

		}
		String body = bodyBuffer.toString();
		
		obj.put("body", body);
		obj.put("author", author.text());
		Element imageDiv = doc.getElementsByClass("vt_img_caption_center").first();
		if(imageDiv != null) {
			Element image = imageDiv.getElementsByTag("img").first();
			obj.put("image", image.attr("src"));
			obj.put("alttext", image.attr("alt"));
		}

		resp.getWriter().print(obj.toJSONString());
	}
}
