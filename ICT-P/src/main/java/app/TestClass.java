package app;

import java.awt.List;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import com.google.protobuf.ByteString;

import javax.sound.sampled.DataLine.Info;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;

public class TestClass {

	public static void main(String[] args) throws Exception {
		streamingMicRecognize();

	}

	public static void streamingMicRecognize() throws Exception {

		ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
		try (SpeechClient client = SpeechClient.create()) {

			responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {
				ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

				public void onStart(StreamController controller) {
				}

				public void onResponse(StreamingRecognizeResponse response) {
					responses.add(response);
				}

				public void onComplete() {
					//Initializing empty list for searched keywords
					//Chosen keywords: "puu" and "kaatunut"
					String[] keywords = new String[] { "puu", "kaatunut" };
					
					//Initializing list for matching words found from transcript
					ArrayList<String> foundWords = new ArrayList<String>();

					//Transcription part
					for (StreamingRecognizeResponse response : responses) {
						StreamingRecognitionResult result = response.getResultsList().get(0);
						SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
						System.out.printf("Transcript : %s\n", alternative.getTranscript());

						//Setting transcription into string
						String string = alternative.getTranscript();
						System.out.println(string);

						//Splitting transcription by space into separate list
						String[] splittedList = string.split(" ");

						//Looping keywords list
						for (String a : keywords) {
							System.out.println("keyword: " + a);

							//Looping splitted list
							for (String b : splittedList) {
								//If element of splitted list matches with element of keywords list
								//Printing "equals" and adding it to foundWords list
								if (b.equals(a)) {
									System.out.println("equals");
									foundWords.add(b);
									
								//Else printing "not" if words doesn't match	
								} else {
									System.out.println("not");
								}
							}
						}
						
						//Looping foundWords list and printing it's elements
						for (int i = 0; i < foundWords.size(); i++) {
							System.out.println("foundWords: " + foundWords.get(i) + " indeksi: " + i);
						}
					}
				}

				public void onError(Throwable t) {
					System.out.println(t);
				}
			};

			ClientStream<StreamingRecognizeRequest> clientStream = client.streamingRecognizeCallable()
					.splitCall(responseObserver);

			RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
					.setEncoding(RecognitionConfig.AudioEncoding.LINEAR16).setLanguageCode("fi-FI")
					.setSampleRateHertz(16000).build();
			StreamingRecognitionConfig streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder()
					.setConfig(recognitionConfig).build();

			StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
					.setStreamingConfig(streamingRecognitionConfig).build(); // The first request in a streaming call
																				// has to be a config

			clientStream.send(request);
			// SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed:
			// true,
			// bigEndian: false
			AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
			DataLine.Info targetInfo = new Info(TargetDataLine.class, audioFormat); // Set the system information to
																					// read from the microphone audio
																					// stream

			if (!AudioSystem.isLineSupported(targetInfo)) {
				System.out.println("Microphone not supported");
				System.exit(0);
			}
			// Target data line captures the audio stream the microphone produces.
			TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
			targetDataLine.open(audioFormat);
			targetDataLine.start();
			System.out.println("Start speaking");

			long startTime = System.currentTimeMillis();
			System.out.println(startTime);
			// Audio Input Stream
			AudioInputStream audio = new AudioInputStream(targetDataLine);

			while (true) {

				long estimatedTime = System.currentTimeMillis() - startTime;

				byte[] data = new byte[1600];

				audio.read(data);

				if (estimatedTime > 15000) { // 60 seconds
					System.out.println("Stop speaking.");
					targetDataLine.stop();
					targetDataLine.close();
					break;
				}

				request = StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(data)).build();
				clientStream.send(request);

			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("error 116");
		}
		responseObserver.onComplete();
	}
}
