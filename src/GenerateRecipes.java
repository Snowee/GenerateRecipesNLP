import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;


public class GenerateRecipes {
	
	private static Map<Vector<String>, Integer> tagTrigramFrequencies = new HashMap<Vector<String>, Integer>();
	private static Map<String, Integer> verbFrequencies = new HashMap<String, Integer>();
	private static Map<String, Map<String, Map<String, Integer>>> verbToSentenceFrequencies;
//	private static Map<String, Map<String, Integer>> structureToSentenceFrequencies =
//			new HashMap<String, Map<String, Integer>>();
//	private static Map<String, Integer> sentenceFrequencies = 
//			new HashMap<String, Integer>();
	private static boolean ibm = true;
	private static boolean distributions = false;
	
	
	public static void main( String args[] ) {
		if( ibm ) {
			Map<String, Double> transProb =
					new HashMap<String, Double>();
			List<String> alignments = new ArrayList<String>();
			boolean fwd = false;
			
			IBMModel1 ibm = new IBMModel1();
			try {
				transProb = ibm.EMAlgorithm( fwd );
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				 alignments = ibm.wordAlignmentViterbi( transProb, fwd );
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ibm.writeAlignmentToFile(alignments, "pred-sent.rev");
		}
		
		if( distributions ) {
			verbToSentenceFrequencies = 
					new HashMap<String, Map<String, Map<String, Integer>>>();
		    String modelPath = DependencyParser.DEFAULT_MODEL;
		    String taggerPath = "english-left3words-distsim.tagger";
			Data data = new Data();
			
			data.getContents();
			Vector<String> recipes = data.recipes;
		    Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)|\\s{3,}", Pattern.MULTILINE | Pattern.COMMENTS);
		    Pattern filter = Pattern.compile("Source|Recipe\\s{1,}by|.*@.*|://|[R|r]ecipe|[1-9][.]|[()]|[D|d]ownloaded|Yield|[C|c]alories|[S|s]hared|[C|c]ontributor"
		    		+ "|[SERVING|serving|Serving]\\s{0,}:|[Y|y]ou|I|;|~");
		    
			MaxentTagger tagger = new MaxentTagger(taggerPath);
		    DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);
			int counter = 0;
		    for( int i = 0; i < recipes.size(); i++ ) {
			    Vector<String> tagTrigram = new Vector<String>();
			    while( tagTrigram.size() != 3 ) {
				    tagTrigram.add( "<START>" );
			    }
			    
				String instructions = data.readJSON( recipes.get(i), "Instructions"); 
				//System.out.println( instructions );
	
				if( instructions != null ) {
					Matcher reMatcher = re.matcher(instructions);
				//System.out.println();
				while (reMatcher.find()) {
					String sentence = reMatcher.group();
					Matcher filterMatcher = filter.matcher(sentence);
				    if ( !filterMatcher.find() ) {
					    DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(sentence));
					    for (List<HasWord> sentence1 : tokenizer) {
						    List<TaggedWord> tagged = tagger.tagSentence(sentence1);
						    GrammaticalStructure gs = parser.predict(tagged);
						    Collection<TypedDependency> dependencies = gs.typedDependencies();
						    //for ( TypedDependency dep : dependencies ) {
								//System.out.println(dep.toString());
							//}
						    //System.out.println();
						    ArrayList<ArrayList<ArrayList<String>>> predicates = createPredicates( dependencies, sentence );
						    ArrayList<ArrayList<String>> predicate = new ArrayList<ArrayList<String>>();
						    ArrayList<ArrayList<String>> abstractPred = new ArrayList<ArrayList<String>>();
						    if( predicates.size() == 2 ) {
						    	predicate = predicates.get(0);
						    	abstractPred = predicates.get(1);
						    }
	//					    System.out.println(counter);
						    if( predicate.size() > 0 ) {
						    	String quasiSentence = arraylistsToString( predicate );
	
						    	sentence = sentence.replaceAll( "\\s+", " " );
						    	sentence = sentence.toLowerCase();
						    	
						    	quasiSentence = quasiSentence.toLowerCase();
						    	if( !quasiSentence.equals("") ) {
							    	JSON json = new JSON();
							    	try {
										json.addToJSON( sentence, quasiSentence, counter );
										counter++;
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
						    	}
						    
						    
	//					    	tagTrigram = addElement( tagTrigram, tagSentence );
	//					    	if( tagTrigram.size() == 3 ) {
	//					    		updateTagFreq( tagTrigram );
	//					    	}
	//					    	
	//					    	Vector<Integer> verbIndices = verbIndices( abstractPred );
	//					    	updateVerbFreq( verbIndices, predicate );
	//					    	updateVerbSentenceFrequencies( predicate, abstractPred );
						    }
						    
						      
						      // Print typed dependencies
						      //System.err.println(gs);
					    }
	
				    }
				
				}
	//		    tagTrigram = addElement( tagTrigram, "<STOP>" );
	//		    updateTagFreq( tagTrigram );			
				}	
				//System.out.println();
			}
		}
		
	}
	
	
	public static void updateVerbSentenceFrequencies( ArrayList<ArrayList<String>> predicate,
			ArrayList<ArrayList<String>> abstractPred ) {
		for( int i = 0; i < predicate.size(); i++ ) {
			
			ArrayList<String> subPredicate = predicate.get(i);
			ArrayList<String> subAbstractPred = abstractPred.get(i);
			String verb = subPredicate.remove(0);
			verb = verb.toLowerCase();
			subAbstractPred.remove(0);
			String subPred = arraylistToString( subPredicate );
			String subAbstPred = arraylistToString( subAbstractPred );
			
			if( !verbToSentenceFrequencies.containsKey(verb) ) {
				Map<String, Map<String, Integer>> structToSent = new HashMap<String, Map<String, Integer>>();
				Map<String, Integer> sentCount = new HashMap<String, Integer>();
				sentCount.put(subPred, 1);
				structToSent.put(subAbstPred, sentCount);
				verbToSentenceFrequencies.put(verb, structToSent);				
			} else {
				Map<String, Map<String, Integer>> structToSent = verbToSentenceFrequencies.get(verb);
				if( !structToSent.containsKey(subAbstPred) ) {
					Map<String, Integer> sentCount = new HashMap<String, Integer>();
					sentCount.put(subPred, 1);
					structToSent.put(subAbstPred, sentCount);
				} else {
					Map<String, Integer> sentCount = structToSent.get(subAbstPred);
					if( !sentCount.containsKey(subPred) ) {
						sentCount.put(subPred, 1);
					} else {
						sentCount.put(subPred, sentCount.get(subPred) + 1);
					}
				}		
			}
		}
	}
	
	public static void updateVerbFreq( Vector<Integer> verbIndices, ArrayList<ArrayList<String>> predicate ) {
		String wordSentence = arraylistsToString( predicate );
		List<String> wordList = new ArrayList<String>(Arrays.asList(wordSentence.split(" ")));
		for( int i = 0; i < verbIndices.size(); i++ ) {
			String verb = wordList.get( verbIndices.get(i) ).toLowerCase();
			if( !verbFrequencies.containsKey( verb ) ) {
				verbFrequencies.put( verb, 1 );
			} else {
				verbFrequencies.put( verb, verbFrequencies.get( verb ) + 1 );
			}
			
		}

	}
	
	public static Vector<Integer> verbIndices( ArrayList<ArrayList<String>> abstractPred ) {
		String tagSentence = arraylistsToString( abstractPred );
		List<String> tagList = new ArrayList<String>(Arrays.asList(tagSentence.split(" ")));
		Vector<Integer> indices = new Vector<Integer>();
		int count = 0;
		for( int i = 0; i < abstractPred.size(); i++ ) {
			for( int j = 0; j < abstractPred.get(i).size(); j++ ) {
				count += 1;
			}
		}
		for( int i = 0; i < count; i++ ) {
			if( tagList.get(i).equals("verb") ) {
				indices.add( i );
			}
		}
		return indices;
	}
	
	public static void updateTagFreq( Vector<String> tagTrigram ) {
		Set<Vector<String>> keys = tagTrigramFrequencies.keySet();
		if( keys.contains( tagTrigram ) ) {
			tagTrigramFrequencies.put( tagTrigram, tagTrigramFrequencies.get( tagTrigram ) + 1 );
		} else {
			tagTrigramFrequencies.put( tagTrigram, 1 );
		}
	}
	
	public static Vector<String> addElement( Vector<String> trigram, String element ) {
		Vector<String> newVector = new Vector<String>();
		if( trigram.size() < 3 ) {
			newVector = new Vector<String>( trigram );
			newVector.add( element );
		} else {
			if( trigram.size() == 3 ) {
				newVector = new Vector<String>();
				newVector.add( trigram.get(1) );
				newVector.add( trigram.get(2) );
				newVector.add( element );
			}
		}
		return newVector;
	}
	
	public static ArrayList<ArrayList<ArrayList<String>>> createPredicates( Collection<TypedDependency> dependencies, String string ) {
		ArrayList<ArrayList<ArrayList<String>>> outputPredicates = new ArrayList<ArrayList<ArrayList<String>>>();
		ArrayList<ArrayList<String>> predicate = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> abstractPred = new ArrayList<ArrayList<String>>();
		boolean notFinished = true;
		boolean NN = false;
		int counter = 0;
		String verb = "";
		String dobj = "";
		//System.out.println(string);
//		for ( TypedDependency dep : dependencies ) {
//			System.out.println(dep);
//		}
		while( notFinished ) {
			for ( TypedDependency dep : dependencies ) {
				if( counter == 0 && dep.reln().toString().equals( "nn" ) ) {
					NN = true;
					verb = dep.dep().value();
					dobj = dep.gov().value();
				}
				counter++;
			}
			
				
			if ( NN ) {
				ArrayList<ArrayList<String>> predicates = new ArrayList<ArrayList<String>>();
				predicates = createPredicate( dependencies, verb, dobj );
				predicate.add( predicates.get(0) );
				abstractPred.add( predicates.get(1) );
				
				ArrayList<String> verbConj = new ArrayList<String>();
				verbConj = loopDependencies( dependencies, "conj", verb );
				
				for( int i = 0; i < verbConj.size(); i++ ) {
					predicates.clear();
					predicates = createPredicate( dependencies, verbConj.get(i), "" );
					predicate.add( predicates.get(0) );
					abstractPred.add( predicates.get(1) );
				}
				outputPredicates.add( predicate );
				outputPredicates.add( abstractPred );
				notFinished = false;

			} else {
				ArrayList<ArrayList<String>> predicates = new ArrayList<ArrayList<String>>();

				verb = loopDependencies( dependencies, "root", "ROOT" ).get(0);
				ArrayList<String> verbConj = new ArrayList<String>();
				verbConj = loopDependencies( dependencies, "conj", verb );
				if( verbConj.size() > 0 ) 
					verbConj.set(0, verb);
				else
					verbConj.add(verb);
				for( int i = 0; i < verbConj.size(); i++ ) {
					predicates.clear();
					predicates = createPredicate( dependencies, verbConj.get(i), "" );
					predicate.add( predicates.get(0) );
					abstractPred.add( predicates.get(1) );
				}
				outputPredicates.add( predicate );
				outputPredicates.add( abstractPred );
				
				notFinished = false;
			}
		}
		return outputPredicates; 
	}
	
	public static ArrayList<String> loopDependencies( Collection<TypedDependency> col, String reln, String gov ) {
		ArrayList<String> dependent = new ArrayList<String>();
		for( TypedDependency dep : col ) {
			if( dep.reln().toString().equals(reln) && dep.gov().value().equals(gov) ) {
				dependent.add(dep.dep().value());
			}
		}
		return dependent;
	}
	
	
	public static ArrayList<ArrayList<String>> createPredicate( Collection<TypedDependency> dependencies, String verb, String dobj ) {
		ArrayList<ArrayList<String>> predicates = new ArrayList<ArrayList<String>>();
		ArrayList<String> predicate = new ArrayList<String>();
		ArrayList<String> abstractPred = new ArrayList<String>();
		ArrayList<String> dobjs = new ArrayList<String>();
		ArrayList<String> preps = new ArrayList<String>();
		ArrayList<String> pobjs = new ArrayList<String>();
		ArrayList<String> amods = new ArrayList<String>();
		ArrayList<String> nComp = new ArrayList<String>();
	
		predicate.add( verb );
		abstractPred.add( "verb" );
		
		dobjs = loopDependencies( dependencies, "dobj", verb );
		if( !dobj.equals("") )
			dobjs.add( 0, dobj );

		for( int i = 0; i < dobjs.size(); i++ ) {
			ArrayList<String> conjDobjs = new ArrayList<String>(); 
			conjDobjs = loopDependencies( dependencies, "conj", dobjs.get(i) );
			if( conjDobjs.size() > 0 )
				conjDobjs.add(0, dobjs.get(i));
			else
				conjDobjs.add(dobjs.get(i));
			for( int k = 0; k < conjDobjs.size(); k++ ) {
				amods = getAmod( dependencies, conjDobjs.get(k) );
				nComp = getNN( dependencies, conjDobjs.get(k), verb );
				
				for( int j = 0; j < amods.size(); j++ ) {
					conjDobjs.set(k, amods.get(j).concat(" ".concat(conjDobjs.get(k))));
				}
				for( int j = 0; j < nComp.size(); j++ ) {
					conjDobjs.set(k, nComp.get(j).concat(" ".concat(conjDobjs.get(k))));
				}
				amods.clear();
				nComp.clear();
				
				predicate.add( conjDobjs.get(k) );
				abstractPred.add( "dobj" );
			}
		}
		
		preps = loopDependencies( dependencies, "prep", verb );
		ArrayList<String> pobjOld = new ArrayList<String>();
		pobjs = new ArrayList<String>();
		for( int i = 0; i < preps.size(); i++ ) {
			pobjs = loopDependencies( dependencies, "pobj", preps.get(i) );
			if( ! pobjs.equals(pobjOld ) ) {			
				for( int k = 0; k < pobjs.size(); k++ ) {
					amods = getAmod( dependencies, pobjs.get(k) );
					nComp = getNN( dependencies, pobjs.get(k), verb );
					if( k == 0 )
						pobjOld = new ArrayList<String>( pobjs );

					for( int j = 0; j < amods.size(); j++ ) {
						pobjs.set(k, amods.get(j).concat(" ".concat(pobjs.get(k))));
					}
					for( int j = 0; j < nComp.size(); j++ ) {
						pobjs.set(k, nComp.get(j).concat(" ".concat(pobjs.get(k))));
					}
					
					amods.clear();
					nComp.clear();
					
					predicate.add( pobjs.get(k) );
					abstractPred.add( "pobj" );
				}
				pobjs.clear();
			}
	
		}
		preps.clear();
		preps = loopDependencies( dependencies, "prep", dobj );
		pobjs.clear();
		pobjOld.clear();
		for( int i = 0; i < preps.size(); i++ ) {
			
			pobjs = loopDependencies( dependencies, "pobj", preps.get(i) );
			if( ! pobjs.equals( pobjOld ) ) {
				for( int k = 0; k < pobjs.size(); k++ ) {
					amods = getAmod( dependencies, pobjs.get(k) );
					nComp = getNN( dependencies, pobjs.get(k), verb );
					if( k == 0 )
						pobjOld = new ArrayList<String>( pobjs );
					for( int j = 0; j < amods.size(); j++ ) {
						pobjs.set(k, amods.get(j).concat(" ".concat(pobjs.get(k))));
					}
					for( int j = 0; j < nComp.size(); j++ ) {
						pobjs.set(k, nComp.get(j).concat(" ".concat(pobjs.get(k))));
					}
					
					amods.clear();
					nComp.clear();
					
					predicate.add( pobjs.get(k) );
					abstractPred.add( "pobj" );
				}
				pobjs.clear();
			}
	
		}
		predicates.add( predicate );
		predicates.add( abstractPred );
		return predicates;
	}
	
