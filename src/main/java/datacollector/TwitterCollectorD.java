package datacollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import twitter4j.Status;
import edu.csula.datascience.acquisition.Collector;

public class TwitterCollectorD implements Collector<Status, Status>{
	MongoClient mc;
	MongoDatabase db;
	MongoCollection<Document> collection;

	public TwitterCollectorD() {
		mc = new MongoClient();
		db = mc.getDatabase("twitter");
		collection = db.getCollection("tweets21");
	}
	@Override
	public Collection<Status> mungee(Collection<Status> inList) {
		int dirty = 0;
		List<Status> cleanList = new ArrayList<Status>();
		for (Status tweet: inList){//int is not checked because an int can not be null.
			if (tweet.getUser().getName() == null || tweet.getText() == null || tweet.getCreatedAt() == null){
				dirty++;
				continue;
			}
			else{
				cleanList.add(tweet);
			}
		}
		System.out.println(dirty + " of tweets removed.");
		dirty = 0;
		return cleanList;
	}

	@Override
	public void save(Collection<Status> data) {
		List<Document> documents = data.stream()
				.map(item -> new Document()
					.append("user", item.getUser().getName())
					.append("favorited", item.getFavoriteCount())
					.append("text", item.getText())
					.append("date", item.getCreatedAt()))
				.collect(Collectors.toList());
		collection.insertMany(documents);
		System.out.println("Save successful.");
	}

}
