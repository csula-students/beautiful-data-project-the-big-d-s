package datacollector;

import java.util.Collection;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;

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
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
	}
}
