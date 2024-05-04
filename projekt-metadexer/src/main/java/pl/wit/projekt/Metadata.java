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
import java.util.HashSet;
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
	
	public String getFileExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}
	/***
	 * 
	 * @param jpegMetadata
	 * @param tagInfo
	 * @return
	 */
	private static String getDateMetadata(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo) {
		final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
		return (field.getValueDescription().toString().substring(1, field.getValueDescription().length()-10).replace(':', '.'));		
	}
	/***
	 * 
	 * @param directory
	 * @throws ImagingException
	 * @throws IOException
	 */
	public void DiscoverImages(String directory) throws ImagingException, IOException{
		
		Path rootDirectory = Paths.get(directory);
		
		try {
			Set<Path> pathSet = new HashSet<Path>();
			
			pathSet = Files.walk(rootDirectory)
					.filter(Files::isRegularFile)
					.collect(Collectors.toSet());
			// Use Set to select only unique dates!
			
			
			for (Path path : pathSet) {
				if (getFileExtension(path.toString()).equals("jpg") || getFileExtension(path.toString()).equals("jpeg")) {
					File file = new File(path.toString());
					String outputPath = new String();
					String outputImagePath = new String();
					
					final ImageMetadata metadata = Imaging.getMetadata(file);
					final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
					
					if (jpegMetadata != null) {
						Integer index = 0;
						outputPath = "..\\output\\".concat(getDateMetadata(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)).concat("\\");
						if (!Files.exists(Paths.get(outputPath))) {
							Files.createDirectories(Paths.get(outputPath));
							//System.out.println("Creating Directory: ".concat(outputPath));
							/// Debug msg
						}
						while (Files.exists(Paths.get(outputImagePath))) {
							++index;
							outputImagePath = outputPath.concat(index.toString().concat(".").concat(getFileExtension(path.toString())));
						}
						System.out.println(outputImagePath);
						Files.copy(path, Paths.get(outputImagePath));
					
						//Files.copy(path, previousFilePath, StandardCopyOption.REPLACE_EXISTING);
						
						/// TO DO: Replace existing files!
						
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}
