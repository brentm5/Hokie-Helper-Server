package org.vt.hokiehelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


@SuppressWarnings("serial")
public class RecsportsPassthrough extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		URL classes = new URL("https://checkin.recsports.vt.edu/fitness_classes/today.xml");
		URLConnection conn = classes.openConnection();
		conn.setDoOutput(true);

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			Document dom = db.parse(conn.getInputStream());
			Element docEle = dom.getDocumentElement();
			NodeList nl = docEle.getElementsByTagName("fitness-class");
			JSONArray jsonarray = new JSONArray();
			for(int i=0; i< nl.getLength(); i++){
				JSONObject obj = new JSONObject();
				Node class_ = nl.item(i);
				NodeList info = class_.getChildNodes();
				//resp.getWriter().println(info);
				for(int p=0; p < info.getLength(); p++){
					Node prop = info.item(p);
					if(prop.getNodeName() != null && prop.getNodeName().equals("name")){
						obj.put("name", prop.getTextContent());
					}else if(prop.getNodeName() != null && prop.getNodeName().equals("classroom")){
						obj.put("classroom", prop.getTextContent());
					}else if(prop.getNodeName() != null && prop.getNodeName().equals("starts-at")){
						obj.put("starts", prop.getTextContent());
					}else if(prop.getNodeName() != null && prop.getNodeName().equals("limit")){
						obj.put("spaces", prop.getTextContent());
					}else if(prop.getNodeName() != null && prop.getNodeName().equals("spaces-left")){
						obj.put("spacesleft", prop.getTextContent());
					}else if(prop.getNodeName() != null && prop.getNodeName().equals("location-name")){
						obj.put("building", prop.getTextContent());
					}
				}
				jsonarray.add(obj);
			}
			resp.getWriter().println(jsonarray);		
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
