package services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

	// Handling the transcript data and sending data to frontend via REST

	// List for incident objects
	public ArrayList<Incident> incidentList = new ArrayList<>();
	// Boolean attribute - is the incident chosen (true) or not chosen (false) by
	// the calltaker
	boolean chosenIncident = false;
	// Getting question and answer content to be sent to checkAnswers method
	static Content con = new Content();
	static Found tofront = new Found();

	int incidentchosen = 0;
	Found fromfrontend = new Found();
	String selectedIncident;
	int selected;

	// Receiving Found object from method sendObject, setting it to object "tofront"
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

	// Setting object tofront to a object list, returning list
	// This method is called from the frontend
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

	// When incident is selected in frontend:
	// Receiving the id of the selected incident and setting it to object fromFront
	@POST
	@Path("/selectincident")
	@Produces(MediaType.TEXT_PLAIN)
	public void selectIncident(String chosenIncident) throws IOException {

		System.out.println("Selected: " + chosenIncident);

		ServerSocket ss = new ServerSocket(6666);
		Socket s = ss.accept();
		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		out.writeUTF(chosenIncident);
		ss.close();
	}

	public void handleData(String transcript) throws IOException, InterruptedException {

		// Checking if incidentList already contains info - it is done only once when
		// the program is ran

		if (incidentList.isEmpty()) {

			try {
				JSONParser parser = new JSONParser();
				// Reading the data from the json file into an Object
				Object data = parser.parse(new FileReader("src/main/java/app/incidentassesments.json"));
				JSONArray array = (JSONArray) data;
				// Fetching data to objects and saving the objects to incidentList
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

					incidentList.add(incident);
				}

			} catch (Exception e) {
				System.err.println("Something went wrong.");
			}

		}

		// If incident hasn't been chosen, method chooseIncident is called, and checking
		// if the incident ID has been received from frontend

		if (chosenIncident == false) {
			chooseIncident(transcript);
			incidentchosen = Integer.parseInt(idFromSocket());
			// If the ID is bigger than zero, an incident has been chosen and
			// the chosenIncident is set "true"
			if (incidentchosen > 0) {
				chosenIncident = true;
				System.err.println(" ID " + incidentchosen);
				int index = (incidentchosen - 1);
				// Setting the data to con of the selected incident
				con.setQuestionList(incidentList.get(index).getContent().getQuestionList());
				// Sending the data to checkAnswers with transcript
				checkAnswers(transcript, con);
			}
			// If incident has been chosen, the data is set to con
		} else {
			int index = (incidentchosen - 1);
			con.setQuestionList(incidentList.get(index).getContent().getQuestionList());
			// Sending the data to checkAnswers with transcript
			checkAnswers(transcript, con);

		}

	}

	// Fetching incident ID from frontend
	public String idFromSocket() throws UnknownHostException, IOException {
		Socket s = new Socket("localhost", 6666);
		DataInputStream dis = new DataInputStream(s.getInputStream());
		String isId = (String) dis.readUTF();

		return isId;
	}

	// Method to sending incident and answer objects to frontend
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

	// Iterating and reading the content of the json and saving it to Content,
	// Question and Answer objects
	// The whole object Content is saved to object Incident in the end
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

	// Method for reading negative keywords of the incident
	private void readNegatives(JSONArray arr, Incident incident) {
		ArrayList<String> list = new ArrayList<>();

		for (int i = 0; i < arr.size(); i++) {
			list.add((String) arr.get(i));

		}
		incident.setNegativeList(list);
	}

	// Method for reading keywords of the incident
	private void readKeyWords(JSONArray arr, Incident incident) {
		ArrayList<String> list = new ArrayList<>();

		for (int i = 0; i < arr.size(); i++) {
			list.add((String) arr.get(i));
		}
		incident.setKeywordList(list);
	}

	// Method for choosing incident,
	// returns boolean value
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
		// otherwise sending found object to object sendObject

		if (objList.get(0).getFoundWords().isEmpty()) {
			chosenIncident = false;

		} else {
			sendObject(objList.get(0));
		}
		return chosenIncident;

	}

	// Checking the answers:
	// Compares transcript to answer keywords and answer phrases of a certain
	// incident
	public void checkAnswers(String transcript, Content con) {
		transcript = transcript.toLowerCase();
		System.out.println("Transcript: " + transcript);

		Answer ans = new Answer();
		Question que = new Question();
		ArrayList<Found> objectList = new ArrayList<Found>();
		String[] splittedList = transcript.split(" ");
		// First 2 loops:
		// the first loop loops questionlist of Content object -> gets answerlists of
		// every questionlist indexes -> sets answerlists to object Question

		for (int i = 0; i < con.getQuestionList().size(); i++) {
			que.setAnswerList(con.getQuestionList().get(i).getAnswerList());

			// the second loop loops answerlists of Question object -> gets value, id and
			// keywords -> sets them to object Answer
			for (int e = 0; e < que.getAnswerList().size(); e++) {
				ans.setAvalue(que.getAnswerList().get(e).getAvalue());
				ans.setId(que.getAnswerList().get(e).getId());
				ans.setKeywordList(que.getAnswerList().get(e).getKeywordList());
				ans.setNegativeList(que.getAnswerList().get(e).getNegativeList());

				// Creating list for found keywords and Found object for saving them and the
				// answers id
				List<String> array = new ArrayList<>();
				Found fou = new Found();

				// Looping through transcript
				for (String splittedWord : splittedList) {
					// Checking if splittedWord is in the keywordlist
					String foundWord = getMatchingSubstring(splittedWord, ans.getKeywordList());

					// If found word is not null, then checking if it is the negativelist
					if (foundWord != null) {

						boolean isNeg = checkNegativeWords(foundWord, ans.getNegativeList());
						// If isNeg == false (the word was not in the negativelist), adding it to the
						// list
						if (isNeg == false) {
							array.add(splittedWord);
						}
					}
				}

				// Phrases
				for (String keyword : ans.getKeywordList()) {
					if (keyword.contains(" ")) {

						// if transcript contains phrase and matching = true, phrase will be added to
						// the same array of already found words
						boolean matching = transcript.contains(keyword);

						if (matching) {
							array.add(keyword);
							int count = StringUtils.countMatches(transcript, keyword);
							System.out.println("Phrases " + count);
						}
					}
				}

				// If the list of found words is greater than 1, setting answer id
				// and
				// the array and it's size to Found Object "fou"
				// Finally adding the fou object to objectList

				if (array.size() > 1) {
					fou.setId(Integer.toString(ans.getId()));
					fou.setFoundWords(array);
					fou.setSize(array.size());
					objectList.add(fou);
				}
			}
		}
		System.out.println("objectList " + objectList.toString());
		// If the objectList has content, sorting objects by their size (list size) in
		// descending order
		if (objectList.size() > 0) {
			Collections.sort(objectList, Comparator.comparingInt(Found::getSize).reversed());

			// If there is only one object, sending that to frontend
			if (objectList.size() == 1) {
				sendObject(objectList.get(0));

			}
			// If there is more than one object in objectList, saving first two indexes
			// sizes to variables
			if (objectList.size() > 1) {

				int index0size = objectList.get(0).getFoundWords().size();
				int index1size = objectList.get(1).getFoundWords().size();
				System.out.println("index 0: " + objectList.get(0).getFoundWords() + " index 1: "
						+ objectList.get(1).getFoundWords());

				// If the first list has more words than the second, the object will be sent to
				// frontend
				// Else (if the lists contain the same amount of words), just printing to
				// console
				if (index0size > index1size) {
					sendObject(objectList.get(0));
				}

				else {
					System.out.println("Cannot make a suggestion.");
				}

			}

		}

	}

	// Method that receives String and List<String>, word is compared to the list
	// and if it matches, it will be returned
	private static String getMatchingSubstring(String str, List<String> substrings) {
		return substrings.stream().filter(str::contains).findAny().orElse(null);
	}

	// Comparing transcript words to the negative keywords list
	// Returns boolean value "found" - if negative word is found, the value is true,
	// otherwise false
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
