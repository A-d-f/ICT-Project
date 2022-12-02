package services;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import java.util.Comparator;

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

	boolean chosenIncident = false;
	static Content con = new Content();
	static Found tofront = new Found();
	static Found incIndex = new Found();

	@POST
	@Path("/getdata")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	public String readData(Found found) {
		System.out.println("readData " + found.getId() + " " + found.getFoundWords() + " " + found.getSize() + " "
				+ found.getValue());
		tofront.setId(found.getId());
		tofront.setFoundWords(found.getFoundWords());
		tofront.setValue(found.getValue());
		return "success";

	}

	@GET
	@Path("/getvalues")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static ArrayList<Found> getFoundValues() {

		ArrayList<Found> list = new ArrayList<>();
		System.out.println("founds id" + tofront.getId());
		list.add(tofront);
		System.out.println("lista" + list.toString());

		return list;

	}

	public void handleData(String transcript) throws IOException, InterruptedException {

		// Tarkistetaan onko incidentListiin jo haettu tiedot eli tehdään vain
		// ensimmäisellä kerralla kun ohjelmaa ajetaan
		if (incidentList.isEmpty()) {

			try {
				JSONParser parser = new JSONParser();// Tee JSONParser

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

					readContent(arr, incident);

					// Tämä try catch tehdään vain kerran alussa, kun JSON luetaan ensimmäisen
					// kerran incident-olioon

					incidentList.add(incident);
				}
				for (Incident i : incidentList) {

				}
			} catch (Exception e) {
				System.err.println("Jotain meni pieleen");
			}

		}

		// Valitsee oikean incidentin keywordien perusteella

		if (chosenIncident == false) {
			chooseIncident(transcript);
		} else {
			System.out.println("INC ID " + incIndex.getId());

			int index = Integer.parseInt(incIndex.getId()) - 1;
			con.setQuestionList(incidentList.get(index).getContent().getQuestionList());
			checkAnswers(transcript, con);

		}

	}

	public void sendObject(Found found) {
		// Creating client etc for REST
		String uri = "http://127.0.0.1:8080/rest/speechservice/getdata";
		Client c = ClientBuilder.newClient();
		WebTarget wt = c.target(uri);
		Builder b = wt.request();
		// Here we create an Entity of a Found object as JSON string format
		Entity<Found> e = Entity.entity(found, MediaType.APPLICATION_JSON);
		String s = b.post(e, String.class);// We get the response as a String
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

			JSONArray karray = (JSONArray) qo.get("qkeywords");
			ArrayList<String> qkeywordList = new ArrayList<>();
			for (int k = 0; k < karray.size(); k++) {
				qkeywordList.add((String) karray.get(k));

			}
			q.setKeywordList(qkeywordList);
			content.addQuestionList(q);

			JSONArray aarray = (JSONArray) jo.get("answers");
			for (int k = 0; k < aarray.size(); k++) {
				JSONObject ao = (JSONObject) aarray.get(k);
				Answer a = new Answer();
				a.setId(ao.get("aid"));
				a.setAvalue((String) ao.get("avalue"));

				JSONArray akeyarr = (JSONArray) ao.get("akeywords");

				ArrayList<String> akeyList = new ArrayList<>();
				for (int m = 0; m < akeyarr.size(); m++) {
					akeyList.add((String) akeyarr.get(m));

				}
				JSONArray aNegakeyarr = (JSONArray) ao.get("anegative");

				ArrayList<String> aNegakeyList = new ArrayList<>();
				for (int n = 0; n < aNegakeyarr.size(); n++) {
					aNegakeyList.add((String) aNegakeyarr.get(n));

				}
				a.setKeywordList(akeyList);
				a.setNegativeList(aNegakeyList);
				q.addAnswerList(a);

			}

		}
		incident.setContent(content);

	}

	private void readNegatives(JSONArray arr, Incident incident) {
		ArrayList<String> list = new ArrayList<>();

		for (int i = 0; i < arr.size(); i++) {
			list.add((String) arr.get(i));

		}
		incident.setNegativeList(list);
	}

	private void readKeyWords(JSONArray arr, Incident incident) {
		ArrayList<String> list = new ArrayList<>();

		for (int i = 0; i < arr.size(); i++) {
			list.add((String) arr.get(i));
		}
		incident.setKeywordList(list);
	}

	// Method for choosing incident,
	// Returns boolean value
	public boolean chooseIncident(String transcript) {

		// transcript changed for LowerCase
		transcript = transcript.toLowerCase();

		// Creating ArrayList for objects
		ArrayList<Found> objList = new ArrayList<Found>();

		// Looping through incidentList for objects
		for (Incident inc : incidentList) {
			// Creating object for receiving values
			Found foundObj = new Found();

			// Setting id and value (name)
			foundObj.setId(Integer.toString(inc.getId()));
			foundObj.setValue(inc.getName());

			// Creating lists for keywords, negative keywords and matching words
			ArrayList<String> keywords = new ArrayList<>();
			ArrayList<String> negatives = new ArrayList<>();
			ArrayList<String> foundWords = new ArrayList<>();

			// getting inc objects keyword lists
			keywords.addAll(inc.getKeywordList());
			negatives.addAll(inc.getNegativeList());

			// Splitting transcript to separate words for looping through
			String[] splittedList = transcript.split(" ");

			// Looping splittedWord list
			for (String splittedWord : splittedList) {

				// Looping keyword list
				for (String keyword : keywords) {

					if (splittedWord.contains(keyword)) {

						// checking that found word is not in the negative keyword list
						// (checkNegativeWords())
						// if not then it is added to foundWords list
						if (checkNegativeWords(splittedWord, negatives) == false) {
							foundWords.add(splittedWord);
						}
					}
				}
			}
			// Looping through phrases
			for (String keyword : keywords) {
				if (keyword.contains(" ")) {

					// if transcript contains phrase, matching = true and phrase will be added to
					// foundWords list
					boolean matching = transcript.contains(keyword);

					if (matching) {
						foundWords.add(keyword);
						int count = StringUtils.countMatches(transcript, keyword);
						System.out.println("Phrases " + count);
					}
				}
			}

			// Setting foundWords list to object
			foundObj.setFoundWords(foundWords);
			// Setting foundWords list size to object
			foundObj.setSize(foundWords.size());
			// Adding object to ojbList
			objList.add(foundObj);

		}

		// Sorting objList with Collections method sort by object's keywordlist size, so
		// the biggest list is in the first index
		Collections.sort(objList, Comparator.comparingInt(Found::getSize).reversed());
		System.out.println("ObjList " + objList.get(0));
		// If previously sorted lists first indexes list isEmpty chosenIncident = false,
		// otherwise true (Incident has been found)
		if (objList.get(0).getFoundWords().isEmpty()) {
			chosenIncident = false;
		} else {

			chosenIncident = true;
			sendObject(objList.get(0));
			incIndex.setId(objList.get(0).getId());

		}

		return chosenIncident;

	}

	// Compares transcript to answer keywords and answer phrases of a certain
	// incident
	public void checkAnswers(String transcript, Content con) {
		transcript = transcript.toLowerCase();
		System.out.println("Transcript: " + transcript);
		List<String> array = new ArrayList<>();
		Answer ans = new Answer();
		Question que = new Question();
//		Found fou = new Found();
		ArrayList<Found> objectList = new ArrayList<Found>();
		boolean negativeFound;
		// First 2 loops:
		// the first loop loops questionlist of Content object -> gets answerlists of
		// every questionlist indexes -> sets answerlists to object Question
		// the second loop loops answerlists of Question object -> gets value, id and
		// keywords -> sets them to object Answer
		for (int i = 0; i < con.getQuestionList().size(); i++) {
			que.setAnswerList(con.getQuestionList().get(i).getAnswerList());

			for (int e = 0; e < que.getAnswerList().size(); e++) {
				ans.setAvalue(que.getAnswerList().get(e).getAvalue());
				ans.setId(que.getAnswerList().get(e).getId());
				ans.setKeywordList(que.getAnswerList().get(e).getKeywordList());
				ans.setNegativeList(que.getAnswerList().get(e).getNegativeList());
				
				Found fou = new Found();
//				fou.setId(Integer.toString(ans.getId()));
				// Comparing single keywords
				String[] splittedList = transcript.split(" ");
				// loops transcript's word separately -> sets them to String splittedWord
				
				for (String splittedWord : splittedList) {
					// loops keyword list of object Answer -> sets keywords to String keyword
					for (String keyword : ans.getKeywordList()) {
						Found fo = new Found();
						if (splittedWord.contains(keyword)) {

							// Creating a new Found object for the method

							
							// Finding more matching words from answerlist
							System.out.println("keywords: " + splittedWord);
							fo = checkAnswerlist(splittedList, ans.getKeywordList(), ans.getNegativeList(), ans.getId());
							System.out.println("FO " + fo.toString());
							// If the size of the object's list is larger than 1...
							fo.setId(Integer.toString(ans.getId()));
							if (fo.getSize() > 1) {
								
								// ...and if the array is empty -> found words are added to array

								if (array.isEmpty()) {
									array.addAll(fo.getFoundWords());
									System.err.println("ID 359: " + fo.getId());
								}
								// Setting answer id to Found object
//								fou.setId(Integer.toString(ans.getId()));
								fou.setId(fo.getId());
								break;
							}

						}
						// Comparing phrases
						else if (keyword.contains(" ")) {

							// if transcript contains phrase and matching = true, phrase will be added to
							// the same
							// array of already found words -> the array will be set to found words of
							// object Found
							boolean matching = transcript.contains(keyword);

							if (matching) {
								System.err.println("ID 385: " + fou.getId());
//								fou.setId(fo.getId());
								array.add(keyword);
								System.err.println("ID 388: " + fou.getId());
								int count = StringUtils.countMatches(transcript, keyword);

								System.out.println("Phrases " + count);

							}
						}
						
					}
					
				}
				System.err.println("ARRAY 367: " + array);
//				for (String keyword : ans.getKeywordList()) {
//					// If keyword is a phrase (contains "SPACE")
//					if (keyword.contains(" ")) {
//
//						// if transcript contains phrase and matching = true, phrase will be added to
//						// the same
//						// array of already found words -> the array will be set to found words of
//						// object Found
//						boolean matching = transcript.contains(keyword);
//
//						if (matching) {
//							System.err.println("ID 385: " + fou.getId());
//							fou.setId(Integer.toString(ans.getId()));
//							array.add(keyword);
//							System.err.println("ID 388: " + fou.getId());
//							int count = StringUtils.countMatches(transcript, keyword);
//
//							System.out.println("Phrases " + count);
//
//						}
//					}
//				}
				fou.setFoundWords(array);
				fou.setSize(fou.getFoundWords().size());
				System.out.println("FOU PRINT: " + fou.getId() + " " + fou.getFoundWords());
				objectList.add(fou);
//				System.err.println("Object list: " + objectList.get(0).toString());
				
			}
//			fou.setFoundWords(array);
//			fou.setSize(fou.getFoundWords().size());
//			
//			objectList.add(fou);
//			System.err.println("Object list: " + objectList.toString());
			
		}
		Collections.sort(objectList, Comparator.comparingInt(Found::getSize).reversed());
		System.out.println("ObjList " + objectList.get(0));
		// If previously sorted lists first indexes list isEmpty chosenIncident = false,
		// otherwise true (Incident has been found)
		
		if (objectList.get(0).getFoundWords().isEmpty()) {
//			chosenIncident = false;
		} else {

//			chosenIncident = true;
//			sendObject(objectList.get(0));
//			incIndex.setId(objectList.get(0).getId());
			sendObject(objectList.get(0));

		}
		System.err.println("ARRAY 393: " + array);
//		System.out.println("FOU PRINT: " + fou.getId() + " " + fou.getFoundWords());
		// Sending the Found object to frontend
//		sendObject(fou);
	}

	// Method for comparing transcript words to every word in keyword list of a
	// certain Answer object
	// Creating object for saving found words and list size
	// If matching words are found -> add the word to "list"
	private Found checkAnswerlist(String[] splittedList, ArrayList<String> keywordList,
			ArrayList<String> negativeList, int id) {
		List<String> list = new ArrayList<>();
		Found f = new Found();
		boolean negativeFound;
		System.out.println("Splitted list checkAList " + splittedList);
		for (String splittedWord : splittedList) {
			
			for (String keyword : keywordList) {
				if (splittedWord.contains(keyword)) {
					System.out.println("Splitted word & keyword checkAList " + splittedWord + " " + keyword);
					
					negativeFound = checkNegativeWords(splittedWord, negativeList);
					System.out.println("BOOLEAN: " + negativeFound);
					if (negativeFound == false) {
						System.out.println("splittedword 406 " + splittedWord);
						list.add(splittedWord);
					}
				}
			}
		}
		f.setFoundWords(list);
		f.setSize(list.size());
//		f.setId(Integer.toString(id));
		return f;
	}

	public static boolean checkNegativeWords(String splittedWord, List<String> negativeKeywords) {
		Iterator<String> negativeIterator = negativeKeywords.iterator();

		String neg = "";
		boolean found = false;
		while (negativeIterator.hasNext()) {

			String negativeWord = negativeIterator.next().toString();

			if (splittedWord.equals(negativeWord)) {

				neg = negativeWord;
				found = true;
				break;
			} else {

				found = false;

			}

		}

		return found;
	}

}
