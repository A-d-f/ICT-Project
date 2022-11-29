package services;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

import data.Answer;
import data.Content;
import data.Found;
import data.Incident;
import data.Question;



@Path("/speechservice")
public class SpeechService {

	// Lista incidenttejä varten jotta voidaan chooseIncidentissä hakea incidenttien
	// avainsanalistat
	public ArrayList<Incident> incidentList = new ArrayList<>();

	// Uusi luokka tehty frontille lähetettävää stringiä varten,
	// mutta ei ainakaan toistaiseksi löydä getterillä chooseIncidentissä
	// päivitettyä arvoa esim "1" vaan tulostaa null

	
	
	static boolean chosenIncident=false;
	static Content con = new Content();
	public static Found tofront = new Found();
	public List<Question> info = new ArrayList<>();
	public List<Answer> info2 = new ArrayList<>();
	public List<List<String>> info3 = new ArrayList<>();
	public List<String> testilista = new ArrayList<>();

	@POST
	@Path("/getdata")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public String readData(Found found) {
		System.out.println("readData " + found.getId());
		tofront.setId(found.getId());
		tofront.setFoundWords(found.getFoundWords());		
		return "success";
	}

	@GET
	@Path("/getthis")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static ArrayList<Found> getTranscript() {

		ArrayList<Found> list = new ArrayList<>();
		Found found = new Found();
		found.setId(tofront.getId());
		System.out.println("founds id" + found.getId());	
		list.add(tofront);
		System.out.println("lista" + list.toString());
	
		return list;
		
	}

	public void handleData(String transcript) throws IOException, InterruptedException {

		//Tarkistetaan onko incidentListiin jo haettu tiedot eli tehdään vain ensimmäisellä kerralla kun ohjelmaa ajetaan
		if (incidentList.isEmpty()) {

			try {
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
	
	public void sendObject(Found found) {
		//Creating client etc for REST
		String uri = "http://127.0.0.1:8080/rest/speechservice/getdata";
		Client c=ClientBuilder.newClient();
		WebTarget wt=c.target(uri);
		Builder b=wt.request();
		//Here we create an Entity of a Found object as JSON string format
		Entity<Found> e=Entity.entity(found,MediaType.APPLICATION_JSON);		
		String s=b.post(e, String.class);//We get the response as a String
	}
	
	
	private void readContent(JSONArray arr, Incident incident) {
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
	
	private ArrayList<String> readAkeyList(ArrayList<String >list) {
		int counter = 0;
		while (counter<2) {
		System.out.println("174 list: " + list);
		counter++;
		}
		return list;
	}
	
	private void readNegatives(JSONArray arr, Incident incident) {
		ArrayList<String> list = new ArrayList<>();
//		System.out.println(arr);
		for (int i = 0; i < arr.size(); i++) {
			list.add((String) arr.get(i));
//			System.out.println("Negatives rivi 111: "+(String) arr.get(i));
		}
		incident.setNegativeList(list);
	}

	private void readKeyWords(JSONArray arr, Incident incident) {
		ArrayList<String> list = new ArrayList<>();
//		System.out.println(arr);
		for (int i = 0; i < arr.size(); i++) {
			list.add((String) arr.get(i));
		}
		incident.setKeywordList(list);
	}

	// Sama metodi mitä aiemmassa koodissa, muutettu voidiksi Found-olion takia

	public void chooseIncident(String transcript) throws IOException, InterruptedException {

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
			tofront.setId("1");
			tofront.setValue("");
			tofront.setFoundWords(foundTreeWords);
			sendObject(tofront);
			System.err.println("Kyse on puun kaatumisesta.");
			

		}
		if (calcFallen == calcShopl) {
			
			incidentFallen = true;
			incidentShopL = true;
			System.err.println("Kyse voi olla puun kaatumisesta tai ryöstöstä.");
		}
		if (calcFallen < calcShopl) {
			chosenIncident=true;
			incidentFallen = false;
			incidentShopL = true;
			tofront.setId("2");
			tofront.setValue("");
			tofront.setFoundWords(foundShopliftingWords);
			sendObject(tofront);
			System.err.println("Kyse on varkaudesta/ryöstöstä.");

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
