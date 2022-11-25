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

import data.Answer;
import data.Content;
import data.Incident;
import data.Question;

import org.json.simple.JSONArray;


@Path("/speechservice")
public class SpeechService {
	
	static ArrayList<Incident> incidentList=new ArrayList<>();
	static String tofront = null;
	
	// List of keywordlists of questions
//	static List<List<Question>> info = new ArrayList<>();
	static List<Question> info = new ArrayList<>();
//	static List<Answer> info2 = new ArrayList<>();
	static List<Answer> info2=new ArrayList<>();
	static List<List<String>> info3=new ArrayList<>();
	
	@GET
	@Path("/getdata")
	@Produces(MediaType.TEXT_PLAIN)
	public String readData() {
		
		return "1";
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
		ArrayList<Incident> incidentList=new ArrayList<>();
		try {
			String json=lueTdsto();//Lue tiedosto
			JSONParser parser = new JSONParser();//Tee JSONParser
			Object obj = parser.parse(json);//Merkkijono parsitaan objektiksi
			
//			Object data = parser.parse(new FileReader("src/main/java/app/incidentassesments.json"));
			JSONArray array = (JSONArray)obj;//Objektista JSONArray
			
			for (int i=0;i<array.size();i++) {
				JSONObject jo = (JSONObject)array.get(i);
				Incident incident=new Incident();
				incident.setId(jo.get("incident"));
				incident.setName((String) jo.get("name"));
				JSONArray arr=(JSONArray)jo.get("keywords");
				readKeyWords(arr, incident);
				arr=(JSONArray)jo.get("negative");
				readNegatives(arr, incident);

				arr=(JSONArray)jo.get("content");
				readContent(arr, incident);
				
				incidentList.add(incident);
			}
			for (Incident i:incidentList) {
				System.out.println(i);
			}
			
			System.out.println("95: " + incidentList.get(1).getKeywordList());
//			tofront = chooseIncident(transcript);
//			System.out.println("100: " + tofront);
			
			
		}
		catch(Exception e) {
		    System.err.println("Jotain meni pieleen");
		}
	}
	
	private static void readContent(JSONArray arr, Incident incident) {
		Content content=new Content();
		for (int i=0;i<arr.size();i++) {
			JSONObject jo=(JSONObject)arr.get(i);
			JSONArray qarray=(JSONArray)jo.get("question");
			JSONObject qo=(JSONObject)qarray.get(0);
			Question q=new Question();
			q.setId(qo.get("qid"));
			q.setQuestion((String) qo.get("qvalue"));
//			System.out.println("Question 77: "+q.getId()+" "+q.getQuestion());
			JSONArray karray=(JSONArray)qo.get("qkeywords");
			ArrayList<String> qkeywordList=new ArrayList<>();
			for (int k=0;k<karray.size();k++) {
				qkeywordList.add((String)karray.get(k));
//				System.out.println("QKeyword 82:"+(String)karray.get(k));
			}
			q.setKeywordList(qkeywordList);
			content.addQuestionList(q);
			
			JSONArray aarray=(JSONArray)jo.get("answers");
			for (int k=0;k<aarray.size();k++) {
				JSONObject ao=(JSONObject)aarray.get(k);
				Answer a=new Answer();
				a.setId(ao.get("aid"));
				a.setAvalue((String) ao.get("avalue"));
//				System.out.println("Answer 93:"+a.getId()+" "+a.getAvalue());
				JSONArray akeyarr=(JSONArray)ao.get("akeywords");
//				System.out.println("AKeywords 94: "+ao.get("akeywords"));
				ArrayList<String> akeyList=new ArrayList<>();
				for (int m=0;m<akeyarr.size();m++) {
					akeyList.add((String) akeyarr.get(m));
//					System.out.println("AKeyword 98:"+a.getId()+" "+a.getAvalue()+" "+(String) akeyarr.get(m));
				}
				a.setKeywordList(akeyList);
				q.addAnswerList(a);
			}
		}
		incident.setContent(content);
	}
	private static void readNegatives(JSONArray arr, Incident incident) {
		ArrayList<String> list=new ArrayList<>();
//		System.out.println(arr);
		for (int i=0;i<arr.size();i++) {
			list.add((String) arr.get(i));
//			System.out.println("Negatives rivi 111: "+(String) arr.get(i));
		}
		incident.setNegativeList(list);
	}
	private static void readKeyWords(JSONArray arr, Incident incident) {
		ArrayList<String> list=new ArrayList<>();
//		System.out.println(arr);
		for (int i=0;i<arr.size();i++) {
			list.add((String) arr.get(i));
		}
		incident.setKeywordList(list);
	}
	
	private static String chooseIncident(String transcript) {
		System.out.println("167 " + incidentList.get(0).getKeywordList());
		
		ArrayList<String> foundTreeWords = new ArrayList<String>();
		ArrayList<String> foundShopliftingWords = new ArrayList<String>();
		int calcFallen = 0;
		int calcShopl = 0;
		boolean treePhraseFound=false;
		boolean shopLiftingPhraseFound=false;
		
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
		// ottaa lausutun fraasin vain kerran vaikka tulisi transcriptissä useamman kerran
		// For checking phrases in fallen tree
		for (String keyword : fallenTreeKeywordList) {
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
		for (String keyword : shopliftingKeywordList) {
			
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
	
	public static String lueTdsto() {
		File in=new File("./src/main/java/app/incidentassesments.json");
		File f=new File("./src/main/java/app/incidentassesments.json");
		if (f.exists()) {
			System.out.println("File exists");
		}
		FileReader fr=null;
		StringBuffer sb=new StringBuffer();
		try{
			fr=new FileReader(in);
			//luettujen merkkien lukumäärä
			int lkm=0;
			//taulukko, johon merkit luetaan
			char [] c=new char[10];
			
			//niin kauan kuin luettujen merkkien 
			//lukumäärä on eri kuin -1 
			while ((lkm=fr.read(c))!=-1){
				//tulostetaan luetut merkit tiedostoon, ei näytölle
//				fw.write(c, 0, lkm);
				System.out.print(c);
				sb.append(c, 0, lkm);
			}
		}
		/*
		 * FileReader -luokan muodostin heittää poikkeuksen
		 * FileNotFoundException, jos tiedostoa ei löydy
		*/		
		catch (FileNotFoundException e){
			Tulosta("Tiedostoa ei löytynyt: "+
					e.getMessage());
		}

		/*
		 * FileReader.read heittää poikkeuksen 
		 * IOException, jos lukeminen epäonnistuu
		 * Saman poikkeuksen heittää myös FileWriter -muodostin
		 */
		catch (IOException e){
			Tulosta("Tiedoston lukeminen epäonnistui: "+
					e.getMessage());
		}
		
		/*
		 * Tapahtuipa poikkeuksia tai ei, try - catch - finally
		 * kokonaisuudessa toteutetaan viimeisenä aina finally -lohko.
		 */		
		finally{
			try{
				if (fr!=null)
					fr.close();
			}
			//Myös close voi heittää poikkeuksen, joka
			//on otettava kiinni
			catch (IOException e){
				//do nothing
			}
		}
		Tulosta(sb.toString());
		return sb.toString();
	}
	static void Tulosta(String s){
		System.out.println(s);
	}
}

