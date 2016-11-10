package cifo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class Writer {

	String fileName = "";
	protected String directoryName = "C:/CIFO_results/";
	protected String fileExtension = ".csv";

	Charset utf8 = StandardCharsets.UTF_8;
	ArrayList<String> lines = new ArrayList<String>();
	// byte[] data = {1, 2, 3, 4, 5};

	public void printLineToFile(String nGenerationResult) {
		ArrayList<String> line = new ArrayList<String>();
		line.add(nGenerationResult);
		try {

			File directory = new File(directoryName);
			if (!directory.exists()) {
				directory.mkdir();
			}

			Files.write(Paths.get(fileName), line, utf8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setFileName(String filename) {
		fileName = directoryName + filename + fileExtension;
	}
}