package app;



public class Main {

	// Starting the program by running this Main.java class
	
	public static void main(String[] args) {
		// Creating objects from classes DataTransfer and InfiniteStreamRecognize
		DataTransfer data = new DataTransfer();
		InfiniteStreamRecognize inffi= new InfiniteStreamRecognize();
		// Creating a thread from DataTransfer and starting it
		Thread dataThread = new Thread(data);
		dataThread.start();
	
		InfiniteStreamRecognizeOptions options = InfiniteStreamRecognizeOptions.fromFlags(args);
		if (options == null) {
			// Could not parse.
			System.out.println("Failed to parse options.");
			System.exit(1);
		}
		// Creating a thread and starting it by calling the infiniteStreamingRecognize method
		try {
			inffi.infiniteStreamingRecognize(options.langCode);
		
		} catch (Exception e) {
			System.out.println("Exception caught: " + e);
		}
		
	}
	
	
}
