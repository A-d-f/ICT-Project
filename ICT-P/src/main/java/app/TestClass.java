package app;

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
		
		//Credential creds = GoogleCredential.getApplicationDefault();
	    ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
	    try (SpeechClient client = SpeechClient.create()) {

	      responseObserver =
	          new ResponseObserver<StreamingRecognizeResponse>() {
	            ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

	            public void onStart(StreamController controller) {}

	            public void onResponse(StreamingRecognizeResponse response) {
	              responses.add(response);
	            }

	            public void onComplete() {
	              for (StreamingRecognizeResponse response : responses) {
	                StreamingRecognitionResult result = response.getResultsList().get(0);
	                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
	                System.out.printf("Transcript : %s\n", alternative.getTranscript());
	              }
	            }

	            public void onError(Throwable t) {
	              System.out.println(t);
	            }
	          };

	      ClientStream<StreamingRecognizeRequest> clientStream =
	          client.streamingRecognizeCallable().splitCall(responseObserver);

	      RecognitionConfig recognitionConfig =
	          RecognitionConfig.newBuilder()
	              .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
	              .setLanguageCode("fi-FI")
	              .setSampleRateHertz(16000)
	              .build();
	      StreamingRecognitionConfig streamingRecognitionConfig =
	          StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build();

	      StreamingRecognizeRequest request =
	          StreamingRecognizeRequest.newBuilder()
	              .setStreamingConfig(streamingRecognitionConfig)
	              .build(); // The first request in a streaming call has to be a config

	      clientStream.send(request);
	      // SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed: true,
	      // bigEndian: false
	      AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
	      DataLine.Info targetInfo =
	          new Info(
	              TargetDataLine.class,
	              audioFormat); // Set the system information to read from the microphone audio stream

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
	        
	        byte[] data = new byte[3200];
	       
	        audio.read(data);
	        
	        if (estimatedTime > 30000) { // 60 seconds
	          System.out.println("Stop speaking.");
	          targetDataLine.stop();
	          targetDataLine.close();
	          break;
	        }
	        
	        request =
	            StreamingRecognizeRequest.newBuilder()
	                .setAudioContent(ByteString.copyFrom(data))
	                .build();
	        clientStream.send(request);
	      
	      }
	    } catch (Exception e) {
	      System.out.println(e);
	      System.out.println("error 116");
	    }
	    responseObserver.onComplete();
	  }
}
