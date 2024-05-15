package pl.wit.projekt;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Jan Konarski - JUnit Tests for verifying App Integrity
 *
 */

public class MultithreadingTest {
	/***
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InterruptedException
	 */
	private String imageOriginalPath = "D:\\My Computer\\OneDrive\\Mój Komputer\\Pulpit\\Metadexer\\Metadexer\\samples\\natural\\bkg1.jpg";
	private String outputDirectoryPath = "D:\\My Computer\\OneDrive\\Mój Komputer\\Pulpit\\Metadexer\\Metadexer\\Bangkok\\";
	private String inputDirectoryPath = "D:\\My Computer\\OneDrive\\Mój Komputer\\Pulpit\\Metadexer\\Metadexer\\samples\\natural\\";
	
	private String photosOutputPath = "D:\\My Computer\\OneDrive\\Mój Komputer\\Pulpit\\Metadexer\\Metadexer\\Photos\\"; // extra
	/// Try to open GUI window
	/**
	 * 
	 * @throws IOException
	 */
	@Test
	public void openWindowTest() throws IOException {
		GUI gui = new GUI();
		Assertions.assertNotNull(gui);
	}
	/// Check if it's possible to copy a file
		/***
		 * 
		 * @throws IOException
		 * @throws NoSuchAlgorithmException
		 * @throws InterruptedException
		 */
		@Test
		public void copyFilesTestSingleThread() throws IOException, NoSuchAlgorithmException, InterruptedException {
			GUI gui = new GUI();
			Metadata metadata = new Metadata(gui);
			
			String imagePath = imageOriginalPath;
			String outputDirectory = outputDirectoryPath;
			cleanup(outputDirectory);
			
			if (Files.exists(Paths.get(imagePath))) {
				File f = new File(imagePath);
				File d = new File(outputDirectory);
				Integer filesExpected = 50;
				metadata.setRootOutputDirectory(outputDirectory);
				
				for(int i = 0; i<filesExpected; ++i) {
					metadata.processImage(f.toPath());
				}
				
				if (d.exists()) {
					Assertions.assertEquals(filesExpected, Metadata.countImageFiles(outputDirectory));
					cleanup(outputDirectory);
				}
			}else
				Assertions.fail("Image file doesn't exist");
	}
	/// Try to use Single-Thread mode
	/***
	 * 
	 * @throws IOException
	 */
	@Test
	public void createAndRunThreadTest() throws IOException {
		GUI gui = new GUI();
		Metadata metadata = new Metadata(gui);
		
		String imagePath = imageOriginalPath;
		String outputDirectory = outputDirectoryPath;
		cleanup(outputDirectory);
		metadata.setRootOutputDirectory(outputDirectory);
		
		WorkerThread thread = new WorkerThread(Paths.get(imagePath), metadata);
		thread.run();
		
		Assertions.assertTrue(thread.getThreadStatus());
		cleanup(outputDirectory);
	}
	/// Check if Scan Directory returns hashes, required for dupe prevention
	/***
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void scanDirectoryTest() throws IOException, NoSuchAlgorithmException {
		GUI gui = new GUI();
		Metadata metadata = new Metadata(gui);
		String inputPath = inputDirectoryPath;
		
		metadata.scanDirectories(inputPath);
		for (String key : metadata.getFileHashcodeSet()) {
			System.out.println(key);
		}
		
		String imagePath = imageOriginalPath;
		String outputDirectory = outputDirectoryPath;
		cleanup(outputDirectory);
		File dir = new File(outputDirectory);
		dir.mkdirs();
		
		metadata.setRootOutputDirectory(outputDirectory);
		WorkerThread thread = new WorkerThread(Paths.get(imagePath), metadata);
		thread.run();
		
		String md5Hash = Metadata.checksum(Paths.get(outputDirectory.concat("2024.01.23\\1.jpg")));
		
		Assertions.assertTrue(metadata.getFileHashcodeSet().contains(md5Hash));	// <- Copied file hash identifies it doesn't need to be copied again.
	}
	/// Verify dupes not generating
	/***
	 * 
	 * @throws IOException
	 */
	@Test
	public void tryCopyingDupeTest() throws IOException {
		GUI gui = new GUI();
		Metadata metadata = new Metadata(gui);
		String imagePath = imageOriginalPath;
		String outputDirectory = outputDirectoryPath;
		
		metadata.setRootOutputDirectory(outputDirectory);
		WorkerThread thread = new WorkerThread(Paths.get(imagePath), metadata);
		thread.run();
		
		Assertions.assertEquals(1, Metadata.countImageFiles(outputDirectory));	// Try to copy a dupe, notice the file count doesn't change.
		cleanup(outputDirectory);
	}
	/// Try to run multi-thread mode
	/***
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InterruptedException
	 */
	@Test
	public void createAndRunMultithreadedTest() throws IOException, NoSuchAlgorithmException, InterruptedException {
		GUI gui = new GUI();
		Metadata metadata = new Metadata(gui);
		
		String inputPath = inputDirectoryPath;
		String outputDirectory = photosOutputPath;
		cleanup(outputDirectory);
		
		File outputDir = new File(outputDirectory);
		outputDir.mkdirs();
		metadata.setRootOutputDirectory(outputDirectory);
		metadata.discoverImages(inputPath, 5);
		
		Integer expectedFiles = Metadata.countImageFiles(inputPath);
		Integer actualFiles = Metadata.countImageFiles(outputDirectory);
		
		Assertions.assertEquals(expectedFiles, actualFiles);
		cleanup(outputDirectory);
	}
	/// Cleanup files and directories created during testing
	/**
	 * 
	 * @param outputDirectory
	 * @throws IOException
	 */
	public void cleanup(String outputDirectory) throws IOException {
		if (Files.exists(Paths.get(outputDirectory))) {
			File d = new File(outputDirectory);
			FileUtils.cleanDirectory(d); /// Warning! - Cleans directory!
			
			File D = new File(outputDirectory.substring(0,outputDirectory.length()-1));
			Files.delete(D.toPath());	/// Warning! - Removes root directory!
		}
	}
}
