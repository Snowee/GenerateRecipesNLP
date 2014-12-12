import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

//goldenStandard.size()

public class AlignmentEvaluation {
	
	public void score(){
		ArrayList<String> ownScore = readFromFiles("aligned.grow-diag-final-and.txt");
		ArrayList<String> goldenStandard = readFromFiles("aligned.grow-diag-final-and.txt");
		
		// Initialize parameters for recall and precision.
		double precision;
		double recall;
		int totalRetrieved = 0;
		int relevantRetrieved = 0;
		int totalRelevant = 0;
		double aer = 0;
		
		// Loop through all sentences.
		for(int i = 0; i < 1 ; i++){
			// Grab a sentence pair from the file.
			String lineOwnScore = ownScore.get(i);
			String lineGoldenStandard = goldenStandard.get(i);
			
			// Split the sentence pair on spaces.
			String[] wordsInLineOwnScore = lineOwnScore.split("\\s+");
			String[] wordsInLineGoldenStandard = lineGoldenStandard.split("\\s+");
			ArrayList<String> alignmentOwnScore = new ArrayList<String>();
			ArrayList<String> alignmentGoldenStandard = new ArrayList<String>();
			for(int j = 0; j < wordsInLineGoldenStandard.length; j++){
				alignmentOwnScore.add(wordsInLineOwnScore[j]);
			}
			for(int h = 0; h < wordsInLineGoldenStandard.length; h++){
				alignmentGoldenStandard.add(wordsInLineGoldenStandard[h]);
			}
				// If the alignment is only in the score that was computed, then this affects the precision negatively.
				// If it was in the golden standard and in the score that was computed, then this affects precision and recall positively.
				for(int k = 0; k < alignmentOwnScore.size(); k++){
					String alignedOwnScore = alignmentOwnScore.get(k);
					if(!alignmentGoldenStandard.contains(alignedOwnScore)){
						System.out.println("not in golden standard");
						totalRetrieved++;
					} else if(alignmentGoldenStandard.contains(alignedOwnScore)){
						System.out.println("correctly classified");
						totalRetrieved++;
						totalRelevant++;
						relevantRetrieved++;
					}
				}
				
				// If an alignment only appears in the golden standard and not in the own computed score,
				// then it affects the recall negatively.
				for(int l = 0; l < alignmentGoldenStandard.size(); l++){
					String alignedGoldenStandard = alignmentGoldenStandard.get(l);
					if(!alignmentOwnScore.contains(alignedGoldenStandard)){
						System.out.println("Only in golden standard");
						totalRelevant++;
					}
				}
			
		}
		
		precision = relevantRetrieved/totalRetrieved;
		recall = relevantRetrieved/totalRelevant;
		
		aer = 1 - (2 * (double)relevantRetrieved/((double)totalRetrieved + (double)totalRelevant));
		
		System.out.println("relevant retrieved: " + relevantRetrieved);
		System.out.println("total retrieved: " + totalRetrieved);
		System.out.println("total relevant: " + totalRelevant);
		
		System.out.println("Precision = " + precision);
		System.out.println("Recall = " + recall);
		System.out.println("AER = " + aer);
	}

	// Read a symmetrized alignment file and only retrieve the sentences with alignment scores.
	public ArrayList<String> readFromFiles(String fileName){
		
		ArrayList<String> sentences = new ArrayList<String>();
		String path = "C:\\Users\\NLP\\TestData\\";
		try (BufferedReader br = new BufferedReader(new FileReader(path+fileName)))
		{
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
					sentences.add(sCurrentLine);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		
		return sentences;
	}
	}