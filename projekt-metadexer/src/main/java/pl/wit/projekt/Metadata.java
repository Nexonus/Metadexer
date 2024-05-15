package pl.wit.projekt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.imaging.*;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.codec.digest.*;

/***
 * @author Jan Konarski (File copying and indexing, GUI notifications, Multi-threading synchronization) 
 * @author Artur Zakrzewski (Threads and processing image logic)
 * @author Mateusz Bura (Threads and processing image logic)
 * 
 */
public class Metadata {
	
	private GUI gui;
	public Metadata(GUI gui) {
		this.gui = gui;
	}
	
	private Set<String>fileHashcodeSet = new HashSet<String>();
	private String rootOutputDirectory=null;
	private Integer copiedFiles = 0;
	protected Map<String, File>directoryMap = new HashMap<String, File>();		// Create a map of image hashes MD5
	private ExecutorService executorService = Executors.newFixedThreadPool(1);	// By default the thread pool size is 1 for single-thread processing.
	
	/// Extract selected TiffField (EXIF data) from image file
	/**
	 * 
	 * @param jpegMetadata
	 * @param tagInfo
	 * @return
	 */
	private static String getDateMetadata(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
		return (field.getValueDescription().substring(1, field.getValueDescription().length()-10).replace(':', '.'));		
	}

