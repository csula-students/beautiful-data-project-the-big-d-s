package datacollector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterSourceD{
	private static List<Status> twitterStatus = new ArrayList<Status>();
	private static List<Status> cleanedStatus = new ArrayList<Status>();
	private static int counter = 0;
	private static Map<String, String> keyMap = new HashMap<String,String>();
	public static void main(String[] args){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("key.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String key : properties.stringPropertyNames()) {
			   keyMap.put(key, properties.get(key).toString());
			}
		
		
		TwitterCollectorD tCollector = new TwitterCollectorD();
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey(keyMap.get("twitterCKey"))
            .setOAuthConsumerSecret(keyMap.get("twitterCKeySecret"))
            .setOAuthAccessToken(keyMap.get("twitterAToken"))
            .setOAuthAccessTokenSecret(keyMap.get("twitterATokenSecret"));
		TwitterStream ts = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener(){
			public void onStatus(Status status) {
	            //System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
	            if (counter == 100){
	            	System.out.println("Cleaning data...");
	            	cleanedStatus.addAll(tCollector.mungee(twitterStatus));
	            	System.out.println("Attempting to save...");
	            	tCollector.save(cleanedStatus);
	            	twitterStatus.clear();
	            	cleanedStatus.clear();
	            	counter = 0;
	            }
	            else{
	            	twitterStatus.add(status);
	            	counter++;
	            }
	        }

	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	            System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
	        }

	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	            System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
	        }

	        public void onScrubGeo(long userId, long upToStatusId) {
	            System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
	        }

	        public void onException(Exception ex) {
	            ex.printStackTrace();
	        }
			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Stall warning.");
			}
		};
		
		FilterQuery q = new FilterQuery();
		String candidates[] = {"Donald Trump", "Hillary Clinton", "Bernie Sanders", "John Kasich"};
		
		q.track(candidates);
		q.language("en");
		ts.addListener(listener);
		ts.filter(q);

	}
}
