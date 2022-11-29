package services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import com.google.api.SystemParameterOrBuilder;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;

import data.Answer;
import data.Content;
import data.Found;
import data.Incident;
import data.Question;

import org.json.simple.JSONArray;

@Path("/speechservice")
public class SpeechService {

	// Lista incidenttejä varten jotta voidaan chooseIncidentissä hakea incidenttien
	// avainsanalistat
	static ArrayList<Incident> incidentList = new ArrayList<>();

	// Uusi luokka tehty frontille lähetettävää stringiä varten,
	// mutta ei ainakaan toistaiseksi löydä getterillä chooseIncidentissä
	// päivitettyä arvoa esim "1" vaan tulostaa null
	static Found tofront = new Found();
	static String testword;
	static boolean chosenIncident=false;

	// List of keywordlists of questions
//	static List<List<Question>> info = new ArrayList<>();
	static List<Question> info = new ArrayList<>();
//	static List<Answer> info2 = new ArrayList<>();
	static List<Answer> info2 = new ArrayList<>();
	static List<List<String>> info3 = new ArrayList<>();
	static List<String> testilista = new ArrayList<>();
//	static JSONArray JScont;
	static List<String> testilista2 = new ArrayList<>();
	static Content con = new Content();
	@GET
	@Path("/getdata")
	@Produces(MediaType.TEXT_PLAIN)
	public String readData() {
		String found = getFoundWord().toString();
		System.out.println("readData " + found);
		return found;
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
	public static void handleData(String transcript) {

		//Tarkistetaan onko incidentListiin jo haettu tiedot eli tehdään vain ensimmäisellä kerralla kun ohjelmaa ajetaan
		if (incidentList.isEmpty()) {

			try {
				// String json=lueTdsto();//Lue tiedosto
				JSONParser parser = new JSONParser();// Tee JSONParser
				// Object obj = parser.parse(json);//Merkkijono parsitaan objektiksi

				Object data = parser.parse(new FileReader("src/main/java/app/incidentassesments.json"));
				JSONArray array = (JSONArray) data;// Objektista JSONArray

				for (int i = 0; i < array.size(); i++) {
					JSONObject jo = (JSONObject) array.get(i);
					Incident incident = new Incident();
					incident.setId(jo.get("incident"));
					incident.setName((String) jo.get("name"));
					JSONArray arr = (JSONArray) jo.get("keywords");
					readKeyWords(arr, incident);
					arr = (JSONArray) jo.get("negative");
					readNegatives(arr, incident);

					arr = (JSONArray) jo.get("content");
					
//					System.out.println("JSCONT 103: " + JScont);
					readContent(arr, incident);
					// Väärässä paikassa -> chooseIncident() alle. 
					// Tämä try catch tehdään vain kerran alussa, kun JSON luetaan ensimmäisen kerran incident-olioon
					// arr -> incidentList (incidentList.get(0).getContent()?)
//					checkAnswers(transcript, arr); 
					
					incidentList.add(incident);
				}
				for (Incident i : incidentList) {
					System.out.println("103: " + i);
					System.out.println("104: " + i.getKeywordList());
				}
			} catch (Exception e) {
				System.err.println("Jotain meni pieleen");
			}
			
		}
		
//		que. incidentList.get(0).getContent().getQuestionList();
		
//		System.out.println("126  " + con.getQuestionList().get(0).getAnswerList().get(0));
		// Valitsee oikean incidentin keywordien perusteella
		
		// if (booleanChosenIncident == null) -> chooseIncident() 
		// else -> checkAnswers()
		if (chosenIncident==false) {
			chooseIncident(transcript);
		}
		for (int w = 0 ; w < incidentList.size(); w++) {
			con.setQuestionList(incidentList.get(w).getContent().getQuestionList());
			checkAnswers(transcript, con);
		}
		
		
		
		
		
		
		// Tähän täytyy tehdä logiikka, joka ensin käy tekemässä chooseIncidentin, josta saadaan boolean (mikä incident on valittu). 
		// Sitten kun se on tehty, voidaan kutsua checkAnswers(). 
		
	}
//	public static void getTranscriptToAnswers(String transcript) {
//		checkAnswers(transcript, JScont);
//	}
	public static void getTranscriptToAnswers(String transcript) {
//		checkAnswers(transcript, incidentList);
		
	}
	
	public Found getFoundWord() {
		System.out.println("118 tofront" + tofront.getFound());
		
		tofront.setFound(testword);
		
		return tofront;
		
	}

	private static void readContent(JSONArray arr, Incident incident) {
		Content content = new Content();
		for (int i = 0; i < arr.size(); i++) {
			JSONObject jo = (JSONObject) arr.get(i);
			JSONArray qarray = (JSONArray) jo.get("question");
			JSONObject qo = (JSONObject) qarray.get(0);
			Question q = new Question();
			q.setId(qo.get("qid"));
			q.setQuestion((String) qo.get("qvalue"));
//			System.out.println("Question 77: "+q.getId()+" "+q.getQuestion());
			JSONArray karray = (JSONArray) qo.get("qkeywords");
			ArrayList<String> qkeywordList = new ArrayList<>();
			for (int k = 0; k < karray.size(); k++) {
				qkeywordList.add((String) karray.get(k));
//				System.out.println("QKeyword 82:"+(String)karray.get(k));
			}
			q.setKeywordList(qkeywordList);
			content.addQuestionList(q);

			JSONArray aarray = (JSONArray) jo.get("answers");
			for (int k = 0; k < aarray.size(); k++) {
				JSONObject ao = (JSONObject) aarray.get(k);
				Answer a = new Answer();
				a.setId(ao.get("aid"));
				a.setAvalue((String) ao.get("avalue"));
//				System.out.println("Answer 93:"+a.getId()+" "+a.getAvalue());
				JSONArray akeyarr = (JSONArray) ao.get("akeywords");
//				System.out.println("AKeywords 94: "+ao.get("akeywords"));
			    ArrayList<String> akeyList = new ArrayList<>();
				for (int m = 0; m < akeyarr.size(); m++) {
					akeyList.add((String) akeyarr.get(m));
//					System.out.println("AKeyword 98:"+a.getId()+" "+a.getAvalue()+" "+(String) akeyarr.get(m));
				}
				a.setKeywordList(akeyList);
				q.addAnswerList(a);
				System.out.println("163: Keywords: " + akeyList);
				System.out.println("Answer keywords: " + a.getId() + " " + a.getKeywordList());
				readAkeyList(akeyList);
				
				
			}
			
		}
		incident.setContent(content);
		System.out.println("Testilista: " + testilista);
	
	}
	
	private static ArrayList<String> readAkeyList(ArrayList<String >list) {
		int counter = 0;
		while (counter<2) {
		System.out.println("174 list: " + list);
		counter++;
		}
		return list;
	}
	
	private static void readNegatives(JSONArray arr, Incident incident) {
		ArrayList<String> list = new ArrayList<>();
//		System.out.println(arr);
		for (int i = 0; i < arr.size(); i++) {
			list.add((String) arr.get(i));
//			System.out.println("Negatives rivi 111: "+(String) arr.get(i));
		}
		incident.setNegativeList(list);
	}

	private static void readKeyWords(JSONArray arr, Incident incident) {
		ArrayList<String> list = new ArrayList<>();
//		System.out.println(arr);
		for (int i = 0; i < arr.size(); i++) {
			list.add((String) arr.get(i));
		}
		incident.setKeywordList(list);
	}

	// Sama metodi mitä aiemmassa koodissa, muutettu voidiksi Found-olion takia
	private static void chooseIncident(String transcript) {
		
		System.out.println("167 " + incidentList.get(0).getKeywordList());
		transcript=transcript.toLowerCase();
		ArrayList<String> foundTreeWords = new ArrayList<String>();
		ArrayList<String> foundShopliftingWords = new ArrayList<String>();
		int calcFallen = 0;
		int calcShopl = 0;
		boolean treePhraseFound = false;
		boolean shopLiftingPhraseFound = false;

		boolean incidentFallen = false;
		boolean incidentShopL = false;

		List<String> fallenTreeKeywordList = new ArrayList<>();
		List<String> fallenTreeNegativeList = new ArrayList<>();
		List<String> shopliftingKeywordList = new ArrayList<>();
		List<String> shopliftingNegativeList = new ArrayList<>();

		fallenTreeKeywordList = incidentList.get(0).getKeywordList();
		fallenTreeNegativeList = incidentList.get(0).getNegativeList();
		shopliftingKeywordList = incidentList.get(1).getKeywordList();
		shopliftingNegativeList = incidentList.get(1).getNegativeList();

		System.out.println("188 ");

		// Transcription part
		// Splitting transcription by space into separate list
		String[] splittedList = transcript.split(" ");

		// Looping keywords list
		for (String splittedWord : splittedList) {

			// Looping splitted list
			for (String fallenTreeWord : fallenTreeKeywordList) {
				// If element of splitted list matches with element of keywords list
				// Printing "equals" and adding it to foundWords list

				if (splittedWord.contains(fallenTreeWord)) {

					// Jos splitted word ei ole negatiivinen eli on esim puu, palautuu false,
					// lisätään listaan foundTreeWords
					if (checkNegativeWords(splittedWord, fallenTreeNegativeList) == false) {

						System.out.println("splittedWord ifissä " + splittedWord);
						foundTreeWords.add(splittedWord);
						calcFallen++;
					}

				}

			}

			for (String shopliftingWord : shopliftingKeywordList) {
				// If element of splitted list matches with element of keywords list
				// Printing "equals" and adding it to foundWords list
				if (splittedWord.contains(shopliftingWord)) {

					// Jos splitted word ei ole negatiivinen eli on esim ryöstö, palautuu false,
					// lisätään listaan foundShopLiftingWords
					if (checkNegativeWords(splittedWord, shopliftingNegativeList) == false) {

						System.out.println("splittedWord ifissä " + splittedWord);
						foundShopliftingWords.add(splittedWord);
						calcShopl++;
					}

				}
			}

		}
		// ottaa lausutun fraasin vain kerran vaikka tulisi transcriptissä useamman
		// kerran
		// For checking phrases in fallen tree
		for (String keyword : fallenTreeKeywordList) {
			if (keyword.contains(" ")) {// tästä eteenpäin käsitellään vain spacen sisältäviä keywordeja

				boolean m = transcript.contains(keyword);

				System.out.println(transcript + " " + m + " mätsää " + keyword);
				if (m) {
					foundTreeWords.add(keyword);
					int count = StringUtils.countMatches(transcript, keyword);
					System.out.println("Fraaseja sanottu " + count + " kertaa kaatuneessa puussa");
					calcFallen = calcFallen + count;
				}
			}
		}
		// For checking phrases in shoplifting
		for (String keyword : shopliftingKeywordList) {

			if (keyword.contains(" ")) {
				boolean m = transcript.contains(keyword);
				System.out.println(transcript + " " + m + " mätsää " + keyword);
				if (m) {
					foundShopliftingWords.add(keyword);
					int count = StringUtils.countMatches(transcript, keyword);
					System.out.println("Fraaseja sanottu " + count + " kertaa ryöstössä");
					calcShopl = calcShopl + count;
				}
			}
		}

		System.out.println("words in foundTreedWords list: " + foundTreeWords.toString()
				+ " number of words in foundTreeWords: " + calcFallen);
		System.out.println("words in foundShopliftingWords list: " + foundShopliftingWords.toString()
				+ " number of words in foundShopliftingWords: " + calcShopl);
		if (calcFallen > calcShopl) {

			incidentFallen = true;
			incidentShopL = false;
			chosenIncident=true;
			tofront.setFound("1");
			testword = "1";
			System.err.println("Kyse on puun kaatumisesta." + testword);
			System.out.println("getFound" + tofront.getFound().toString());
		}
		if (calcFallen == calcShopl) {
			
			incidentFallen = true;
			incidentShopL = true;
			tofront.setFound("");
			testword = "";
			System.err.println("Kyse voi olla puun kaatumisesta tai ryöstöstä." + testword);
		}
		if (calcFallen < calcShopl) {
			chosenIncident=true;
			incidentFallen = false;
			incidentShopL = true;
			tofront.setFound("2");
			testword = "2";
			System.err.println("Kyse on varkaudesta/ryöstöstä." + testword);

		}
		
		// if calcFallen > calcShopl tai calcFallen < calcShopl -> boolean chosenIncident == true
	}
	
	
	
	public static void checkAnswers(String transcript, Content con) {
		Answer ans = new Answer();
		Question que = new Question();
		for (int i = 0; i < con.getQuestionList().size(); i++) {
			que.setAnswerList(con.getQuestionList().get(i).getAnswerList());
		
		
		for (int e = 0; e < que.getAnswerList().size(); e++) {
			ans.setAvalue(que.getAnswerList().get(e).getAvalue());
			ans.setId(que.getAnswerList().get(e).getId());
			ans.setKeywordList(que.getAnswerList().get(e).getKeywordList());
		
		for (String keyword : ans.getKeywordList()) {
			if (transcript.contains(keyword)) {
				System.err.println("Puheessa mainittiin ID "+ans.getId() +" eli vastaus: " + ans.getAvalue());
			}
		}
		}
		}
//		System.err.println("TADAAAAAA 390: "+ans.getAvalue() + " "+ans.getId()+" "+ans.getKeywordList());
	
	
	}
	
	
//	public static void checkAnswers(String transcript, ArrayList<Incident> incidentList2) {
//		
//		
//		
//		for (int l=0; l<incidentList2.size(); l++) {
////			JSONObject jo = (JSONObject) incidentList2.get(l);
////			JSONArray aarray = (JSONArray) jo.get("answers");
//			Content con = new Content();
//			
//			
//			List<String> lista = new ArrayList<>();
//			for (int k = 0; k < aarray.size(); k++) {
//				
////				Answer ans = new Answer();
//				Answer ans = new Answer();
//				JSONObject ao = (JSONObject) aarray.get(k);
//				ans.setId(ao.get("aid"));
//				JSONArray akeys = (JSONArray) ao.get("akeywords");
//				ans.setAvalue((String) ao.get("avalue"));
//				System.out.println("Akeys 349: " + akeys);
//				ans.setKeywordList(akeys);
//				System.out.println("TADAAAA " + ans.keywordsToString());
//				String asd = ans.keywordsToString();
//				for (String keyword : ans.getKeywordList()) {
//					if (transcript.contains(keyword)) {
//						System.err.println("Puheessa mainittiin ID "+ans.getId() +" eli vastaus: " + ans.getAvalue());
//					}
//				}
//				
////			
//				}
//				
//				}
//			}
		
	

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

	
}
