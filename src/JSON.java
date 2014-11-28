import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//String: sentence
//Two arrays: predicate - abstract predicate
//String: semi sentence


public class JSON {
	@SuppressWarnings("unchecked")
	public void addToJSON(String sentence, ArrayList<String> arrayWords, ArrayList<String> arrayTags, String quasiSentence) throws IOException{
        JSONObject dataStorage = new JSONObject();

        int counter = 2;
        
        dataStorage.put("Sentence", sentence);
        dataStorage.put("Quasi-Sentence", quasiSentence);
        dataStorage.put("ArrayWords", arrayWords);
        dataStorage.put("ArrayTags", arrayTags);
        
        System.out.println(dataStorage);
		 
		FileWriter file = new FileWriter("C:\\Users\\NLP\\Entries\\Entry" + counter + ".json");
		try {
			file.write(dataStorage.toJSONString());
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + dataStorage);
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			file.flush();
			file.close();
		}
        }
	}