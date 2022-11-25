package services;


import java.io.FileReader;
import java.io.Reader;
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
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import data.Answer;
import data.Content;
import data.Question;

import org.json.simple.JSONArray;


@Path("/speechservice")
public class SpeechService {
	// List of keywordlists of questions
//	static List<List<Question>> info = new ArrayList<>();
	static List<Question> info = new ArrayList<>();
	static List<Answer> info2 = new ArrayList<>();
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
		System.out.println("Keylist for tree : " + keyListForTree);
		// Getting the incident assesment tree for shoplifting to an object
		JSONObject shopLiftingJSON = (JSONObject) listMap.get(1);
		//Incident assesment tree for kaupparyöstö:
		
		// testin vuoksi erilliset metodit
		handleContent1(fallenTreeJSON);
		
		
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

	} // testin vuoksi erilliset metodit
	private static void handleContent1(JSONObject jsonObject) {

//		List<String> contentList = (List<String>) jsonObject.get(content);
		Object content = jsonObject.get("content");
//		System.out.println(content);
		Map<Integer, Object> contentMap = new HashMap<Integer, Object>();
//		for (int y=0; y<content.)
//		contentMap.put(null, contentMap)
		contentMap = getJson2(content);
		System.out.println("Content map 201: "+contentMap);
		JSONObject contentMapped;
//		JSONArray answerMapped = null;
		Object questions=null;
		Object answers = null;
		JSONArray jiisoni = new JSONArray();
		JSONArray jiisoni2 = new JSONArray();
		for (int i = 0; i<contentMap.size(); i++) {
			contentMapped = (JSONObject) contentMap.get(i);
//			jiisoni= (JSONArray) contentMap.get(i);
			
//			System.out.println("Testilista: "+jiisoni);
			questions = contentMapped.get("question");
			answers = contentMapped.get("answers");
			
			System.out.println("221: "+answers);
			jiisoni.add(questions);
			jiisoni.add(answers);
//			jiisoni2.add(answers);
//			testi.add((String) contentMapped.get("question"));
			System.out.println("Testilista: "+jiisoni);
//			System.out.println("Testilista2: "+jiisoni2);
			System.out.println("Kysymykset: " + (i+1) + " " + questions);
//			for (int y =0; y<jiisoni.size(); y++) {
//				JSONObject quesvalue=(JSONObject) contentMapped.get("value");
//				System.out.println("Quesvalue: "+quesvalue);
//			}
		}
		
		handleContent2(jiisoni);
//		handleContent3(jiisoni2);
		

	
}
	private static void handleContent2(JSONArray json) {
		System.out.println("Handle content 2 lista: " + json);
		Map<Integer, Object> objectmap = new HashMap<Integer, Object>();
		Object objekti = (Object) json;
		objectmap=getJson2(objekti);
		//
		for (int i=0; i<objectmap.size(); i++) {
			System.out.println("233: "+objectmap.get(i));
			objectmap.put(i, objectmap.get(i));
			System.out.println("objectmap: "+objectmap);
			System.out.println("keywords: "+  objectmap.get("keywords"));
		}
		
		// loop through objectmap and call getValue with parameter object value
		objectmap.forEach((key, value)-> {
			getValue(value);
		});
		System.err.println(" RIVIN 262 KYSYMYSLISTA TÄSSÄ OLKAA HYVÄ!: "+info.toString());
		System.err.println(" RIVIN 263 VASTAUSLISTA TÄSSÄ OLKAA HYVÄ!: "+info2.toString());
}
//	private static void handleContent3(JSONArray json) {
//		System.out.println("Handle content 2 lista: " + json);
//		Map<Integer, Object> objectmap = new HashMap<Integer, Object>();
//		Object objekti = (Object) json;
//		objectmap=getJson2(objekti);
//		//
//		for (int i=0; i<objectmap.size(); i++) {
//			System.out.println("233: "+objectmap.get(i));
//			objectmap.put(i, objectmap.get(i));
//			System.out.println("objectmap 2: "+objectmap);
//			System.out.println("keywords: "+  objectmap.get("keywords"));
//		}
//		
//		// loop through objectmap and call getValue with parameter object value
//		objectmap.forEach((key, value)-> {
//			getValue2(value);
//		});
//		System.err.println(" RIVIN 251 INFOLISTA TÄSSÄ OLKAA HYVÄ!: "+info.toString());
//}

	private static void getValue(Object value) {
//		System.out.println("TADAAAAAAAA: " + value);
		//Change object to JsonArray
		JSONArray array= (JSONArray) value;
		// Object from arrays first index
//		JSONObject quesObject = null;
		JSONObject quest = (JSONObject) array.get(0);
		String valueSt= (String) quest.get("qvalue");
		String id=(String) quest.get("qid");
		List<String> keyt = (List<String>) quest.get("qkeywords");
		
		for (int u=0;u<array.size();u++) {
			 JSONObject quesObject= (JSONObject) array.get(u);
			 System.out.println("quesobject: " + quesObject);
				// List for objects keywords
//				List<String> keyt = (List<String>) quesObject.get("qkeywords");
//				String valueSt = (String) quesObject.get("qvalue");
//				String id=(String) quesObject.get("qid");
//				List<String> neg = (List<String>) quesObject.get("qnegative");
				List<String> akeyt= (List<String>) quesObject.get("akeywords");
				String aValue = (String) quesObject.get("avalue");
				String aid = (String) quesObject.get("aid");
				System.out.println("AID: " + aid);
				List<String> anegative=(List<String>) quesObject.get("anegative");
//				System.out.println("String value onpi: "+valueSt);
				// saving all question objects keywords (keyslist) to an arraylist info
				Question que = new Question(id,valueSt,keyt);
				Answer ans = new Answer(aid, aValue, akeyt);
//				Content content = new Content(id, valueSt, keyt, neg, aid, aValue, akeyt, anegative);
//				que.setKeywords(keyt);
//				System.out.println("Question luokan keyt: "+content.getQkeywords());
//				System.out.println("Question luokan value:" + content.getQvalue());
//				System.out.println("Question luokan ID:" + content.getQid());
//				
//				System.out.println("Answer luokan keyt: "+content.getAkeywords());
//				System.out.println("Answer luokan value:" + content.getAvalue());
//				System.out.println("Answer luokan ID:" + content.getAid());
				info.add(que);
				info2.add(ans);
		}
		
		
	}
	private static void getValue2(Object value) {
//		System.out.println("TADAAAAAAAA: " + value);
		//Change object to JsonArray
		JSONArray array= (JSONArray) value;
		// Object from arrays first index
		JSONObject quesObject= (JSONObject) array.get(0);
		System.out.println("getValue 2 quesObject: "+ quesObject);
		// List for objects keywords
		List<String> keyt = (List<String>) quesObject.get("akeywords");
		String valueSt = (String) quesObject.get("avalue");
		String id=(String) quesObject.get("aid");
		List<String> neg = (List<String>) quesObject.get("anegative");
		System.out.println("String value onpi: "+valueSt);
		// saving all question objects keywords (keyslist) to an arraylist info
//		Content content = new Content(id, valueSt, keyt, neg);
//		Question que = new Question(id,valueSt,keyt);
//		Answer ans = new Answer()
//		que.setKeywords(keyt);
//		System.out.println("Answer luokan keyt: "+content.getAkeywords());
////		que.setValue(valueSt);
//		System.out.println("Answer luokan value:" + content.getAvalue());
//		System.out.println("Answer luokan ID:" + content.getAid());
//		info.add(content);
		
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
	public static Map<Integer, Object> getJson2(Object data) {
		JSONParser parser = new JSONParser();
		try {
			// save JSON inside an Object
			//Object data = parser.parse(new FileReader("src/main/java/app/incidentassesments.json"));
			// Iterator for going through data object
			Iterator<Object> iterator = ((ArrayList) data).iterator();
			// Iterator for calculating size
			Iterator<Object> iterator2 = ((ArrayList) data).iterator();
			// New hashmap for saving iterators objects
			Map<Integer, Object> listMap2 = new HashMap<Integer, Object>();
			// loop for calculating how many object is inside the iterator
			int iteratorSize = 0;
			while (iterator2.hasNext()) {
				iteratorSize++;
				iterator2.next();
			}

			while (iterator.hasNext()) {

				for (int i = 0; i < iteratorSize; i++) {
					listMap2.put(i, iterator.next());
				}

			}
			// Going through the listMap hashmap and calling method handleHashmap for each
			// object inside this hashmap, number is
			// the number of incident assesment tree (integer) and incident is the
			// object/what is inside in every incident assesment tree

			return listMap2;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


}