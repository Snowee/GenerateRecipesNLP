import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

	
	public static void main( String args[] ) {
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
		for( int i = 0; i < recipes.size(); i++ ) {
			String instructions = data.readJSON( recipes.get(i), "Instructions"); 
			System.out.println( instructions );
			Matcher reMatcher = re.matcher(instructions);
			System.out.println();

			while (reMatcher.find()) {
				String sentence = reMatcher.group();
				Matcher filterMatcher = filter.matcher(sentence);
			    if ( !filterMatcher.find() ) {
					System.out.println(sentence);
				    DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(sentence));
				    for (List<HasWord> sentence1 : tokenizer) {
					    List<TaggedWord> tagged = tagger.tagSentence(sentence1);
					    GrammaticalStructure gs = parser.predict(tagged);
					    Collection<TypedDependency> dependencies = gs.typedDependencies();
					    for ( TypedDependency dep : dependencies ) {
							System.out.println(dep.toString());
						}
					    System.out.println();
					    ArrayList<String> predicate = createPredicates( dependencies );
					    if( predicate.size() > 0 ) {
					    	System.out.println();
					    }
					    
					      
					      // Print typed dependencies
					      //System.err.println(gs);
				    }
			    }
			}
			System.out.println();
		}
		
	}
	
	
	public static ArrayList<String> createPredicates( Collection<TypedDependency> dependencies ) {
		ArrayList<String> predicate = new ArrayList<String>();
		ArrayList<String> abstractPred = new ArrayList<String>();
		boolean notFinished = true;
		boolean NN = false;
		int counter = 0;
		String verb = "";
		String dobj = "";
		ArrayList<String> conjVerb = new ArrayList<String>();
		ArrayList<String> verbPrep = new ArrayList<String>();
		ArrayList<String> dobjs = new ArrayList<String>();
		ArrayList<String> pobjs = new ArrayList<String>();
		ArrayList<String> ncomp = new ArrayList<String>();
		ArrayList<String> amods = new ArrayList<String>();
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
				verbPrep = loopDependencies( dependencies, "prep", dobj );
				if( verbPrep.size() == 0 ) {
					predicate.add(verb);
					abstractPred.add("verb");
					dobjs = loopDependencies( dependencies, "conj", dobj );
					ncomp = loopDependencies( dependencies, "nn", dobj );
					for( int i = 0; i < ncomp.size(); i++ ) {
						if( !ncomp.get(i).equals(verb) )
							dobj = ncomp.get(i).concat(" ".concat(dobj));
					}
					amods = loopDependencies( dependencies, "amod", dobj );
					for( int i = 0; i < amods.size(); i++ ) {
						dobj = amods.get(i).concat(" ".concat(dobj));
					}
					predicate.add(dobj);
					abstractPred.add("dobj");
					if( dobjs.size() != 0 ) {
						for( int i = 0; i < dobjs.size(); i++ ) {
							ncomp = loopDependencies( dependencies, "nn", dobjs.get(i) );
							for( int j = 0; j < ncomp.size(); j++ ) {
								if( !ncomp.get(j).equals(verb) )
									dobjs.set(i, ncomp.get(j).concat(" ".concat(dobjs.get(i))));
							}
							amods = loopDependencies( dependencies, "amod", dobjs.get(i));
							for( int j = 0; j < amods.size(); j++ ) {
								dobjs.set(i, amods.get(j).concat(" ".concat(dobjs.get(i))));
							}
							predicate.add(dobjs.get(i));
							abstractPred.add("dobj");
						}
					}
				} else {
					ncomp = loopDependencies( dependencies, "nn", dobj );
					for( int i = 0; i < ncomp.size(); i++ ) {
						if( !ncomp.get(i).equals(verb) )
							dobj = ncomp.get(i).concat(" ".concat(dobj));
					}
					verb = verb.concat(" ".concat(verbPrep.get(0)));
					predicate.add(verb);
					abstractPred.add("verb");
					dobjs = loopDependencies( dependencies, "conj", dobj );

					amods = loopDependencies( dependencies, "amod", dobj );
					for( int i = 0; i < amods.size(); i++ ) {
						dobj = amods.get(i).concat(" ".concat(dobj));
					}
					predicate.add(dobj);
					abstractPred.add("dobj");
					if( dobjs.size() != 0 ) {
						for( int i = 0; i < dobjs.size(); i++ ) {
							ncomp = loopDependencies( dependencies, "nn", dobjs.get(i) );
							for( int j = 0; j < ncomp.size(); j++ ) {
								if( !ncomp.get(j).equals(verb) )
									dobjs.set(i, ncomp.get(j).concat(" ".concat(dobjs.get(i))));
							}
							amods = loopDependencies( dependencies, "amod", dobjs.get(i));
							for( int j = 0; j < amods.size(); j++ ) {
								dobjs.set(i, amods.get(j).concat(" ".concat(dobjs.get(i))));
							}
							predicate.add(dobjs.get(i));
							abstractPred.add("dobj");
						}
					}
					pobjs = loopDependencies( dependencies, "pobj", verbPrep.get(0) );
					if( pobjs.size() != 0 ) {
						predicate.add(pobjs.get(0));
						abstractPred.add("pobj");
					}
					
				}
				notFinished = false;

			}
//					 else {
//						predicate.add("and");
//						abstractPred.add("conj");
//						verbPrep = loopDependencies( dependencies, "prep", dobj );
//						if( verbPrep.size() == 0 ) {
//							predicate.add(verb);
//							abstractPred.add("verb");
//						} else {
//							verb.concat(" ".concat(verbPrep.get(0)));
//							predicate.add(verb);
//							abstractPred.add("verb");
//						}
//
//					}
				
				
				
			else {
				notFinished = false;

				continue;
//					if( dep.reln().toString() == "root") {
//						verb = dep.dep().toString();
//					}
//					conjVerb = loopDependencies( dependencies, "conj", verb );
//					if( conjVerb.size() == 0 ) {
//						
//					}
			}
			
			//}
		}
		return predicate;
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
	
}
