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
		
		Map<String, Double> transProb = new HashMap<String, Double>();
		ArrayList<String> allSourceWords = new ArrayList<String>();
		Map<String, Double> sTotalE = new HashMap<String, Double>();  
		Map<String, Double> transCount = new HashMap<String, Double>();
		Map<String, Double> targetCount = new HashMap<String, Double>();
		
		String[][] sentenceEntries = createEntries();
		
		for( int i = 0; i < sentenceEntries.length; i++ ) {
			String[] sentencePair = sentenceEntries[i];
			String sentence1 = sentencePair[0];
			String sentence2 = sentencePair[1];
			
			String[] sentence1Split = sentence1.split(" ");
			String[] sentence2Split = sentence2.split(" ");
			
			for( int m = 0; m < sentence1Split.length; m++ ) {
				for( int n = 0; n < sentence2Split.length; n++ ) {
					String key = sentence1Split[m] + "_" + sentence2Split[n];
					transProb.put( key, 0.25 );
					transCount.put( key, 0.0 );
							
					String sourceWord = sentence1Split[m];
					if( !allSourceWords.contains( sourceWord ) ) {
						allSourceWords.add( sourceWord );
					}
					String targetWord = sentence2Split[n];
					targetCount.put(targetWord, 0.0);
				}
			}

		}
		
		
		int nrOfIters = 10;
		for( int k = 0; k < nrOfIters; k++ ) {
			for( Map.Entry<String, Double> entry : transCount.entrySet() ) {
				transCount.put( entry.getKey(), 0.0 );
			}
			for( Map.Entry<String, Double> entry : targetCount.entrySet() ) {
				targetCount.put( entry.getKey(), 0.0 );
			}
			
			for( int i = 0; i < sentenceEntries.length; i++ ) {
				String[] sentencePair = sentenceEntries[i];
				String sentence1 = sentencePair[0];
				String sentence2 = sentencePair[1];
				
				String[] sentence1Split = sentence1.split(" ");
				String[] sentence2Split = sentence2.split(" ");
				
				sTotalE.clear();
				for( int n = 0; n < sentence1Split.length; n++ ) {
					sTotalE.put( sentence1Split[n], 0.0 );
				}
				for( int n = 0; n < sentence1Split.length; n++ ) {
					for( int m = 0; m < sentence2Split.length; m++ ) {
						String key = sentence1Split[n] + "_" + sentence2Split[m];
						sTotalE.put(sentence1Split[n], sTotalE.get(sentence1Split[n]) + transProb.get(key));
					}
				}
				
				for( int n = 0; n < sentence1Split.length; n++ ) {
					for( int m = 0; m < sentence2Split.length; m++ ) {
						String key = sentence1Split[n] + "_" + sentence2Split[m];
						String sourceWord = sentence1Split[n];
						String targetWord = sentence2Split[m];
						
						transCount.put( key, transCount.get(key) + (transProb.get(key)/sTotalE.get(sourceWord)) );
						targetCount.put(targetWord, targetCount.get(targetWord) + (transProb.get(key)/sTotalE.get(sourceWord)) );
					}
				}
				
			}
			
			for( int i = 0; i < allSourceWords.size(); i++ ) {
				for( Map.Entry<String, Double> entry : targetCount.entrySet() ) { 
					String key = allSourceWords.get(i) + "_" + entry.getKey();
					if( transProb.containsKey(key) ) {
						transProb.put( key, transCount.get(key)/targetCount.get(entry.getKey()));
					}
				}
			}
			
		}
		
	for (Entry<String, Double> entry : transProb.entrySet()) {
		if( entry.getValue() > .7 ) {
			System.out.println(entry.getKey()+" : "+entry.getValue());
		}
	}
		
		
