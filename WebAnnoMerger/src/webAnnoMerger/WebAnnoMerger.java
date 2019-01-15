package webAnnoMerger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebAnnoMerger {

	/**
	 * patterns to parse the .tsv webanno exports.
	 */
	private static final Pattern PATTERN_SENTENCE_INDEX = Pattern.compile("(\\d+-\\d+)");
	private static final Pattern PATTERN_SENTENCE_INDEX_DETAILED = Pattern.compile("(\\d+)(-\\d+)");
	private static final Pattern PATTERN_LABELS = Pattern.compile("\\[(\\d+)\\]");

	private static final Boolean DEBUG = false;

	private static StringBuilder webAnnoMetaInformation = new StringBuilder();

	public static void main(String[] args) throws Exception, FileNotFoundException {
		if (DEBUG)
			System.out.printf("%s is set up.\n", "webAnnoMerger");

		StringBuilder output = new StringBuilder();

		ArrayList<String> lines = mergeInputFiles(args);

		// 2. parse lines
		int globalEntryIndex = 0;
		Boolean flagNewLine = false;
		for (String line : lines) {
			// skip newline and # except #Text=
			if (line.isEmpty() || (line.startsWith("#") && !line.startsWith("#Text="))) {
				output.append(line + "\n");
				continue;
			} else if (line.startsWith("#Text=")) {
				output.append(line + "\n");
				globalEntryIndex++;
				continue;
			}

			// replace local index with global index
			String result = replaceLocalIndexWithGlobalIndex(line, globalEntryIndex);

			// replace local unique label with global unique label
			// result = replaceLocalUniqueLabelWithGlobalUniqueLabel(result,
			// globalEntryIndex);

			if (DEBUG)
				System.out.println(result);

			output.append(result + "\n");
		}

		// 3. generate output
		writeOutput(output.toString());
	}

	/**
	 * takes .jar args and treats them as file names. merges all files into array of
	 * strings. if args are missing, reads all .tsv from current directory.
	 * 
	 * @param args .jar args treated as file names
	 * @return all files merges into an array of strings
	 * @throws IOException
	 */
	private static ArrayList<String> mergeInputFiles(String[] args) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();

		if (args.length > 0) { // handle filenames handed over as parameters
			for (int i = 0; i < args.length; i++) {
				System.out.println(args[i]);
				File f = new File(args[i]);
				lines.addAll(readFile(f));
			}
		} else { // read all .tsv files from current directory, in case no arguments are provided
			File[] files = new File("./").listFiles();
			if (files == null) {
				System.err.println("No files found...");
			} else {
				for (File child : files) {
					if (child.isFile() && child.getName().endsWith(".tsv")) {
						System.out.println("read file " + child.getName());
						lines.addAll(readFile(child));
					}
				}
			}
		}

		return lines;
	}

	/**
	 * take line from webAnno export and turns all local labels, e.g. [222] to
	 * global unique labels like [x222].
	 * 
	 * @param line             of webAnno export with local unique labels, e.g.
	 *                         [222]
	 * @param globalEntryIndex global entry index for merged document.
	 * @return
	 */
	private static String replaceLocalUniqueLabelWithGlobalUniqueLabel(String line, int globalEntryIndex) {
		String result = line;

		if (DEBUG)
			System.out.print(" local_labels: ");

		Matcher labelsMatcher = PATTERN_LABELS.matcher(line);
		while (labelsMatcher.find()) {
			if (DEBUG)
				System.out.print(labelsMatcher.group(1) + " ");

			/*
			 * result = result.replaceFirst("\\[" + labelsMatcher.group(1) + "\\]", "\\[" +
			 * globalEntryIndex + "" + labelsMatcher.group(1) + "\\]");
			 */
			result = result.replaceFirst(String.format("\\[%s\\]", labelsMatcher.group(1)),
					String.format("\\[%d%s\\]", globalEntryIndex, labelsMatcher.group(1)));
		}
		if (DEBUG)
			System.out.println();
		return result;
	}

	/**
	 * takes line from webAnno export and changes local entry index to global entry
	 * index.
	 * 
	 * @param line             from webAnno export.
	 * @param globalEntryIndex global entry index for merged document.
	 * @return
	 */
	private static String replaceLocalIndexWithGlobalIndex(String line, int globalEntryIndex) {
		String result = line;

		Matcher indexMatcher = PATTERN_SENTENCE_INDEX.matcher(line);
		if (indexMatcher.find()) {
			if (DEBUG)
				System.out.printf("\nglobal_index: %d local_index: %s", globalEntryIndex, indexMatcher.group(1));

			Matcher detailedIndexMatcher = PATTERN_SENTENCE_INDEX_DETAILED.matcher(indexMatcher.group(1));
			if (detailedIndexMatcher.find()) {
				result = result.replaceFirst(indexMatcher.group(1),
						String.format("%d%s", globalEntryIndex, detailedIndexMatcher.group(2)));
			}
		}
		return result;
	}

	/**
	 * reads webAnno export and drops first lines being web anno meta info. this may
	 * vary for future versions of webanno.
	 * 
	 * @param f file
	 * @return
	 * @throws IOException
	 */
	private static ArrayList<String> readFile(File f) throws IOException {
		webAnnoMetaInformation = new StringBuilder(); // redirect meta information into separate variable

		ArrayList<String> lines = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
		String line;

		int lineIdx = 0;
		boolean firstNewLineFlag = false; // hack to avoid double new lines in output file. TODO: solve more elegantly ;)
		while ((line = br.readLine()) != null) {
			++lineIdx;

			if (line.startsWith("#T_SP=") || line.startsWith("#FORMAT=")) {
				webAnnoMetaInformation.append(line + "\n");
				continue;
			} 
			else if (line.isEmpty() && !firstNewLineFlag) {
				webAnnoMetaInformation.append("\n");
				firstNewLineFlag = true;
				continue;
			}

			if (DEBUG)
				System.out.printf("%d: %s\n", lineIdx, line);
			lines.add(line);

			/*
			 * if (id.isEmpty() || id.equals("_")) id = String.valueOf(corpusIDCounter);
			 */
		}

		br.close();
		return lines;
	}

	/**
	 * given output string, writes file to resources-folder with time stamp.
	 * 
	 * @param output
	 * @throws IOException
	 */
	private static void writeOutput(String output) throws IOException {
		// get time stamp for file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(timeStamp + "_webanno.tsv"), StandardCharsets.UTF_8));

		bw.write(webAnnoMetaInformation.toString());
		bw.write(output);
		bw.flush();
		bw.close();
	}

}
