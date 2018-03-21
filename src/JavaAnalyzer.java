/**
 * The JavaAnalyzer parses java source code files by keeping a list of variables in use.
 *
 * @author Joseph Antaki
 * @author Abby Beizer
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

public class JavaAnalyzer extends Analyzer {
	private Set<String> variablesList;

    /**
     * The main constructor. Initializes list of variables.
     */
	public JavaAnalyzer() {
		super();
		//instantiate map of variables
		variablesList = new HashSet<>();
	}

    /**
     * Pads non-alphanumeric characters with spaces to make the code easier to work with
     * @param s the line of code to format
     * @return the formatted line of code
     */
	public String flattenCode(String s)
	{	
		String finalString = s.trim().replaceAll("\n", "").replaceAll("\t","").replaceAll("\r", "").replaceAll("\f", "");
		//Separate all special characters
        finalString = finalString.replace("=", " = ");
        finalString = finalString.replace("(", " ( ");
        finalString = finalString.replace(")", " ) ");
        finalString = finalString.replace(";", " ; ");
        finalString = finalString.replace("{", " { ");
        finalString = finalString.replace("}", " } ");
        finalString = finalString.replace("[", " [ ");
        finalString = finalString.replace("]", " ] ");
        finalString = finalString.replace(":", " : ");
        finalString = finalString.replace(":", " : ");
        finalString = finalString.replace("\"", " \" ");
        finalString = finalString.replace(",", " , ");
		//Create an array of all words separated by a space
		String words[] = finalString.split(" ");
		//Transform the array into an ArrayList
		ArrayList<String> list = new ArrayList<>(Arrays.asList(words));
		Iterator<String> itty = list.iterator();
		String result="";
		while(itty.hasNext()) {
			String word = itty.next();
			//Remove any blank words in the ArrayList
			if(!word.equals(""))
			{
				result+=word+" ";
			}
		}
		return result.trim();
	}

    /**
     * Adds all variable names of a given type to the list of variables
     * @param arr An array of string tokens that make up a line of code
     * @param type The type of variable to pick out
     */
	private void searchForType(String[] arr, String type) {
        for (int i = 0; i < arr.length - 2; i++) {
            if (arr[i].equals(type) && !arr[i+2].equals("(")) {
                if(arr[i+1].equals("[")) {
                    variablesList.add(arr[i+3]);
                }
                else {
                    variablesList.add(arr[i + 1]);
                }
            }
        }
    }

    /**
     * Extracts variable names from a line of code
     * @param s The line of code to extract variables from
     */
    public void extractVariables(String s)
    {
    	s = flattenCode(s);
    	
        //Create an array of all words separated by a space
        String words[] = s.split(" ");
        //extract String and primitive variables
        searchForType(words, "String");
        searchForType(words, "boolean");
        searchForType(words, "byte");
        searchForType(words, "char");
        searchForType(words, "short");
        searchForType(words, "int");
        searchForType(words, "long");
        searchForType(words, "float");
        searchForType(words, "double");
        //extract all other variables
        for (int i = 2; i < words.length; i++) {
            if((!words[i-1].equals(")"))&&(words[i].equals("=") || words[i].equals(";")) && Character.isUpperCase(words[i-2].charAt(0))){
                variablesList.add(words[i-1]);
            }
        }
        //Transform the array into an ArrayList
        ArrayList<String> list = new ArrayList<>(Arrays.asList(words));
        //start iterator
        Iterator<String> itty = list.iterator();
        //set a switch keep track of when we are in a string
        boolean stringSwitch = false;
        //the following loop removes strings, it is unfortunate but in order to detect variables strings must not be present
        while(itty.hasNext())
        {
            String word = itty.next();

            //if we run into the beginning of a string we turn the switch on and remove the first character, this gets activated again at the end of the switch
            if(word.equals("\""))
            {
                stringSwitch = !stringSwitch;
                itty.remove();
            }
            //while inside of a string we remove it
            else if(stringSwitch && !word.equals("\""))
            {
                itty.remove();
            }

        }
        //put words back into an array
        words = list.toArray(new String[list.size()]);
    }

	@Override
	/*
	 * Removes comments and extracts variables from source code.
	 * @param filename the file to be parsed
	 */
	public void parse(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			String[] cleanLine=null;
			boolean mComment = false;    //whether the current line is a continuation of a multi-line comment

			//Runs until end of file
			while ((line = br.readLine()) != null) {

				/* First, remove any comments from the line */

				//If the previous line began or continued a multi-line comment
				if (mComment) {
					//If the current line terminates the comment,
					//remove all text before the terminating character
					//and set flag to false
					if (line.contains("*/")) {
						line = line.replaceAll("^.*\\*/", "");
						mComment = false;
					}
					//if the current line does not terminate the comment,
					//then it continues to the next line
					else {
						continue;
					}
				}

				//removes single-line comments in the format "/* --- */"
				line = line.replaceAll("/\\*.*\\*/", "");
				//removes single-line comments in the format "//"
				line = line.replaceAll("//.*$", "");

				//removes all characters following "/*" and flags the next line as a continuation of a multi-line comment
				if (line.contains("/*")) {
					line = line.replaceAll("/\\*.*$", "");
					mComment = true;
				}

				//After removal of comments, process remaining characters

				//ignore lines that are blank
				if (line.length() == 0) {
					continue;
				}

				/* Second, extract the variables: */

				extractVariables(line);

			}
            }
			catch(FileNotFoundException fnf){	//from FileReader
				SIT.notifyUser("Error: File " + filename + " could not be parsed.");
			}
			catch(IOException io){	//from BufferedReader
				SIT.notifyUser("Error reading the contents of " + filename + "." );
			}
            System.out.println(variablesList);
	}



	@Override
    /**
     * Analyzes a java source code file for vulnerabilities,
     * @param filename The name of the file to be analyzed
     */
	protected void analyze(String filename) {
		parse(filename);
	}

}
