import java.io.StringReader;
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
	    Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
	    Pattern filter = Pattern.compile("Source|Recipe\\s{1,}by|.*@.*|://|[R|r]ecipe|[1-9][.]|[()]|[D|d]ownloaded|Yield|[C|c]alories|[S|s]hared|[C|c]ontributor"
	    		+ "|[SERVING|serving|Serving]\\s{0,}:");
	    
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
				      Object[] dep = dependencies.toArray();
				      for ( int j = 0; j < dep.length; j++ ) {
				    	  System.out.println(dep[j]);
				      }
				      
				      // Print typed dependencies
				      //System.err.println(gs);
				    }
			    }
			}
			System.out.println();
		}
		
	}
}
