package app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Json {

	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		try {
			Object data = parser.parse(new FileReader("src/main/java/app/incidentassesments.json"));
//            
           
			Iterator<Object> iterator = ((ArrayList) data).iterator();
			int i= 0;
			Map<String, List<String>> listMap = new HashMap<String,List<String>>();
			ArrayList list=new ArrayList();
			while (iterator.hasNext()) {
				ArrayList tempList = new ArrayList();
				tempList.add(iterator.next());
				listMap.put("lista",tempList);
				
				//System.err.println("list: "+ list);
				i++;
				System.out.println("incidenttree for incident: "+i+" "+tempList);
			}
			System.err.println("list  "+ listMap);
//			 String keywords = (String)jsonObject.get("keywords");
//            System.err.println("keywords : "+ c);
//			 System.err.println("keywords : "+ keywords);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}