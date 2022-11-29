package app;

import java.io.IOException;

import services.SpeechService;

public class DataTransfer implements Runnable {
	boolean testi=true;
	static String savedTranscript="";
	private static String currentTranscript="";
	SpeechService service=new SpeechService();
	@Override
	public void run() {
		while (testi==true) {
		System.out.println("Current transcript: " + currentTranscript);
		currentTranscript=""; //nollaus
		System.out.println("Saved transcript: " + savedTranscript);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
	}
	public void dataFetch(String transcript) throws IOException, InterruptedException {
		System.err.println("Json-luokan transcript:");
		System.out.println("Transkripti: " + transcript);
		saveTranscriptToString(transcript);
		//SpeechService.getTranscript(transcript);
		service.handleData(transcript);

		
	}
	public void saveTranscriptToString(String transcript) {
		currentTranscript=transcript;
		savedTranscript = savedTranscript + transcript;

		System.out.println("Saved transcript: " + savedTranscript);
	}
	
}
