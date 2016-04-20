package datacollector;

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
		db = mc.getDatabase("test");
		collection = db.getCollection("tweets");
	}
	@Override
	public Collection<Status> mungee(Collection<Status> src) {
		return src;
	}

	@Override
	public void save(Collection<Status> data) {
		List<Document> documents = data.stream()
				.map(item -> new Document()
					.append("user", item.getUser())
					.append("favorited", item.getFavoriteCount())
					.append("text", item.getText())
					.append("date", item.getCreatedAt()))
				.collect(Collectors.toList());
		collection.insertMany(documents);
	}

}
