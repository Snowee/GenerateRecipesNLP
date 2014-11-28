import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;


public class createPredicate {

	public void predicate( Collection<TypedDependency> sentence, String s ){
		List<List<String>> sen = new ArrayList<List<String>>();
		List<String> predicate = new ArrayList<String>();
		List<String> dependencies = new ArrayList<String>();
		List<String> abstractPredicate = new ArrayList<String>();
		List<List<String>> abstractSen = new ArrayList<List<String>>();

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


			System.out.println(sen);
			System.out.println(abstractSen);
			//System.out.println(abstractPredicate);
			System.out.println();

		}

	}

	public List<String> returnDependencies( Collection<TypedDependency> sentence, String word){
		List<String> relations = new ArrayList<String>();

		for( TypedDependency dep : sentence ){
			if( dep.gov().value().equals(word) && ( dep.reln().toString().equals("pobj") || dep.reln().toString().equals("amod")  || dep.reln().toString().equals("dobj") || dep.reln().toString().equals("nn") || dep.reln().toString().equals("prep") || dep.reln().toString().equals("conj") ) ){
				relations.add( dep.reln().toString() );
				relations.add( dep.dep().value() );
			}
		}

		return relations;
	}

	public String returnElems( Collection<TypedDependency> sentence, String word ){
		List<String> dep = new ArrayList<String>();
		List<String> elems = new ArrayList<String>();
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
