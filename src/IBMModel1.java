import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class IBMModel1 {
	
	public void EMAlgorithm() throws IOException{
		int s = 0;
		String[][] sentenceEntries = createEntries();
		
		HashMap<ArrayList<String>, Double> translationProbs = new HashMap<ArrayList<String>, Double>();
		HashMap<String, Double> quasiWordsCount = new HashMap<String, Double>();
		HashMap<ArrayList<String>, Double> wordPairCount = new HashMap<ArrayList<String>, Double>();
		
		//Preprocess sentences to gain access on word level.
		for(int i = 0; i < sentenceEntries.length ; i++){
			String realSentence = sentenceEntries[i][0];
			String quasiSentence = sentenceEntries[i][1];
			
			String[] wordsRealSentence = realSentence.split("\\s+");
			String[] wordsQuasiSentence = quasiSentence.split("\\s+");
				
			for(int j = 0; j < wordsQuasiSentence.length; j++){	
				quasiWordsCount.put(wordsQuasiSentence[j], 0.0);
				
				for(int q = 0; q < wordsRealSentence.length; q++){				
					ArrayList<String> realQuasiPair = new ArrayList<String>();
					realQuasiPair.add(wordsRealSentence[q]);
					realQuasiPair.add(wordsQuasiSentence[j]);
					
					translationProbs.put(realQuasiPair, 0.25);
					wordPairCount.put(realQuasiPair, 0.0);
				}
			}
		}
		
		/*
		for (Entry<ArrayList<String>, Double> entry : wordPairCount.entrySet()) {
		    System.out.println(entry.getKey()+" : "+entry.getValue());
		}
				
		
		for (Entry<ArrayList<String>, Double> entry : translationProbs.entrySet()) {
		    System.out.println(entry.getKey()+" : "+entry.getValue());
		}
		 */
		
		do{
			for (Entry<ArrayList<String>, Double> wordPair : wordPairCount.entrySet()) {
			    wordPairCount.put(wordPair.getKey(), 0.0);
			}
			for(Entry<String, Double> quasiWord : quasiWordsCount.entrySet()){
				quasiWordsCount.put(quasiWord.getKey(), 0.0);
			}
			
			// Loop through all sentences.
			for(int i = 0 ; i < sentenceEntries.length; i++){
				// Get the real sentence - quasi sentence pair
				String[] wordsRealSentence2 = sentenceEntries[i][0].split("\\s+");
				String[] wordsQuasiSentence2 = sentenceEntries[i][1].split("\\s+");
				
				// Create a hashmap to be used for the variable s-Total(e)
				HashMap<String, Double> sTotal_e = new HashMap<String, Double>();
				
				ArrayList<String> wordPair = new ArrayList<String>();

				
				// Loop through all words from the real sentence
				for(int j = 0 ; j < wordsRealSentence2.length; j++){
					
					// Set all s-total values on 0 for all words in the real sentence
					sTotal_e.put(wordsRealSentence2[j], 0.0);
					
					// Loop through all quasi words
					for(int q = 0; q < wordsQuasiSentence2.length; q++){
						
						// Create an arraylist with the current real - quasi word pair.
						wordPair.add(wordsRealSentence2[j]);
						wordPair.add(wordsQuasiSentence2[q]);
						
						// Get the translation probability from the translation probability hashmap with the 
						// real - quasi word pair.
						//System.out.println(wordsRealSentence2[j]);
						//System.out.println(wordsQuasiSentence2[q]);
						double translationProbValue = translationProbs.get(wordPair);
						
						// Alter the s-total with the translation probability.
						sTotal_e.put(wordsRealSentence2[j], sTotal_e.get(wordsRealSentence2[j]) + translationProbValue);
						/*
						for (Entry<String, Double> entry : sTotal_e.entrySet()) {
							System.out.println(entry.getKey()+" : "+entry.getValue());
						}
						System.out.println("For loopje door");
						System.out.println(); 
						*/
						wordPair.clear();
					}
				}
				
				// Loop through all words in the real sentence
				for(int z = 0; z < wordsRealSentence2.length ; z++ ){
					// Loop through all the words in the quasi sentence
					for(int k = 0; k < wordsQuasiSentence2.length ; k++ ){
						ArrayList<String> wordPair2 = new ArrayList<String>();
						wordPair2.add(wordsRealSentence2[z]);
						wordPair2.add(wordsQuasiSentence2[k]);
						
						// Compute translation probability divided bij s-total(e)
						double translationProb = translationProbs.get(wordPair2);
						double sTotal_eValue = sTotal_e.get(wordsRealSentence2[z]);
						double updateValueCount = (translationProb/sTotal_eValue);
					
						// Update count for real world given quasi word and the total of quasi words.
						wordPairCount.put(wordPair2, wordPairCount.get(wordPair2) + updateValueCount);
						quasiWordsCount.put(wordsQuasiSentence2[k], quasiWordsCount.get(wordsQuasiSentence2[k])
								+ updateValueCount);
					}
				}
			}
		
			// Update the translation probabilities.
			for(Entry<String, Double> quasiWord : quasiWordsCount.entrySet()){
				double fValue = quasiWord.getValue();
				for (Entry<ArrayList<String>, Double> wordPair : wordPairCount.entrySet()) {
					ArrayList<String> wordPair_e = wordPair.getKey();
					
					Double wordPair_eValue = wordPair.getValue();
					Double translationProbValue = wordPair_eValue/fValue;
					
					translationProbs.put(wordPair_e, translationProbValue);	
				}
			}
			s++;
		}
		while(s < 1);
		
		for (Entry<ArrayList<String>, Double> entry : translationProbs.entrySet()) {
		    System.out.println(entry.getKey()+" : "+entry.getValue());
		}
	}	
	
	public String[][] getEntriesFromJSON(){
		JSONParser parser = new JSONParser();
		int len = new File("C:\\Users\\NLP\\Entries").listFiles().length; 
		String[][] sentenceEntries = new String[len][2];
		
		try {
			for(int i = 0; i < len; i++ ){
				Object dataStorage = parser.parse(new FileReader("C:\\Users\\NLP\\Entries\\Entry"+ i +".json"));
	 
				JSONObject jsonObject = (JSONObject) dataStorage;
				 
				String realSentence = (String) jsonObject.get("Sentence");
				//System.out.println(realSentence);

				String quasiSentence = (String) jsonObject.get("Quasi-Sentence");
				//System.out.println(quasiSentence);
	 
				sentenceEntries[i][0] = realSentence;
				sentenceEntries[i][1] = quasiSentence;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//System.out.println(sentenceEntries[0][0]);
		//System.out.println(sentenceEntries[0][1]);
		
		return sentenceEntries;
	}
	
	public String[][] createEntries() throws IOException{
		String[][] entriesENNL= new String[1000][2]; 
		
		int i = 0;
	    BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\NLP\\TestData\\corpus_1000en.txt"));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            entriesENNL[i][0] = line;
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	            

	            
	            i++;
	        }
	    } finally {
	        br.close();
	    }
	    
		int j = 0;
	    @SuppressWarnings("resource")
		BufferedReader br2 = new BufferedReader(new FileReader("C:\\Users\\NLP\\TestData\\corpus_1000nl.txt"));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br2.readLine();

	        while (line != null) {
	            sb.append(line);
	            entriesENNL[j][1] = line;
	            sb.append(System.lineSeparator());
	            line = br2.readLine();


	            
	            j++;
	        }
	    } finally {
	        br.close();
	    }
	    
	    return entriesENNL;
	}
}