//	
//	public static ArrayList<String> getDobjs( Collection<TypedDependency> dependencies, String governor ) {
//		
//	}
	
	public static ArrayList<String> getAmod( Collection<TypedDependency> dependencies, String governor ) {
		ArrayList<String> amods = new ArrayList<String>();
		amods = loopDependencies( dependencies, "amod", governor );
		return amods;
	}
	
	public static ArrayList<String> getNN( Collection<TypedDependency> dependencies, String governor, String verb ) {
		ArrayList<String> nComp = new ArrayList<String>();
		nComp = loopDependencies( dependencies, "nn", governor );
		nComp.remove( verb );
		return nComp;
	}
	
	
	public static String arraylistsToString( ArrayList<ArrayList<String>> string ) {
		String quasiSentence = "";
		for( int i = 0; i < string.size(); i++ ) {
			ArrayList<String> predicate = string.get(i);
			for( String s : predicate ) {
				quasiSentence = quasiSentence.concat( " ".concat( s ) );
			}
		}
		return quasiSentence;
	}
	
	public static String arraylistToString( ArrayList<String> string ) {
		String quasiSentence = "";
		for( String s : string ) {
			quasiSentence = quasiSentence.concat( " ".concat( s ) );
		}
		return quasiSentence;
	}
	
	
	public static ArrayList<ArrayList<ArrayList<String>>> predicate( Collection<TypedDependency> sentence, String s ){
		ArrayList<ArrayList<String>> sen = new ArrayList<ArrayList<String>>();
		ArrayList<String> predicate = new ArrayList<String>();
		ArrayList<String> dependencies = new ArrayList<String>();
		ArrayList<String> abstractPredicate = new ArrayList<String>();
		ArrayList<ArrayList<String>> abstractSen = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<ArrayList<String>>> returnSentences = 
				new ArrayList<ArrayList<ArrayList<String>>>();
		
		//Check only first element of sentence
		TypedDependency rel = sentence.stream().findFirst().get();

		//Consider root element
		if(rel.reln().toString().equals("root")){	

			//Dependencies of root dependent
			dependencies = returnDependencies( sentence, rel.dep().value());

			//System.out.println(sentence.toString());
			System.out.println(s);
			System.out.println(dependencies);

			//Add root dependent
			predicate.add(rel.dep().value());
			abstractPredicate.add("verb");

			//Dependencies of root dependent
			for( int i = 0; i < dependencies.size(); i+=2 ){
				String elems = "";
				//String abstractElems = "";

				if( dependencies.get(i).equals("conj") ){
					elems = "";
					elems = dependencies.get(i+1);
					predicate = new ArrayList<String>();
					abstractPredicate = new ArrayList<String>();
					abstractPredicate.add("verb");
				}
				else if( !dependencies.get(i).equals("prep") ){
					elems = dependencies.get(i+1);
					abstractPredicate.add("dobj"); 
				}
				else{
					List<String> dependencies2 = returnDependencies(sentence, dependencies.get(i+1));
					//System.out.printf("Dependencies of %s: %s\n", dependencies.get(i+1), dependencies2);

					System.out.println(dependencies2);
					if(!dependencies2.isEmpty())
						elems = returnElems( sentence, dependencies2.get(1));
					
					//Dependencies of dependencies
					/*for( int j = 0; j < dependencies2.size(); j+=2 ){
						//System.out.println(returnDependencies( sentence, dependencies2.get(j+1)));
						elems = returnElems( sentence, dependencies2.get(j+1));
					}*/
					abstractPredicate.add("pobj");
				}
				predicate.add(elems);
				
				if( sen.isEmpty() || dependencies.get(i).equals("conj")){
					sen.add(predicate);
					abstractSen.add(abstractPredicate);
				}
			}

			returnSentences.add(sen);
			returnSentences.add(abstractSen);
			System.out.println(sen);
			System.out.println(abstractSen);
			//System.out.println(abstractPredicate);
			;

		}
		return returnSentences;
	}

	public static ArrayList<String> returnDependencies( Collection<TypedDependency> sentence, String word){
		ArrayList<String> relations = new ArrayList<String>();

		for( TypedDependency dep : sentence ){
			if( dep.gov().value().equals(word) && 
					( dep.reln().toString().equals("pobj") || 
							dep.reln().toString().equals("amod")  || 
							dep.reln().toString().equals("dobj") || 
							dep.reln().toString().equals("nn") || 
							dep.reln().toString().equals("prep") || 
							dep.reln().toString().equals("conj") ) ) {
				relations.add( dep.reln().toString() );
				relations.add( dep.dep().value() );
			}
		}

		return relations;
	}

	public static String returnElems( Collection<TypedDependency> sentence, String word ){
		ArrayList<String> dep = new ArrayList<String>();
		ArrayList<String> elems = new ArrayList<String>();
		boolean amodNN = false;
		int amodCounter = 0;

		dep = returnDependencies( sentence, word );


		for(int i = 1; i < dep.size(); i+=2){
			if( !dep.get(i-1).equals("prep")){
				elems.add(dep.get(i));

				if(dep.get(i-1).equals("amod") || dep.get(i-1).equals("nn")){
					amodNN = true;
					amodCounter++;
				}
			}
		}


	if( amodNN ){
		elems.add(amodCounter, word);
	}
	else{
		elems.add(0, word);
	}

	return String.join(" ", elems);
}
	
	
}
