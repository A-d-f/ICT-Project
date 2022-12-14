package app;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.api.server.spi.Client;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeResponse;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.sun.xml.internal.stream.Entity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.TargetDataLine;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;


// Code from Google API, handles the speech recognition and transcription

public class InfiniteStreamRecognize {

	private final int STREAMING_LIMIT = 290000; // ~5 minutes

	public final String RED = "\033[0;31m";
	public final String GREEN = "\033[0;32m";
	public final String YELLOW = "\033[0;33m";

	// Creating shared object
	private volatile BlockingQueue<byte[]> sharedQueue = new LinkedBlockingQueue();
	private TargetDataLine targetDataLine;
	private int BYTES_PER_BUFFER = 6400; // buffer size in bytes

	private int restartCounter = 0;
	private ArrayList<ByteString> audioInput = new ArrayList<ByteString>();
	private ArrayList<ByteString> lastAudioInput = new ArrayList<ByteString>();
	private int resultEndTimeInMS = 0;
	private int isFinalEndTime = 0;
	private int finalRequestEndTime = 0;
	private boolean newStream = true;
	private double bridgingOffset = 0;
	private boolean lastTranscriptWasFinal = false;
	private StreamController referenceToStreamController;
	private ByteString tempByteString;
	private boolean status = true;
	DataTransfer data = new DataTransfer();

