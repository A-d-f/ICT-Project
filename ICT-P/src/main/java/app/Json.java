package app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	static String savedTranscript = "";
	
	public static Map<Integer, Object> getJson() {
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
			//Going through the listMap hashmap and calling method handleHashmap for each object inside this hashmap, number is
			// the number of incident assesment tree (integer) and incident is the object/what is inside in every incident assesment tree
			
			return listMap;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static JSONArray handleHashmapNegatives(Object incident, Integer number) {
		JSONObject jobj=(JSONObject) incident;
		String incidenttreenegative="negative";
//		String incidenttreekeywords="keywords";
		
		JSONArray msg=(JSONArray) jobj.get(incidenttreenegative);
//		JSONArray msg2=(JSONArray) jobj.get(incidenttreekeywords);
//		System.err.println("Negative keywords "+number+": "+ msg);
//		System.err.println("Keywords "+number+": "+ msg2);
		
		return msg;
	}
	private static JSONArray handleHashmapPositives(Object incident, Integer number) {
		JSONObject jobj=(JSONObject) incident;
		String incidentname="name";
		String incidenttreekeywords="keywords";
		
		String incidentn=(String) jobj.get(incidentname);
		System.err.println(incidentn);
		//jos transcript on poiminut puuhun liittyviä-> eka if
		if(incidentn.equalsIgnoreCase("kaatunut puu")) {
			JSONArray treeKeys=(JSONArray) jobj.get(incidenttreekeywords);
			System.err.println("tuleeko täältä keyt puulle?  "+treeKeys);
			return treeKeys;
		} else {
			JSONArray shopLiftKeys=(JSONArray) jobj.get(incidenttreekeywords);
			System.err.println("tuleeko täältä keyt varastamiselle?  "+shopLiftKeys);
			return shopLiftKeys;
		}
	
		
//		System.err.println("Negative keywords "+number+": "+ msg);
//		System.err.println("Keywords "+number+": "+ msg2);
		 
		
	}
	
	public static void dataFetch(String transcript) {
		System.err.println("Json-luokan transcript:");
		System.out.println("Transkripti: " + transcript);
		saveTranscriptToString(transcript);
		findKeywords(transcript);
	}

	public static void findKeywords(String transcript) {

		System.out.println("no modifications " + transcript);
		transcript = transcript.toLowerCase();
		Map<Integer, Object> listMap = new HashMap<Integer, Object>();
		System.out.println("lowerCaseTranscript? " + transcript);
		listMap= getJson();
		System.err.println(listMap);
		//listMap.forEach((number, incident)-> handleHashmapNegatives(incident, number));
		//listMap.forEach((number, incident)-> handleHashmapPos(incident, number));
		Object treeKey = listMap.keySet().toArray()[0];
		Object valueForFirstKey = listMap.get(treeKey);
		Object theftKey = listMap.keySet().toArray()[1];
		Object valueForSecKey = listMap.get(theftKey);
		
		JSONObject jobj=(JSONObject) valueForFirstKey;
		String incidenttreekeywords="keywords";
		
		JSONArray treeKeys=(JSONArray) jobj.get(incidenttreekeywords);
		
		System.err.println("JSONArray List:   "+treeKeys);
		List<String> treekeywords = new ArrayList<String>();
		List<String> treekeywords1 = new ArrayList<String>();
		treekeywords=(ArrayList<String>) Arrays.asList(treeKeys.toString());
//		for (int i=0; i<treekeywords.size();i++) {
//			treekeywords1.add(treekeywords);
//		}
		System.out.println("Arraylist treekeywords   "+treekeywords);
		
		// Lists for negative keywords to ignore
		List<String> negativeFallenKeywords = Arrays.asList("puukko", "puukkoa", "puuliiteri", "puuliiteristä",
				"puuliiterissä", "päällystää", "puimuri");
		List<String> negativeShopliftingKeywords = Arrays.asList("ryöstäytyä", "varasto", "varaslähtö");

		// Lists for keywords to search
		List<String> fallenTreeList = Arrays.asList("puu", "pui", "kaatu", "pääll");
		System.err.println("String List arraysas "+fallenTreeList);
		List<String> shopliftingList = Arrays.asList("ryöst", "varas", "asee");

		// Initializing list for matching words found from transcript
		ArrayList<String> foundTreeWords = new ArrayList<String>();
		ArrayList<String> foundShopliftingWords = new ArrayList<String>();
		int calcFallen = 0;
		int calcShopl = 0;

		// Transcription part

		// Splitting transcription by space into separate list
		String[] splittedList = transcript.split(" ");

		// Looping keywords list
		for (String splittedWord : splittedList) {

			// Looping splitted list

			for (String fallenTreeWord : fallenTreeList) {
				// If element of splitted list matches with element of keywords list
				// Printing "equals" and adding it to foundWords list
				if (splittedWord.contains(fallenTreeWord)) {

					// Jos splitted word ei ole negatiivinen eli on esim puu, palautuu false,
					// lisätään listaan foundTreeWords
					if (checkNegativeWords(splittedWord, negativeFallenKeywords) == false) {

						System.out.println("splittedWord ifissä " + splittedWord);
						foundTreeWords.add(splittedWord);
						calcFallen++;
					}

				}

			}
			for (String shopliftingWord : shopliftingList) {
				// If element of splitted list matches with element of keywords list
				// Printing "equals" and adding it to foundWords list
				if (splittedWord.contains(shopliftingWord)) {

					// Jos splitted word ei ole negatiivinen eli on esim puu, palautuu false,
					// lisätään listaan foundTreeWords
					if (checkNegativeWords(splittedWord, negativeShopliftingKeywords) == false) {

						System.out.println("splittedWord ifissä " + splittedWord);
						foundShopliftingWords.add(splittedWord);
						calcShopl++;
					}

				}
			}

		}

		System.out.println("words in foundTreedWords list: " + foundTreeWords.toString()
				+ " number of words in foundTreeWords: " + calcFallen);
		System.out.println("words in foundShopliftingWords list: " + foundShopliftingWords.toString()
				+ " number of words in foundShopliftingWords: " + calcShopl);

	}


	public static boolean checkNegativeWords(String splittedWord, List<String> negativeKeywords) {
		Iterator<String> negativeIterator = negativeKeywords.iterator();

		String neg = "";
		boolean found = false;
		while (negativeIterator.hasNext()) {

			String negativeWord = negativeIterator.next().toString();
			// System.out.println("splitted " + splittedWord + " negative " + negativeWord);
			if (splittedWord.equals(negativeWord)) {
				// System.out.println("negative word found " + negativeWord);
				neg = negativeWord;
				found = true;
				break;
			} else {
				// System.out.println("not found" + splittedWord);
				found = false;

			}

		}

		return found;
	}

	public static void saveTranscriptToString(String transcript) {

		savedTranscript = savedTranscript + transcript;

		System.out.println("Saved transcript: " + savedTranscript);
	}
}
