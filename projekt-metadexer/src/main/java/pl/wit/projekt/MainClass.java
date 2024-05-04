package pl.wit.projekt;

import java.io.IOException;

/// Run package pl.wit.projekt as Java Application!
public class MainClass {
	public static void main(String[] args) throws IOException {
		Metadata md = new Metadata();

		new GUI();
		//md.DiscoverImages("..\\samples");
		// Start searching for JPG or JPEG files (non-artificial!) 
		// in given directory and sub-directories.
		
	}
}
