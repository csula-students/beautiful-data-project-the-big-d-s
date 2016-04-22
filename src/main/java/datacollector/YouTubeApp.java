package datacollector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

public class YouTubeApp {
	private static String query = "Donald Trump Hillary Clinton Bernie Sanders, John Kasich";
	public static void main(String[] args) {
		YoutubeSource ys = new YoutubeSource(query);
		YoutubeCollector yc = new YoutubeCollector();
		while (ys.hasNext()){
			Collection<CommentThread> videoComments = ys.next();
			Collection<CommentSnippet> cleanList = yc.mungee(videoComments);
			yc.retrieveVideoTitle(ys.getVideoTitle());
			yc.retrieveVideoDate(ys.getVideoCreatedDate());
			yc.save(cleanList);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
