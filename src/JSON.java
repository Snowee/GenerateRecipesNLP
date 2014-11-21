import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONObject;

//String: sentence
//Two arrays: predicate - abstract predicate
//String: semi sentence

public class JSON {
	public void addToJSON(String sentence, ArrayList<String> one, ArrayList<String> two, String quasiSentence) throws IOException{
        JSONObject dataStorage = new JSONObject();

        for(int i = 0; i<10; i++){
        
		dataStorage.put("Sentence:", sentence);
		dataStorage.put("Quasi-sentence:", quasiSentence);
		dataStorage.put("ArrayTags:", one);
		dataStorage.put("ArrayWords:", two);
		 
		FileWriter file = new FileWriter("C:\\Users\\NLP\\dataStorage.json", true);
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
}
