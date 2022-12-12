package app;

import java.io.IOException;
import services.SpeechService;

// Class to send transcript data to SpeechService.java

public class DataTransfer implements Runnable {
	boolean status = true;
	static String savedTranscript = "";
	private static String currentTranscript = "";
	SpeechService service = new SpeechService();

	// Initially was supposed to send the data forward to SpeechService when this
	// thread is active,
	// but it functions basically even without this threading structure.

	@Override
	public void run() {
		while (status == true) {

			currentTranscript = ""; // clearing

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	// Sending the transcript to SpeechService.java using method handleData
	public void dataFetch(String transcript) throws IOException, InterruptedException {

		saveTranscriptToString(transcript);

		service.handleData(transcript);

	}

	// Saving the whole transcript
	public void saveTranscriptToString(String transcript) {
//		currentTranscript = transcript;
		savedTranscript = savedTranscript + transcript;

	}

}
