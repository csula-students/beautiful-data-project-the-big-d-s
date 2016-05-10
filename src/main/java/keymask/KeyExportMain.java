package keymask;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KeyExportMain {
	public static void main(String[] args){
		Map<String, String> keyMap = new HashMap<String, String>();
		
		//enter your keys below
		String twitterCKey = "";
		String twitterCKeySecret = "";
		String twitterAToken = "";
		String twitterATokenSecret = "";
		
		String youtubeAPIKey = "";
		
		keyMap.put("twitterCKey", twitterCKey);
		keyMap.put("twitterCKeySecret", twitterCKeySecret);
		keyMap.put("twitterAToken", twitterAToken);
		keyMap.put("twitterATokenSecret", twitterATokenSecret);
		keyMap.put("youtubeAPIKey", youtubeAPIKey);
		
		Properties properties = new Properties();{
			for (Map.Entry<String, String> entry : keyMap.entrySet()){
				properties.put(entry.getKey(), entry.getValue());
			}
			try {
				properties.store(new FileOutputStream("key.properties"), null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
