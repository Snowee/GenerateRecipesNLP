import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

//import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Data {

	private File recipePath;
	public String path;
	public Vector<String> recipes = new Vector<String>();
	
	public Data() {
		recipePath = new File("C:\\Users\\NLP\\recipes");
		path = recipePath.toString();
	}
	
	
	public void getContents() {
		File[] listOfFiles = recipePath.listFiles();
		
		for ( int i = 0; i < listOfFiles.length; i++ ) {
			if ( listOfFiles[i].isFile() ) {
				String fileName = listOfFiles[i].getName();
				if ( fileName.endsWith(".json") ) {
					recipes.add( fileName );
				}
			}
		}
	}
	
	
	public String readJSON( String fileName, String object ) {
		JSONParser parser = new JSONParser();
		String jsonString = "";
		try {
			Object obj = parser.parse( 
					new FileReader(path.concat("\\".concat(fileName))));
			
			JSONObject jsonObj = (JSONObject) obj;
			
			jsonString = (String) jsonObj.get(object);
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		} catch ( ParseException e ) {
			e.printStackTrace();
		}
		return jsonString;
	}
	
}
