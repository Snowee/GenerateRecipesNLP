import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class IBMModel1 {
	
	
	public List<String> wordAlignmentViterbi( 
			Map<String, Double> transProb, boolean fwd ) throws IOException {

		List<String> sentenceAlignments = 
				new ArrayList<String>();
		
		//String[][] sentenceEntries = createEntries( fwd );
		String[][] sentenceEntries = getEntriesFromJSON( fwd );
		for( int i = 0; i < sentenceEntries.length; i++ ) {
			String alignment = "";
			String[] sentencePair = sentenceEntries[i];
			String sentence1 = sentencePair[0];
			String sentence2 = sentencePair[1];
			
			String[] sentence1Split = sentence1.split(" ");
			String[] sentence2Split = sentence2.split(" ");
			
			for( int m = 0; m < sentence1Split.length; m++ ) {
				double bestProb = -1;
				int bestAlignment = -1;
				String wordAlignment = Integer.toString(m) + "-";
				for( int n = 0; n < sentence2Split.length; n++ ) {
					String key = sentence1Split[m] + "_" + sentence2Split[n];
					double wordProb = transProb.get(key);
					if( wordProb > bestProb ) {
						bestAlignment = n;
						bestProb = wordProb;
					}
				}
				wordAlignment = wordAlignment + Integer.toString(bestAlignment);
				alignment = alignment + wordAlignment + " ";
			}
			sentenceAlignments.add(alignment.trim());
		}
		
		return sentenceAlignments;
	}

	public void writeAlignmentToFile( List<String> alignments, String name ) {
		
		try {
			File file = new File( name );
			
			if( !file.exists() ) {
				file.createNewFile();
			} else {
				file.delete();
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter( file.getAbsoluteFile() );
			BufferedWriter bw = new BufferedWriter(fw);
			for( int i = 0; i < alignments.size(); i++ ) {
				String alignment = alignments.get(i);
			
				bw.write(alignment);
				bw.newLine();
			}
			bw.close();
			
		} catch ( IOException e) {
			e.printStackTrace();
		}
			
		
	}
	
	public Map<String, Double> EMAlgorithm( boolean fwd ) throws IOException{
		
		Map<String, Double> transProb = new HashMap<String, Double>();
		ArrayList<String> allSourceWords = new ArrayList<String>();
		Map<String, Double> sTotalE = new HashMap<String, Double>();  
		Map<String, Double> transCount = new HashMap<String, Double>();
		Map<String, Double> targetCount = new HashMap<String, Double>();
		
		//String[][] sentenceEntries = createEntries( fwd );
		String[][] sentenceEntries = getEntriesFromJSON( fwd );
		
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
		
		
		int nrOfIters = 5;
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
//	double max = 0.0;
//	String best = "";
//	
//	for (Entry<String, Double> entry : transProb.entrySet()) {
//		if( entry.getValue() > .7 ) {
//			System.out.println(entry.getKey()+" : "+entry.getValue());
//		}
//		if( entry.getValue() > max ) {
//			best = entry.getKey();
//			max = entry.getValue();
//		}
//	}
//		System.out.println(best + Double.toString(max));
		
		return transProb;
	}	
	
	public String[][] getEntriesFromJSON( boolean fwd ){
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
				if( fwd ) {
					sentenceEntries[i][0] = quasiSentence;
					sentenceEntries[i][1] = realSentence;
				} else{
					sentenceEntries[i][0] = realSentence;
					sentenceEntries[i][1] = quasiSentence;
				}
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
	
	public String[][] createEntries( boolean fwd ) throws IOException{
		String[][] entriesENNL= new String[1000][2]; 
		
		int i = 0;
	    BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\NLP\\TestData\\corpus_1000en.txt"));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            if( fwd ) {
	            	entriesENNL[i][0] = line;
	            } else {
	            	entriesENNL[i][1] = line;
	            }
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
	            if( fwd ) {
	            	entriesENNL[j][1] = line;
	            } else {
	            	entriesENNL[j][0] = line;
	            }
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