import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

//goldenStandard.size()

public class CopyOfAlignmentEvaluation {
	
	public void score(){
		ArrayList<String> ownScore = readFromFiles("corpus_1000_ennl_viterbi.txt");
		ArrayList<String> goldenStandard = readFromFiles("corpus_1000_ennl_viterbi.txt");
		
		// Initialize parameters for recall and precision.
		double precision;
		double recall;
		int totalRetrieved = 0;
		int relevantRetrieved = 0;
		int totalRelevant = 0;
		
		// Loop through all sentences.
		for(int i = 0; i < goldenStandard.size() ; i++){
			// Grab a sentence pair from the file.
			String lineOwnScore = ownScore.get(i);
			String lineGoldenStandard = goldenStandard.get(i);
			
			// Split the sentence pair on spaces.
			String[] wordsInLineOwnScore = lineOwnScore.split("\\s+");
			String[] wordsInLineGoldenStandard = lineGoldenStandard.split("\\s+");
			
			for(int j = 0; j < wordsInLineGoldenStandard.length; j++){
				ArrayList<String> alignmentOwnScore = new ArrayList<String>();
				ArrayList<String> alignmentGoldenStandard = new ArrayList<String>();
				
				// Retrieve only the words from the sentences and gather the
				// corresponding alignment scores.
				if(!isInteger(wordsInLineOwnScore[j]) && !wordsInLineOwnScore[j].equals("({") && !wordsInLineOwnScore[j].equals("})")){
					alignmentOwnScore = getAlignment(wordsInLineOwnScore, j);
				}
				
				if(!isInteger(wordsInLineGoldenStandard[j]) && !wordsInLineGoldenStandard[j].equals("({") && !wordsInLineGoldenStandard[j].equals("})")){
					alignmentGoldenStandard = getAlignment(wordsInLineOwnScore, j);
				}
				
				// If the alignment is only in the score that was computed, then this affects the precision negatively.
				// If it was in the golden standard and in the score that was computed, then this affects precision and recall positively.
				for(int k = 0; k < alignmentOwnScore.size(); k++){
					String alignedOwnScore = alignmentOwnScore.get(k);
					if(alignedOwnScore.equals("null")){
						alignmentGoldenStandard.contains(alignedOwnScore);
					} else if(!alignmentGoldenStandard.contains(alignedOwnScore)){
						totalRetrieved++;
					} else if(alignmentGoldenStandard.contains(alignedOwnScore)){
						totalRetrieved++;
						totalRelevant++;
						relevantRetrieved++;
					}
				}
				
				// If an alignment only appears in the golden standard and not in the own computed score,
				// then it affects the recall negativle.
				for(int l = 0; l < alignmentGoldenStandard.size(); l++){
					String alignedGoldenStandard = alignmentGoldenStandard.get(l);
					if(!alignmentOwnScore.contains(alignedGoldenStandard)){
						totalRelevant++;
					}
				}
			}
		}
		
		precision = relevantRetrieved/totalRetrieved;
		recall = relevantRetrieved/totalRelevant;
		
		System.out.println("Precision = " + precision);
		System.out.println("Recall = " + recall);
	}

	// Read a symmetrized alignment file and only retrieve the sentences with alignment scores.
	public ArrayList<String> readFromFiles(String fileName){
		
		ArrayList<String> sentences = new ArrayList<String>();
		String path = "C:\\Users\\NLP\\TestData\\";
		try (BufferedReader br = new BufferedReader(new FileReader(path+fileName)))
		{
			String sCurrentLine;
			int i = 1;
			while ((sCurrentLine = br.readLine()) != null) {
				if(i % 3 == 0){
					sentences.add(sCurrentLine);
				}
				i++;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		
		return sentences;
	}
	
	// Check if a String parameter is an integer.
	public static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c <= '/' || c >= ':') {
				return false;
			}
		}
		return true;
	}
	
	// Get the alignment scores for a word.
	public ArrayList<String> getAlignment(String[] wordsInLine, int j){
		ArrayList<String> alignmentNumbers = new ArrayList<String>();
		int k = j+2;
		while( !wordsInLine[k].equals("})")){
			String word = wordsInLine[k];
			alignmentNumbers.add(word);
			k++;
		}
		if(alignmentNumbers.size() == 0){
			alignmentNumbers.add("null");
		}
		return alignmentNumbers;
	}
}
