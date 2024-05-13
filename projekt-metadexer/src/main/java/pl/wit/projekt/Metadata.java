package pl.wit.projekt;

/***
 * @author - Jan Konarski
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.codec.digest.*;

public class Metadata {
	
	/*
	 * TO DO:
	 * - Create private members for OutputDirectory
	 * - Create private members for OutputImage
	 * - Try to remove unnecessary variables
	 * 
	 * - After that's done, create a function to copy a file with getImageDestination(), getFolderDestination() getters.
	 * - Perform a loop to copy and paste files while Main Thread is active AND there is no concurrent thread currently active.
	 */
	private Set<String>fileHashcodeSet = new HashSet<String>();
	
	private String strOutputDirectoryPath=null;
	private String strOutputImagePath=null;
	//private static List<String> indexedImageList = new ArrayList<>();
	//private static List<Long> fileSizeList = new ArrayList<>();
	
	/***
	 * 
	 * @param jpegMetadata
	 * @param tagInfo
	 * @return
	 */
	/// Extract selected TiffField (EXIF data) from image file
	private static String getDateMetadata(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
		return (field.getValueDescription().substring(1, field.getValueDescription().length()-10).replace(':', '.'));		
	}
	/**
	 * 
	 * @param filename
	 * @return
	 */
	/// Get file extension (could do it with regex though..)
	public String getFileExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}
	/***
	 * 
	 * @param directory
	 * @throws ImagingException
	 * @throws IOException
	 * @throws InterruptedException 
	 * @throws NoSuchAlgorithmException 
	 */
	/// Scan for JPG or JPEG images in the parent directory and sub-directories - Directory is INPUT path
	public void discoverImages(String directory, Integer threadCount) throws IOException, InterruptedException, NoSuchAlgorithmException{
		// zapewnia utworzenie puli wątków do której można w miarę potrzeby dodawać kolejne wątki, a także ponownie wykorzystywać już istniejące.
		// Create new thread pool
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		Path rootDirectory = Paths.get(directory);
		
		try {
			Set<Path> pathSet = new HashSet<>();
			
			pathSet = Files.walk(rootDirectory)
					.filter(Files::isRegularFile)
					.collect(Collectors.toSet());
			// Use Set to select only unique dates!
			// This is important to make sure that we don't get duplicate output folders for sorting.
			
			for (Path path : pathSet) {
				
				if (getFileExtension(path.toString()).equals("jpg") || getFileExtension(path.toString()).equals("jpeg")) {
					executorService.execute(new WorkerThread(path, this));
					fileHashcodeSet.add(checksum(path));
				}
			}
			executorService.shutdown();
			while (!executorService.isTerminated()) {
                // Czekaj na zakończenie wszystkich wątków (do wywalenia najwyżej bo w sumie to nic nie robi)
            }
		}catch (IOException e) {
			e.printStackTrace();
		} 		
		//System.out.println("Zakończono przetwarzanie WSZYTSKICH obrazów."); 
	}
	
	public synchronized void processImage(Path path) throws IOException, ImagingException, NoSuchAlgorithmException {
		//System.out.println("Przetwarzanie obrazu: \t" + path.toString());
	    File file = new File(path.toString());
	    final ImageMetadata metadata = Imaging.getMetadata(file);
	    final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

	    if (jpegMetadata != null) {
			Integer imageIndex = 1;
			
			String localOutputDirectory = (getStrOutputDirectoryPath().concat(getDateMetadata(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)).concat("\\"));
			String localOutputImage = localOutputDirectory.concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString()));
			String previousFileHashcode = null;
			String previousFilePath = null;
			
			if (!Files.exists(Paths.get(localOutputDirectory))) {
				Files.createDirectories(Paths.get(localOutputDirectory));
			}
			while (Files.exists(Paths.get(localOutputImage))) {
				previousFilePath = localOutputDirectory.concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString()));
				++imageIndex;
				localOutputImage = localOutputDirectory.concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString()));
			}
			if (imageIndex > 1) {
				previousFileHashcode = checksum(Paths.get(previousFilePath));
			}
			if (!fileHashcodeSet.contains(previousFileHashcode)) {
				Files.copy(path, Paths.get(localOutputImage));
			}else
				System.out.println("File already exists: ".concat(localOutputImage));
			
			
		}
		//System.out.println("Zakończono przetwarzanie obrazu: " + path.toString());
	
}

	public String getStrOutputDirectoryPath() {
		return strOutputDirectoryPath;
	}
	public String getStrOutputImagePath() {
		return strOutputImagePath;
	}
	/*
	public static List<String> getIndexedImageList() {
		synchronized (lock){
			return indexedImageList;
		}
	}
	*/
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
}

