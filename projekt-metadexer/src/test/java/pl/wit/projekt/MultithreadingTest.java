package pl.wit.projekt;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
 * JUnit tests
 */

public class MultithreadingTest {
public Integer countRegularFiles(String inputPath) throws IOException {
			
		List<Path>pathList = new ArrayList<Path>();
		
		pathList = Files.walk(Paths.get(inputPath))
					.filter(Files::isRegularFile)
					.collect(Collectors.toList());
		return pathList.size();
	}
		
	@Test
	public void copyFilesTest() throws IOException {
		
		String outputDirectory = "D:\\My Computer\\OneDrive\\Mój Komputer\\Pulpit\\Sort Output";
		String inputDirectory = "D:\\My Computer\\OneDrive\\Mój Komputer\\Pulpit\\Sort input";
		Integer filesExpectedCount = countRegularFiles(inputDirectory);
		
		Assertions.assertEquals(filesExpectedCount, countRegularFiles(outputDirectory));
	}
	
}