	/// Get file extension using FilenameUtils, could be done with substring or regex but I thought it'd be quicker this way.
	/**
	 * 
	 * @param filename
	 * @return
	 */
	public static String getFileExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}
	// Check if file has acceptable extension.
	/**
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean validImageFile(String filename) {
		switch(getFileExtension(filename)) {
			case "jpg":{
				return true;
			}
			case "jpeg":{
				return true;
			}
			default:{
				return false;
			}
		}
		// Add potentially other file formats in the future, leaving this as a switch.
	}
	
	/// Use this to extract hashcodes from existing files to prevent duplicate images.
	/**
	 * 
	 * @param directory
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public void scanDirectories(String directory) throws IOException, NoSuchAlgorithmException {
		Set<Path> pathSet = new HashSet<>();
		pathSet = Files.walk(Paths.get(directory))
				.filter(Files::isRegularFile)
				.collect(Collectors.toSet());
		for (Path path : pathSet) {
			fileHashcodeSet.add(checksum(path));
		}
	}
	/// Scan for JPG or JPEG images in the parent directory and sub-directories - Directory is INPUT path
	/**
	 * 
	 * @param directory
	 * @param threadCount
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws NoSuchAlgorithmException
	 */
	public void discoverImages(String directory, Integer threadCount) throws IOException, InterruptedException, NoSuchAlgorithmException{

		// Create new thread pool of a fixed size
		this.executorService = Executors.newFixedThreadPool(threadCount);
		Path rootDirectory = Paths.get(directory);
		
		try {
			Set<Path> pathSet = new HashSet<>();
			pathSet = Files.walk(rootDirectory)
					.filter(Files::isRegularFile)
					.collect(Collectors.toSet());
			// Use Collectors to filter out only regular files from Input Root Folder - we're searching for all non-directories that we'll add to the set.
			
			/// Select JPG or JPEG files for input source path, give work to ExecutorService to process input. (Get rid of non JPG's)
			scanDirectories(getRootOutputDirectoryPath()); // Scan directories for duplicate files, single-thread.
			for (Path path : pathSet) {
				if (getFileExtension(path.toString()).equals("jpg") || getFileExtension(path.toString()).equals("jpeg")) {
					executorService.execute(new WorkerThread(path, this));	// Begin multi-threading processing for a given ThreadPool Size
				}
				else {
					gui.notifyError(path.toString());
			    }
				System.out.println(executorService.toString());
			}
			executorService.shutdown();
			while (!executorService.isTerminated()) {}
			
			//System.out.println(executorService.toString());
			//System.out.println("Finished work on all threads.");	// debug
			
		}catch (IOException e) {
			e.printStackTrace();
		} 		
		gui.notifyCopiedFiles(copiedFiles);	// Send a notification to the Output Log about amount of copied files.
	}
	/// Image Processing:
	/*
	 * - Check if image has metadata
	 * - If it does, create or link it to an output subdirectory eg. 2024.05.03/ <- put image here
	 * - If directory exists, check if there's files inside
	 * - If there are, increment image index 1.jpg -> 2.jpg
	 * - Try to save the file in the output subdirectory, if there's no exact same copy of it already present (MD5 hash)
	 */
	/**
	 * 
	 * @param path
	 * @throws IOException
	 * @throws ImagingException
	 * @throws NoSuchAlgorithmException
	 */
	public void processImage(Path path) throws IOException, ImagingException, NoSuchAlgorithmException {
	    File file = new File(path.toString());
	    final ImageMetadata metadata = Imaging.getMetadata(file);
	    final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
	    
	    if (jpegMetadata != null) {
			Integer imageIndex = 1;	// Create an index for copied image files, 1.jpg, 2.jpg ...
			
			String exifDate = getDateMetadata(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			String localOutputDirectory = null;
			String localOutputFile = null;
			String previousOutputFile = null;

			File directory = null;
			
			/// SYNCHRONIZED BLOCK 1 - SUPER IMPORTANT FOR MULTI-THREADING, ALLOW ONLY ONE THREAD TO WORK IN A CURRENT DIRECTORY, OR MAKE A NEW ONE IF IT DOESN'T EXIST YET (Resolved potential concurrent access error)
			synchronized(directoryMap) {
				if (directoryMap.containsKey(exifDate)) {
					directory = directoryMap.get(exifDate);}
				else {
					localOutputDirectory = (getRootOutputDirectoryPath().concat(exifDate).concat("\\"));
					directory = new File(localOutputDirectory);
					directory.mkdirs();
					directoryMap.put(exifDate, directory);}
			}
			/// SYNCHRONIZED BLOCK 2 - SUPER IMPORTANT FOR MULTI-THREADING, ALLOW ONLY ONE THREAD TO SET OUTPUT FILE NAME AND CREATE A FILE! (Resolved concurrency error)
			synchronized(directory) {
				if (directory != null) {
					localOutputFile = directory.toString().concat("\\").concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString())); 
					while (Files.exists(Paths.get(localOutputFile)) || localOutputFile == null) {
						previousOutputFile = localOutputFile;
						localOutputFile = directory.toString().concat("\\").concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString())); 
						++imageIndex;
					}
					if (imageIndex == 1 && previousOutputFile == null) {
						previousOutputFile = localOutputFile;
					}
					
					if (!Files.exists(Paths.get(previousOutputFile))) {
						Files.copy(path, Paths.get(localOutputFile));
						gui.notifyCopied(path.toString());
						++copiedFiles;
					}
					else {
						if (!fileHashcodeSet.contains(checksum(Paths.get(previousOutputFile)))) {
							Files.copy(path, Paths.get(localOutputFile));
							gui.notifyCopied(path.toString());
							++copiedFiles;
							}
						else {
							gui.notifyDuplicate(path.toString());	// Report duplicate file warning
						}
					}
					System.out.println(previousOutputFile + " " + imageIndex + localOutputFile);
			}
		}
	}else {
		gui.notifyNoMetadata(path.toString());	// Report no metadata warning
	}
}
	/**
	 * 
	 * @return
	 */
	public String getRootOutputDirectoryPath() {
		return rootOutputDirectory;
	}
	// Set root for output directory, that's where the folders will be created in later on
	/**
	 * 
	 * @param strOutputDirectoryPath
	 */
	public void setRootOutputDirectory(String strOutputDirectoryPath) {
		this.rootOutputDirectory = strOutputDirectoryPath;
	}
	// Count regular image files, JPG, JPEG allowed.
	/**
	 * 
	 * @param inputPath
	 * @return
	 * @throws IOException
	 */
	public static Integer countImageFiles(String inputPath) throws IOException {
			List<Path>pathList = new ArrayList<Path>();
			pathList = Files.walk(Paths.get(inputPath))
						.filter(Files::isRegularFile)
						.filter(p -> validImageFile(p.toString()))
						.collect(Collectors.toList());
			return pathList.size();
	}
	/// Generate MD5 checksum for file of pathA - Used to make sure there's no two files with exact same hash in the output directory.
	/***
	 * 
	 * @param pathA
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static String checksum(Path pathA) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(Files.readAllBytes(pathA));
		byte[] byteDigest = md.digest();
		String md5HexA = DigestUtils.md5Hex(byteDigest).toUpperCase();
		return md5HexA;
	}

	public Set<String> getFileHashcodeSet() {
		return fileHashcodeSet;
	}

}

