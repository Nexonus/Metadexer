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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.imaging.*;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

public class Metadata {
	
	private Set<String> imagePathSet = new HashSet<String>();
	private String outputRootPath = "";
	private String outputDirectoryPath = "";
	private String outputImagePath = "";
	private String fileExtension = "";
	private Set<String> indexedImageSet = new HashSet<String>();
	
	private Integer threadCount;
	
	/// FIX THE isEmpty fails on NULL string
	
	private static String getDateMetadata(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
		return (field.getValueDescription().toString().substring(1, field.getValueDescription().length()-10).replace(':', '.'));		
	}
	
	public void DiscoverImages(String searchDirectory) throws ImagingException, IOException{
		try {
			// Walk through all files in directories and subdirectories and add each JPG file to the set.
			imagePathSet = (Files.walk(Paths.get(searchDirectory)))
					.filter(p -> Files.isRegularFile(p))
					.map(p -> p.toString().toLowerCase())
					.filter(f -> f.endsWith("jpg"))
					.collect(Collectors.toSet());
			
			ExecutorService es = Executors.newFixedThreadPool(getThreadCount());
			
			for (String imagePath : imagePathSet) {
				
				ExtractImageMetadata(imagePath);
				setFileExtension("jpg");
				// Add support for other file types later on
				
				//Integer imageIndex = 1;
				
				es.execute(new ThreadWorker(imagePath));
				indexedImageSet.add(imagePath);

				//es.execute(new ThreadWorker(getOutputImagePath()));
				
				/// This doesn't work anymore... I have no idea how did i break it.
				/*
				String localOutputImage = getOutputDirectoryPath().concat(imageIndex.toString()).concat(".").concat(getFileExtension());
				while (Files.exists(Paths.get(localOutputImage))) {
					++imageIndex;
					localOutputImage = getOutputDirectoryPath().concat(imageIndex.toString()).concat(".").concat(getFileExtension());
				}
				System.out.println(localOutputImage);
				*/
			}
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	public void ExtractImageMetadata(String imagePath) throws ImagingException, IOException {
		File selectedFile = new File(imagePath);
		final ImageMetadata metadata = Imaging.getMetadata(selectedFile);
		final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		
		// Make sure the selected image actually has metadata. (For example generated images don't have EXIF fields).
		if (jpegMetadata != null) {			
			setOutputDirectoryPath(getOutputRootPath().concat(getDateMetadata(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)).concat("\\"));
			/// Create sub-directories in the output root Directory.: 2020.03.01, 2020.04.05 etc.
			if (!Files.exists(Paths.get(getOutputDirectoryPath()))) {
				Files.createDirectories(Paths.get(getOutputDirectoryPath()));
			}
			//Integer imageIndex = 1;
			//setOutputImagePath(getOutputDirectoryPath().concat(imageIndex.toString()).concat(".").concat(getFileExtension()));
		}
	}

	public Set<String> getImagePathSet() {
		return imagePathSet;
	}
	/// Destination of created directories
	public String getOutputDirectoryPath() {
		return outputDirectoryPath;
	}
	public void setOutputDirectoryPath(String outputDirectoryPath) {
		this.outputDirectoryPath = outputDirectoryPath;
	}
	/// Destination of copied images
	public String getOutputImagePath() {
		return outputImagePath;
	}
	public void setOutputImagePath(String outputImagePath) {
		this.outputImagePath = outputImagePath;
	}
	/// Destination of generated directories
	public String getOutputRootPath() {
		return outputRootPath;
	}
	public void setOutputRootPath(String outputRootPath) {
		this.outputRootPath = outputRootPath;
	}
	
	/// Local image file extension
	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}

	public Set<String> getIndexedImageSet() {
		return indexedImageSet;
	}
	
	
	/*
	 * TO DO:
	 * - Create private members for OutputDirectory
	 * - Create private members for OutputImage
	 * - Try to remove unnecessary variables
	 * 
	 * - After that's done, create a function to copy a file with getImageDestination(), getFolderDestination() getters.
	 * - Perform a loop to copy and paste files while Main Thread is active AND there is no concurrent thread currently active.
	 */
	
	/*
	private String strOutputDirectoryPath=null;
	private String strOutputImagePath=null;
	private List<String> indexedImageList = new ArrayList<String>();
	private List<Long> fileSizeList = new ArrayList<Long>();
	
	/// Extract selected TiffField (EXIF data) from image file
	private static String getDateMetadata(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
		return (field.getValueDescription().toString().substring(1, field.getValueDescription().length()-10).replace(':', '.'));		
	}
	
	/// Get file extension (could do it with regex though..)
	public String getFileExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}
	/// Scan for JPG or JPEG images in the parent directory and sub-directories - Directory is INPUT path
	public void DiscoverImages(String directory) throws ImagingException, IOException{
		
		Path rootDirectory = Paths.get(directory);
		
		try {
			Set<Path> pathSet = new HashSet<Path>();
			
			pathSet = Files.walk(rootDirectory)
					.filter(Files::isRegularFile)
					.collect(Collectors.toSet());
			// Use Set to select only unique dates!
			// This is important to make sure that we don't get duplicate output folders for sorting.
			
			for (Path path : pathSet) {
				
				if (getFileExtension(path.toString()).equals("jpg") || getFileExtension(path.toString()).equals("jpeg")) {
					File file = new File(path.toString());
					
					// Get metadata
					final ImageMetadata metadata = Imaging.getMetadata(file);
					final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
					
					// MAKE SURE, that the image actually contains TIFF fields, this is true for natural JPG files (done with a camera), but
					// this won't apply for artificially generated images, we're skipping these.
					
					if (jpegMetadata != null) {
						Integer imageIndex = 1;
						
						String localOutputDirectory = (getStrOutputDirectoryPath().concat(getDateMetadata(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)).concat("\\"));
						String localOutputImage = localOutputDirectory.concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString()));
						
						if (!Files.exists(Paths.get(localOutputDirectory))) {
							Files.createDirectories(Paths.get(localOutputDirectory));
						}
						while (Files.exists(Paths.get(localOutputImage))) {
							++imageIndex;
							localOutputImage = localOutputDirectory.concat(imageIndex.toString()).concat(".").concat(getFileExtension(path.toString()));
						}
						
						//Copy files, only if these weren't added yet. Check if there's any file with exact same size in bytes (length)
						
						File currentDirectory = new File(localOutputDirectory);
						File[] directoryFiles = currentDirectory.listFiles();
						
						if (directoryFiles != null) {
							for (File child : directoryFiles) {
								fileSizeList.add(child.length());
							}
						}
						if (!fileSizeList.contains(Files.size(path))) {
							indexedImageList.add(localOutputImage);
							Files.copy(path, Paths.get(localOutputImage));
						}
						System.out.println(localOutputImage);
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getStrOutputDirectoryPath() {
		return strOutputDirectoryPath;
	}
	public String getStrOutputImagePath() {
		return strOutputImagePath;
	}
	public List<String> getIndexedImageList() {
		return indexedImageList;
	}
	public void setStrOutputDirectoryPath(String strOutputDirectoryPath) {
		this.strOutputDirectoryPath = strOutputDirectoryPath;
	}
	*/
	
}

