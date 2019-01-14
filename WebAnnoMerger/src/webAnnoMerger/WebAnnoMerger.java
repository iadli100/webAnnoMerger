package webAnnoMerger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebAnnoMerger {
	
	private static String LINE = "1-1\t...\t[111]\t[222][333]\n";
	
	public static void main(String[] args){
		System.out.printf("%s is set up.\n", "webAnnoMerger");
		
		// Pattern p = Pattern.compile("{\\d-\\d}");
		Pattern index = Pattern.compile("(\\d-\\d)");
		Pattern labels = Pattern.compile("\\[(\\d+)\\]"); 
		
        Matcher matcher = labels.matcher(LINE);
        
        // check all occurrence
        while (matcher.find()) {
        	System.out.println(matcher.group(1));
        	System.out.println("replacement: " + 
        			matcher.group(1).replaceAll(matcher.group(1), "i"+matcher.group(1)));
        	String result = LINE.replaceAll(matcher.group(1), "i"+matcher.group(1));
        	System.out.println(result);
            System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end() + " ");
            System.out.println(matcher.group());
        }
        
        // now create a new pattern and matcher to replace whitespace with tabs
        /*
        Pattern replace = Pattern.compile("\\s+");
        Matcher matcher2 = replace.matcher(LINE);
        System.out.println(matcher2.replaceAll("\t"));
        // */
	}
	
	
}
