package pl.wit.projekt;

/***
 * @author - Jan Konarski (Files and GUI), Artur Zakrzewski, Mateusz Bura (Threads and Processing image logic)
 * Pobieranie metadanych z obrazów JPG
 * Tworzenie katalogów docelowych
 * Kopiowanie plików JPG i ich indeksowanie
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

public class Metadata {
	
	private GUI gui;
	public Metadata(GUI gui) {
		this.gui = gui;
	}
	
	private Set<String>fileHashcodeSet = new HashSet<String>();
	
	private String strOutputDirectoryPath=null;
	private String strOutputImagePath=null;
	private Integer progressValue = 0;
	private Integer copiedFiles = 0;
	
	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	
	/// Extract selected TiffField (EXIF data) from image file
	private static String getDateMetadata(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
		return (field.getValueDescription().substring(1, field.getValueDescription().length()-10).replace(':', '.'));		
	}

	/// Get file extension using FilenameUtils
	public String getFileExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}
	
	/// Use this to extract hashcodes from existing files to prevent duplicate images.
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
	public void discoverImages(String directory, Integer threadCount) throws IOException, InterruptedException, NoSuchAlgorithmException{
		// zapewnia utworzenie puli wątków do której można w miarę potrzeby dodawać kolejne wątki, a także ponownie wykorzystywać już istniejące.
		// Create new thread pool
		progressValue = 0;
		this.executorService = Executors.newFixedThreadPool(threadCount);
		Path rootDirectory = Paths.get(directory);
		
		try {
			Set<Path> pathSet = new HashSet<>();
			pathSet = Files.walk(rootDirectory)
					.filter(Files::isRegularFile)
					.collect(Collectors.toSet());
			// Use Collectors to filter out only regular files from Input Root Folder
			
			/// Select JPG or JPEG files for input source path, give work to ExecutorService to process input. (Get rid of non JPG's)
			scanDirectories(getStrOutputDirectoryPath()); // single-thread scan of output dir
			for (Path path : pathSet) {
				if (getFileExtension(path.toString()).equals("jpg") || getFileExtension(path.toString()).equals("jpeg")) {
					executorService.execute(new WorkerThread(path, this));	// multi-thread processing
				}
				else {
					gui.notifyError(path.toString());
			    	++progressValue;
			    }
				System.out.println(executorService.toString());
			}
			executorService.shutdown();
			while (!executorService.isTerminated()) {}
			
			System.out.println(executorService.toString());
			System.out.println("Finished work on all threads.");
			
		}catch (IOException e) {
			e.printStackTrace();
		} 		
		gui.notifyCopiedFiles(copiedFiles);
		//System.out.println("Zakończono przetwarzanie WSZYTSKICH obrazów."); //dbg
	}
	
	/***
	 * 
	 * @param path
	 * @throws IOException
	 * @throws ImagingException
	 * @throws NoSuchAlgorithmException
	 */
	// Przetwarzanie obrazu, ekstrakcja metadanych, nazwanie katalogów docelowych oraz pliku docelowego
	
	public synchronized void processImage(Path path) throws IOException, ImagingException, NoSuchAlgorithmException {
		//System.out.println("Przetwarzanie obrazu: \t" + path.toString());
	    File file = new File(path.toString());
	    final ImageMetadata metadata = Imaging.getMetadata(file);
	    final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
	    boolean dupe = false;

	    if (jpegMetadata != null) {
			Integer imageIndex = 1;
			
			String localOutputDirectory = (getStrOutputDirectoryPath().concat(getDateMetadata(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)).concat("\\"));
			String localOutputImage = localOutputDirectory.concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString()));
			String previousFilePath = localOutputImage;
			
			if (!Files.exists(Paths.get(localOutputDirectory))) {
				Files.createDirectories(Paths.get(localOutputDirectory));
			}
	
			while (Files.exists(Paths.get(localOutputImage))) {
				previousFilePath = localOutputDirectory.concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString()));
				++imageIndex;
				localOutputImage = localOutputDirectory.concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString()));
			}
			setStrOutputImagePath(localOutputImage);
			if (Files.exists(Paths.get(previousFilePath))) {
				// Create a new file *if it's not a duplicate!*
				if (!fileHashcodeSet.contains(checksum(Paths.get(previousFilePath)))) {
					Files.copy(path, Paths.get(localOutputImage));
					++copiedFiles;
				}else {
					dupe = true;
					gui.notifyDuplicate(path.toString());
				}
			}else
			{
				// Create a new file, if it wasn't created yet.
				Files.copy(path, Paths.get(localOutputImage));
				++copiedFiles;
			}
				
			
			++progressValue;
			if (!dupe) {
				gui.notifyCopied(path.toString());
				gui.setProgressValue(progressValue);
			}
			dupe = false;
		}
	    else {
	    	gui.notifyError(path.toString());
	    	++progressValue;
	    }
		//System.out.println("Zakończono przetwarzanie obrazu: " + path.toString());
}

	public String getStrOutputDirectoryPath() {
		return strOutputDirectoryPath;
	}
	public String getStrOutputImagePath() {
		return strOutputImagePath;
	}
	public void setStrOutputDirectoryPath(String strOutputDirectoryPath) {
		this.strOutputDirectoryPath = strOutputDirectoryPath;
	}
	
	public Integer countRegularFiles(String inputPath) throws IOException {
			List<Path>pathList = new ArrayList<Path>();
			pathList = Files.walk(Paths.get(inputPath))
						.filter(Files::isRegularFile)
						.collect(Collectors.toList());
			return pathList.size();
	}
	
	private static String checksum(Path pathA) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(Files.readAllBytes(pathA));
		byte[] byteDigest = md.digest();
		String md5HexA = DigestUtils.md5Hex(byteDigest).toUpperCase();
		return md5HexA;
	}

	public void setStrOutputImagePath(String strOutputImagePath) {
		this.strOutputImagePath = strOutputImagePath;
	}
	/*
	protected void shutdown() {
		this.thread.setInterrupted(true);
	}
	private boolean getShutdown() {
		return this.thread.interrupted;
	}*/
}

