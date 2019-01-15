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
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebAnnoMerger {

	private static String LINE = "1-1\t...\t[111]\t[222][333]\n";

	/**
	 * patterns to parse the .tsv webanno exports.
	 */
		private static final Pattern PATTERN_SENTENCE_INDEX = Pattern.compile("(\\d+-\\d+)");
		private static final Pattern PATTERN_LABELS = Pattern.compile("\\[(\\d+)\\]");

	public static void main(String[] args) throws Exception, FileNotFoundException {
		System.out.printf("%s is set up.\n", "webAnnoMerger");

		StringBuilder output = new StringBuilder();

		// 1. read file
		File f = new File("resources/dwds_export_Aussage_steht_1.tsv");
		File f2 = new File("resources/dwds_export_Aussage_steht_2.tsv");

		ArrayList<String> lines = readFile(f);
		lines.addAll(readFile(f2));
		

		// 2. parse lines
		int globalEntryIndex = 0;
		for(String line : lines) {
			// skip newline and # except #Text=
			if (line.isEmpty() || (line.startsWith("#") && !line.startsWith("#Text="))) {
				// 
				// TODO: add unprocessed line to global output file
				//
				continue;
			} 
			else if (line.startsWith("#Text=")) {
				// 
				// TODO: add unprocessed line to global output file
				//
				globalEntryIndex++;
			}
			
			// replace local index with global index
			Matcher indexMatcher = PATTERN_SENTENCE_INDEX.matcher(line);
			if (indexMatcher.find()) {
				System.out.printf("%d: %s\n", globalEntryIndex, indexMatcher.group(1));
			}
			
			
			// replace local unique label with global unique label
			Matcher labelsMatcher = PATTERN_LABELS.matcher(line);
			String result = line;
			while (labelsMatcher.find()) {
				System.out.print(labelsMatcher.group(1) + " ");

				// result = result.replaceFirst(labelsMatcher.group(1), globalEntryIndex + "" + labelsMatcher.group(1));
				// result = result.replaceAll(labelsMatcher.group(1), globalEntryIndex + "" + labelsMatcher.group(1)); // tweaking the right pattern

				// result = result.replaceFirst("\\[" + labelsMatcher.group(1) + "\\]", "\\[" + globalEntryIndex + "" + labelsMatcher.group(1) + "\\]");
				result = result.replaceFirst(String.format("\\[%s\\]", labelsMatcher.group(1)), String.format("\\[%d%s\\]", globalEntryIndex, labelsMatcher.group(1)));
			}
			System.out.println();
			System.out.println(result);
			
			
			/*
			Pattern index = Pattern.compile("(\\d-\\d)");
			Pattern labels = Pattern.compile("\\[(\\d+)\\]");

			Matcher matcher = labels.matcher(LINE);

			while (matcher.find()) {
				System.out.println(matcher.group(1));
				System.out.println("replacement: " + matcher.group(1).replaceAll(matcher.group(1), "i" + matcher.group(1)));
				String result = LINE.replaceAll(matcher.group(1), "i" + matcher.group(1));
				System.out.println(result);
				System.out.print("Start index: " + matcher.start());
				System.out.print(" End index: " + matcher.end() + " ");
				System.out.println(matcher.group());
			}
			// */
			
		}


		// 3. generate output
		writeOutput(output.toString());

		// now create a new pattern and matcher to replace whitespace with tabs
		/*
		 * Pattern replace = Pattern.compile("\\s+"); Matcher matcher2 =
		 * replace.matcher(LINE); System.out.println(matcher2.replaceAll("\t")); //
		 */
	}

	private static ArrayList<String> readFile(File f) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
		String line;

		int lineIdx = 0;
		int corpusIDCounter = 0;
		while ((line = br.readLine()) != null) {
			++lineIdx;

			System.out.printf("%d: %s\n", lineIdx, line);
			lines.add(line);

			line = line.trim();
			if (!line.isEmpty() && !line.startsWith("#")) {
				++corpusIDCounter;

				// String[] entries = PATTERN_SEARCH_TAB.split(line);

				// if (entries.length != 6) {

				// String timestamp = entries[1].trim();

				// if (timeStampMatcher.find()) {

				// timestamp = timeStampMatcher.group(3) + "." + timeStampMatcher.group(2) + "."
				// + timeStampMatcher.group(1);

				// if (id.isEmpty() || id.equals("_"))

				// id = String.valueOf(corpusIDCounter);
			}

		}
		return lines;
	}

	/**
	 * given output string, writes file to resources-folder with time stamp.
	 * 
	 * @param output
	 * @throws IOException
	 */
	private static void writeOutput(String output) throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("resources/" + timeStamp + "_webanno.tsv"), StandardCharsets.UTF_8));
		bw.write(output.toString());
		bw.flush();
		bw.close();
	}

}
