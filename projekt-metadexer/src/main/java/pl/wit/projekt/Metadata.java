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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.imaging.*;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.io.FilenameUtils;

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
	private String strOutputDirectoryPath=null;
	private String strOutputImagePath=null;
	private List<String> indexedImageList = new ArrayList<String>();
	private List<Long> fileSizeList = new ArrayList<Long>();
	
	/***
	 * 
	 * @param jpegMetadata
	 * @param tagInfo
	 * @return
	 */
	/// Extract selected TiffField (EXIF data) from image file
	private static String getDateMetadata(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
		return (field.getValueDescription().toString().substring(1, field.getValueDescription().length()-10).replace(':', '.'));		
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
	 */
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
						
						/***
						 * Copy files, only if these weren't added yet. Check if there's any file with exact same size in bytes (length)
						 */
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
	
}

