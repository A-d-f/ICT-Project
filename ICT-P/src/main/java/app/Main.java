package app;



public class Main {

	public static void main(String[] args) {
		DataTransfer data = new DataTransfer();
		InfiniteStreamRecognize inffi= new InfiniteStreamRecognize();
		Thread dataThread = new Thread(data);
		dataThread.start();
		InfiniteStreamRecognizeOptions options = InfiniteStreamRecognizeOptions.fromFlags(args);
		if (options == null) {
			// Could not parse.
			System.out.println("Failed to parse options.");
			System.exit(1);
		}

		try {
			inffi.infiniteStreamingRecognize(options.langCode);
		
		} catch (Exception e) {
			System.out.println("Exception caught: " + e);
		}
		
	}//main ends
	
	
}