//		int s = 0;
//		String[][] sentenceEntries = createEntries();
//		
//		HashMap<ArrayList<String>, Double> translationProbs = new HashMap<ArrayList<String>, Double>();
//		HashMap<String, Double> quasiWordsCount = new HashMap<String, Double>();
//		HashMap<ArrayList<String>, Double> wordPairCount = new HashMap<ArrayList<String>, Double>();
//		
//		// Preprocess sentences to gain access on word level.
//		for(int i = 0; i < sentenceEntries.length ; i++){
//			String realSentence = sentenceEntries[i][0];
//			String quasiSentence = sentenceEntries[i][1];
//			
//			String[] wordsRealSentence = realSentence.split("\\s+");
//			String[] wordsQuasiSentence = quasiSentence.split("\\s+");
//				
//			for(int j = 0; j < wordsQuasiSentence.length; j++){	
//				quasiWordsCount.put(wordsQuasiSentence[j], 0.0);
//				
//				for(int q = 0; q < wordsRealSentence.length; q++){				
//					ArrayList<String> realQuasiPair = new ArrayList<String>();
//					realQuasiPair.add(wordsRealSentence[q]);
//					realQuasiPair.add(wordsQuasiSentence[j]);
//					
//					translationProbs.put(realQuasiPair, 0.25);
//					wordPairCount.put(realQuasiPair, 0.0);
//				}
//			}
//		}
//		
//		/*
//		for (Entry<ArrayList<String>, Double> entry : wordPairCount.entrySet()) {
//		    System.out.println(entry.getKey()+" : "+entry.getValue());
//		}
//				
//		
//		for (Entry<ArrayList<String>, Double> entry : translationProbs.entrySet()) {
//		    System.out.println(entry.getKey()+" : "+entry.getValue());
//		}
//		 */
//		
//		do{
//			for (Entry<ArrayList<String>, Double> wordPair : wordPairCount.entrySet()) {
//			    wordPairCount.put(wordPair.getKey(), 0.0);
//			}
//			for(Entry<String, Double> quasiWord : quasiWordsCount.entrySet()){
//				quasiWordsCount.put(quasiWord.getKey(), 0.0);
//			}
//			
//			// Loop through all sentences.
//			for(int i = 0 ; i < sentenceEntries.length; i++){
//				// Get the real sentence - quasi sentence pair
//				String[] wordsRealSentence2 = sentenceEntries[i][0].split("\\s+");
//				String[] wordsQuasiSentence2 = sentenceEntries[i][1].split("\\s+");
//				
//				// Create a hashmap to be used for the variable s-Total(e)
//				HashMap<String, Double> sTotal_e = new HashMap<String, Double>();
//				
//				ArrayList<String> wordPair = new ArrayList<String>();
//
//				
//				// Loop through all words from the real sentence
//				for(int j = 0 ; j < wordsRealSentence2.length; j++){
//					
//					// Set all s-total values on 0 for all words in the real sentence
//					sTotal_e.put(wordsRealSentence2[j], 0.0);
//					
//					// Loop through all quasi words
//					for(int q = 0; q < wordsQuasiSentence2.length; q++){
//						
//						// Create an arraylist with the current real - quasi word pair.
//						wordPair.add(wordsRealSentence2[j]);
//						wordPair.add(wordsQuasiSentence2[q]);
//						
//						// Get the translation probability from the translation probability hashmap with the 
//						// real - quasi word pair.
//						//System.out.println(wordsRealSentence2[j]);
//						//System.out.println(wordsQuasiSentence2[q]);
//						double translationProbValue = translationProbs.get(wordPair);
//						
//						// Alter the s-total with the translation probability.
//						sTotal_e.put(wordsRealSentence2[j], sTotal_e.get(wordsRealSentence2[j]) + translationProbValue);
//						
//						wordPair.clear();
//					}
//				}
//				
//				// Loop through all words in the real sentence
//				for(int z = 0; z < wordsRealSentence2.length ; z++ ){
//					// Loop through all the words in the quasi sentence
//					for(int k = 0; k < wordsQuasiSentence2.length ; k++ ){
//						ArrayList<String> wordPair2 = new ArrayList<String>();
//						wordPair2.add(wordsRealSentence2[z]);
//						wordPair2.add(wordsQuasiSentence2[k]);
//						
//						// Compute translation probability divided bij s-total(e)
//						double translationProb = translationProbs.get(wordPair2);
//						double sTotal_eValue = sTotal_e.get(wordsRealSentence2[z]);
//						double updateValueCount = (translationProb/sTotal_eValue);
//					
//						// Update count for real world given quasi word and the total of quasi words.
//						wordPairCount.put(wordPair2, wordPairCount.get(wordPair2) + updateValueCount);
//						quasiWordsCount.put(wordsQuasiSentence2[k], quasiWordsCount.get(wordsQuasiSentence2[k])
//								+ updateValueCount);
//					}
//				}
//			}
//		
//			// Update the translation probabilities.
//			for(Entry<String, Double> quasiWord : quasiWordsCount.entrySet()){
//				double fValue = quasiWord.getValue();
//				for (Entry<ArrayList<String>, Double> wordPair : wordPairCount.entrySet()) {
//					ArrayList<String> wordPair_ef = wordPair.getKey();
//					
//					Double wordPair_eValue = wordPair.getValue();
//					Double translationProbValue = wordPair_eValue/fValue;
//					
//					translationProbs.put(wordPair_ef, translationProbValue);	
//				}
//			}
//			s++;
//		}
//		while(s < 10);
//		
//		for (Entry<ArrayList<String>, Double> entry : translationProbs.entrySet()) {
//		    System.out.println(entry.getKey()+" : "+entry.getValue());
//		}
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