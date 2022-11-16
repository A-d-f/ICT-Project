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
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Iterators;

public class Json {

	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		try {
			// save JSON inside an Object
			Object data = parser.parse(new FileReader("src/main/java/app/incidentassesments.json"));
			// Iterator for going through data object
			Iterator<Object> iterator = ((ArrayList) data).iterator();
			// Iterator for calculating size
			Iterator<Object> iterator2 = ((ArrayList) data).iterator();
			// New hashmap for saving iterators objects
			Map<Integer, Object> listMap = new HashMap<Integer, Object>();
			// loop for calculating how many object is inside the iterator
			int iteratorSize = 0;
			while (iterator2.hasNext()) {
				iteratorSize++;
				iterator2.next();
			}
			
			while (iterator.hasNext()) {
				
				for (int i=0; i<iteratorSize; i++) {
				listMap.put(i, iterator.next());
				}
					
			}
			listMap.forEach((number, incident)-> handleHashmap(incident, number));
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void handleHashmap(Object incident, Integer number) {
		JSONObject jobj=(JSONObject) incident;
		String incidenttreenegative="negative";
		String incidenttreekeywords="keywords";
		JSONArray msg=(JSONArray) jobj.get(incidenttreenegative);
		JSONArray msg2=(JSONArray) jobj.get(incidenttreekeywords);
		System.err.println("Negative keywords "+number+": "+ msg);
		System.err.println("Keywords "+number+": "+ msg2);
		
		
	}
}