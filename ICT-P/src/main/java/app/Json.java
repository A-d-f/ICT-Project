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
			System.err.println(iteratorSize);
			
			while (iterator.hasNext()) {
				
				for (int i=0; i<iteratorSize; i++) {
				listMap.put(i, iterator.next());
				}
					
			}
			System.err.println("list  "+ listMap);
			//mapping the hashmap for separate lists
			listMap.forEach((k, v)-> System.out.println("KEY: " + k + " List: "+ v));
			System.err.println(listMap.get(1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}