package datacollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;

import twitter4j.Status;

import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.csula.datascience.acquisition.Collector;

public class YoutubeCollector implements Collector<CommentSnippet, CommentThread> {

	MongoClient mc;
	MongoDatabase db;
	MongoCollection<Document> collection;
	private String videoTitle;
	private String videoCreatedDate;
	
	public YoutubeCollector(){
		mc = new MongoClient();
		db = mc.getDatabase("youtubetest2");
		collection = db.getCollection("ycomments");
	}
	
	@Override
	public Collection<CommentSnippet> mungee(Collection<CommentThread> inList) {
		int dirty = 0;
		List<CommentSnippet> cleanList = new ArrayList<CommentSnippet>();
		for (CommentThread comment: inList){
			CommentSnippet snip = comment.getSnippet().getTopLevelComment().getSnippet();
			if (snip.getTextDisplay() == null || snip.getPublishedAt() == null || snip.getAuthorDisplayName() == null){
				dirty++;
				continue;
			}
			else{
				cleanList.add(snip);
			}
		}
		System.out.println(dirty + " of comments removed.");
		dirty = 0;
		return cleanList;
	}

	@Override
	public void save(Collection<CommentSnippet> inList) {
		
		if (!inList.isEmpty()){
			List<Document> documents = inList.stream()
					.map(item -> new Document()
						.append("user", item.getAuthorDisplayName())
						.append("like", item.getLikeCount())
						.append("text", item.getTextDisplay())
						.append("date", item.getPublishedAt().toString())
						.append("video_title", videoTitle)
						.append("video_date", videoCreatedDate))
					.collect(Collectors.toList());
			collection.insertMany(documents);
			System.out.println("Save successful.");
		}

	}
	public void retrieveVideoTitle(String videoTitle){
		this.videoTitle = videoTitle;
	}

	public void retrieveVideoDate(String videoCreatedDate){
		this.videoCreatedDate = videoCreatedDate;
	}
}
