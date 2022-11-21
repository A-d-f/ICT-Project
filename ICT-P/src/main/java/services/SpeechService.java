package services;


import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


@Path("/speechservice")
public class SpeechService {
	
	@GET
	@Path("/getdata")
	@Produces(MediaType.TEXT_PLAIN)
	public String readData() {


		return "testing";

	}

	
//	@GET
//	@Path("/getTranscript")
//	@Produces(MediaType.TEXT_PLAIN)
//	public static String getTranscript(String currentTranscript) {
//		currentTranscript=transcript;
//		return "Testing";
//	}
	@GET
	@Path("/handledata")
	@Produces(MediaType.TEXT_PLAIN)
	public static String handleData(String transcript) {
		boolean incidentFallen=false;
		boolean incidentShopL=false;
		String incidenttreekeywords = "keywords";
		String incidenttreenegativekeywords = "negative";
		System.out.println("no modifications " + transcript);
		transcript = transcript.toLowerCase();
		Map<Integer, Object> listMap = new HashMap<Integer, Object>();
		System.out.println("lowerCaseTranscript? " + transcript);
		listMap = getJson();

		// Getting the incident assesment tree for tree falling to an object
		JSONObject fallenTreeJSON = (JSONObject) listMap.get(0);
		System.err.println("Fallen tree JSON: " + fallenTreeJSON);
		
		// List contains JSONs keywords for falling tree
		List<String> keyListForTree = (List<String>) fallenTreeJSON.get(incidenttreekeywords);
		// List contains JSONs negative words for tree falling
		List<String> keyNegativeListForTree = (List<String>) fallenTreeJSON.get(incidenttreenegativekeywords);

		// Getting the incident assesment tree for shoplifting to an object
		JSONObject shopLiftingJSON = (JSONObject) listMap.get(1);
		//Incident assesment tree for kaupparyöstö:
		
		
		List<String> keyListForSL = (List<String>) shopLiftingJSON.get(incidenttreekeywords);
		// List contains JSONs negative words for shoplifting
		List<String> keyNegativeListForSL = (List<String>) shopLiftingJSON.get(incidenttreenegativekeywords);
		System.err.println("Shoplifting JSON: " + shopLiftingJSON);

		// Initializing list for matching words found from transcript
		ArrayList<String> foundTreeWords = new ArrayList<String>();
		ArrayList<String> foundShopliftingWords = new ArrayList<String>();
		int calcFallen = 0;
		int calcShopl = 0;
		boolean treePhraseFound=false;
		boolean shopLiftingPhraseFound=false;
		// Transcription part

		// Splitting transcription by space into separate list
		String[] splittedList = transcript.split(" ");

		// Looping keywords list
		for (String splittedWord : splittedList) {

			// Looping splitted list

			for (String fallenTreeWord : keyListForTree) {
				// If element of splitted list matches with element of keywords list
				// Printing "equals" and adding it to foundWords list

				if (splittedWord.contains(fallenTreeWord)) {

					// Jos splitted word ei ole negatiivinen eli on esim puu, palautuu false,
					// lisätään listaan foundTreeWords
					if (checkNegativeWords(splittedWord, keyNegativeListForTree) == false) {

						System.out.println("splittedWord ifissä " + splittedWord);
						foundTreeWords.add(splittedWord);
						calcFallen++;
					}

				}

			}

			for (String shopliftingWord : keyListForSL) {
				// If element of splitted list matches with element of keywords list
				// Printing "equals" and adding it to foundWords list
				if (splittedWord.contains(shopliftingWord)) {

					// Jos splitted word ei ole negatiivinen eli on esim ryöstö, palautuu false,
					// lisätään listaan foundShopLiftingWords
					if (checkNegativeWords(splittedWord, keyNegativeListForSL) == false) {

						System.out.println("splittedWord ifissä " + splittedWord);
						foundShopliftingWords.add(splittedWord);
						calcShopl++;
					}

				}
			}

		}
		// ottaa lausutun fraasin vain kerran vaikka tulisi transcriptissä useamman kerran
		// For checking phrases in fallen tree
		for (String keyword : keyListForTree) {
			if (keyword.contains(" ")) {//tästä eteenpäin käsitellään vain spacen sisältäviä keywordeja
				
				boolean m = transcript.contains(keyword);
				
				System.out.println(transcript + " " + m + " mätsää " + keyword);
				if (m) {
					foundTreeWords.add(keyword);
					int count = StringUtils.countMatches(transcript,keyword);
					System.out.println("Fraaseja sanottu " + count + " kertaa kaatuneessa puussa");
					calcFallen=calcFallen+count;
				}
			}
		}
		// For checking phrases in shoplifting
		for (String keyword : keyListForSL) {
			
			if (keyword.contains(" ")) {
				boolean m = transcript.contains(keyword);
				System.out.println(transcript + " " + m + " mätsää " + keyword);
				if (m) {
					foundShopliftingWords.add(keyword);
					int count = StringUtils.countMatches(transcript,keyword);
					System.out.println("Fraaseja sanottu " + count + " kertaa ryöstössä");
					calcShopl=calcShopl+count;
				}
			}
		}
	
		System.out.println("words in foundTreedWords list: " + foundTreeWords.toString()
				+ " number of words in foundTreeWords: " + calcFallen);
		System.out.println("words in foundShopliftingWords list: " + foundShopliftingWords.toString()
				+ " number of words in foundShopliftingWords: " + calcShopl);
		if(calcFallen>calcShopl) {
			System.err.println("Kyse on puun kaatumisesta.");
			incidentFallen=true;
			incidentShopL=false;
		}
		if(calcFallen==calcShopl) {
			System.err.println("Kyse voi olla puun kaatumisesta tai ryöstöstä.");
			incidentFallen=true;
			incidentShopL=true;
		}
		if (calcFallen<calcShopl){
			System.err.println("Kyse on varkaudesta/ryöstöstä.");
			incidentFallen=false;
			incidentShopL=true;
		}

		return "Getting transcript";

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

				for (int i = 0; i < iteratorSize; i++) {
					listMap.put(i, iterator.next());
				}

			}
			// Going through the listMap hashmap and calling method handleHashmap for each
			// object inside this hashmap, number is
			// the number of incident assesment tree (integer) and incident is the
			// object/what is inside in every incident assesment tree

			return listMap;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	


}