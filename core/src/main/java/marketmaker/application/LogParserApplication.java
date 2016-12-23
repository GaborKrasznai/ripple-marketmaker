package marketmaker.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class LogParserApplication {

	public static void main(String[] args) throws Exception {

		String filename = "logFile.log";
		if (args.length > 0) {
			filename = args[0];
		}
		File file = new File(filename);
		if (!file.exists()) {
			throw new Exception("Arquivo nao existe. " + file.getAbsolutePath());
		}

		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		while (line != null) {
			if (line.indexOf(" - ") > 0) {
				String content = line.substring(line.indexOf(" - "));
				lines.add(content);
			} else {
				lines.add(line);
			}

			line = reader.readLine();
		}

		for (int x = 0; x < lines.size(); x++) {
			System.out.println(x + " " + lines.get(x));
		}

	}

}