	public String convertMillisToDate(double milliSeconds) {
		long millis = (long) milliSeconds;
		DecimalFormat format = new DecimalFormat();
		format.setMinimumIntegerDigits(2);
		return String.format("%s:%s /", format.format(TimeUnit.MILLISECONDS.toMinutes(millis)),
				format.format(TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
	}

	/** Performs infinite streaming speech recognition */
	public void infiniteStreamingRecognize(String languageCode) throws Exception {

		// Microphone Input buffering
		class MicBuffer implements Runnable {

			@Override
			public void run() {
				// In order to continue the thread loop
				while (status == true) {

					System.out.println(YELLOW);
					System.out.println("Start speaking...Press Ctrl-C to stop");
					targetDataLine.start();

					byte[] data = new byte[BYTES_PER_BUFFER];
					while (targetDataLine.isOpen()) {
						try {
							int numBytesRead = targetDataLine.read(data, 0, data.length);
							if ((numBytesRead <= 0) && (targetDataLine.isOpen())) {
								continue;
							}

							sharedQueue.put(data.clone());
						} catch (InterruptedException e) {
							System.out.println("Microphone input buffering interrupted : " + e.getMessage());
						}
					}

				}
			}
		}

		// Creating microphone input buffer thread
		MicBuffer micrunnable = new MicBuffer();
		Thread micThread = new Thread(micrunnable);
		ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
		try (SpeechClient client = SpeechClient.create()) {

			ClientStream<StreamingRecognizeRequest> clientStream;
			responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {

				ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

				public void onStart(StreamController controller) {
					referenceToStreamController = controller;
				}

				public void onResponse(StreamingRecognizeResponse response) {
					responses.add(response);
					StreamingRecognitionResult result = response.getResultsList().get(0);
					Duration resultEndTime = result.getResultEndTime();
					resultEndTimeInMS = (int) ((resultEndTime.getSeconds() * 1000)
							+ (resultEndTime.getNanos() / 1000000));
					double correctedTime = resultEndTimeInMS - bridgingOffset + (STREAMING_LIMIT * restartCounter);

					SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
					if (result.getIsFinal()) {
						System.out.print(GREEN);
						System.out.print("\033[2K\r");

						System.out.printf("%s: %s [confidence: %.2f]\n", convertMillisToDate(correctedTime),
								alternative.getTranscript(), alternative.getConfidence());
						isFinalEndTime = resultEndTimeInMS;
						lastTranscriptWasFinal = true;
						String transcript = alternative.getTranscript();

						try {
							onComplete(transcript);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						System.out.print(RED);
						System.out.print("\033[2K\r");
						System.out.printf("%s: %s", convertMillisToDate(correctedTime), alternative.getTranscript());
						lastTranscriptWasFinal = false;
					}
				}

				public void onComplete(String transcript) throws IOException, InterruptedException {

					// Calling dataFetch method from DataTransfer thread, and sending transcript as
					// a parameter to it

					data.dataFetch(transcript);
				}

				public void onError(Throwable t) {
				}

				@Override
				public void onComplete() {
					// TODO Auto-generated method stub

				}
			};
			clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

			RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
					.setEncoding(RecognitionConfig.AudioEncoding.LINEAR16).setLanguageCode(languageCode)
					.setSampleRateHertz(16000).build();

			StreamingRecognitionConfig streamingRecognitionConfig = StreamingRecognitionConfig.newBuilder()
					.setConfig(recognitionConfig).setInterimResults(true).build();

			StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
					.setStreamingConfig(streamingRecognitionConfig).build(); // The first request in a streaming call
																				// has to be a config

			clientStream.send(request);

			try {
				// SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed:
				// true,
				// bigEndian: false
				AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
				DataLine.Info targetInfo = new Info(TargetDataLine.class, audioFormat); // Set the system information to
																						// read from the microphone
																						// audio
				// stream

				if (!AudioSystem.isLineSupported(targetInfo)) {
					System.out.println("Microphone not supported");
					System.exit(0);
				}
				// Target data line captures the audio stream the microphone produces.
				targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
				targetDataLine.open(audioFormat);
				micThread.start();

				long startTime = System.currentTimeMillis();

				while (true) {

					long estimatedTime = System.currentTimeMillis() - startTime;

					if (estimatedTime >= STREAMING_LIMIT) {

						clientStream.closeSend();
						referenceToStreamController.cancel(); // remove Observer

						if (resultEndTimeInMS > 0) {
							finalRequestEndTime = isFinalEndTime;
						}
						resultEndTimeInMS = 0;

						lastAudioInput = null;
						lastAudioInput = audioInput;
						audioInput = new ArrayList<ByteString>();

						restartCounter++;

						if (!lastTranscriptWasFinal) {
							System.out.print('\n');
						}

						newStream = true;

						clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);

						request = StreamingRecognizeRequest.newBuilder().setStreamingConfig(streamingRecognitionConfig)
								.build();

						System.out.println(YELLOW);
						System.out.printf("%d: RESTARTING REQUEST\n", restartCounter * STREAMING_LIMIT);

						startTime = System.currentTimeMillis();

					} else {

						if ((newStream) && (lastAudioInput.size() > 0)) {
							// if this is the first audio from a new request
							// calculate amount of unfinalized audio from last request
							// resend the audio to the speech client before incoming audio
							double chunkTime = STREAMING_LIMIT / lastAudioInput.size();
							// ms length of each chunk in previous request audio arrayList
							if (chunkTime != 0) {
								if (bridgingOffset < 0) {
									// bridging Offset accounts for time of resent audio
									// calculated from last request
									bridgingOffset = 0;
								}
								if (bridgingOffset > finalRequestEndTime) {
									bridgingOffset = finalRequestEndTime;
								}
								int chunksFromMs = (int) Math.floor((finalRequestEndTime - bridgingOffset) / chunkTime);
								// chunks from MS is number of chunks to resend
								bridgingOffset = (int) Math.floor((lastAudioInput.size() - chunksFromMs) * chunkTime);
								// set bridging offset for next request
								for (int i = chunksFromMs; i < lastAudioInput.size(); i++) {
									request = StreamingRecognizeRequest.newBuilder()
											.setAudioContent(lastAudioInput.get(i)).build();
									clientStream.send(request);
								}
							}
							newStream = false;
						}

						tempByteString = ByteString.copyFrom(sharedQueue.take());

						request = StreamingRecognizeRequest.newBuilder().setAudioContent(tempByteString).build();

						audioInput.add(tempByteString);
					}

					clientStream.send(request);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
// [END speech_transcribe_infinite_streaming]