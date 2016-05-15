package datacollector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import edu.csula.datascience.acquisition.Source;

public class YoutubeSource implements Source<CommentThread>{

	private YouTube youtube;
	private String apiKey;
	private DateTime videoDate = new DateTime(new Date());
	private String query;
	private String videoTitle;
	private String videoCreatedDate;
	
	public YoutubeSource(String query){
		Map<String, String> keyMap = new HashMap<String,String>();
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
		apiKey = keyMap.get("youtubeAPIKey");
		
		 youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
             public void initialize(HttpRequest request) throws IOException {
             }
         }).setApplicationName("youtube-cmdline-search-sample").build();
		 this.query = query;
		 
	}
	
	private ResourceId search(Iterator<SearchResult> iteratorSearchResults){
		System.out.println("You searched for: " + query);
		ResourceId rId = null;
		while (iteratorSearchResults.hasNext()){
			SearchResult singleVideo = iteratorSearchResults.next();
			rId = singleVideo.getId();
			System.out.println("Video ID: " + rId.getVideoId());
			System.out.println("Title: " + singleVideo.getSnippet().getTitle());
			System.out.println("Created Date : " + singleVideo.getSnippet().getPublishedAt());
			videoTitle = singleVideo.getSnippet().getTitle();
			videoCreatedDate = singleVideo.getSnippet().getPublishedAt().toString();
			
			Long dateValue = singleVideo.getSnippet().getPublishedAt().getValue() - 1;
			//Long dateValue = singleVideo.getSnippet().getPublishedAt().getValue() + 1;
			videoDate = new DateTime(dateValue);
		}
		return rId;
	}
	
	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public Collection<CommentThread> next() {
		String queryTerm = query;
		List<CommentThread> videoComments = new ArrayList<CommentThread>();
		 try {
			YouTube.Search.List search = youtube.search().list("id, snippet");
			search.setKey(apiKey);
			search.setQ(queryTerm);
			search.setType("video");
			search.setOrder("Date");
			//search.setOrder("viewCount");
			search.setMaxResults((long) 1);
			DateTime curDate = videoDate;
			//search.setPublishedAfter(curDate);
			search.setPublishedBefore(curDate);
			
			SearchListResponse searchResponse = search.execute();
			List<SearchResult> searchResultList = searchResponse.getItems();
			ResourceId resourceid = search(searchResultList.iterator());
			
			if (resourceid.getKind().equals("youtube#video")){
				try {
					CommentThreadListResponse videoCommentsListResponse = youtube.commentThreads()
							.list("snippet")
							.setKey(apiKey)
							.setVideoId(resourceid.getVideoId())
							.setTextFormat("plainText")
							.execute();
					videoComments = videoCommentsListResponse.getItems();
					if (!videoComments.isEmpty()){
						for (CommentThread comment : videoComments){
							CommentSnippet snip = comment.getSnippet().getTopLevelComment().getSnippet();
							System.out.println("User: " + snip.getAuthorDisplayName());
							System.out.println("Comment: " + snip.getTextDisplay());
							System.out.println("Date: " + snip.getPublishedAt().toString());
							System.out.println("Likes: " + snip.getLikeCount());
							
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return videoComments;
	}

	public String getVideoTitle(){
		return videoTitle;
	}
	public String getVideoCreatedDate(){
		return videoCreatedDate;
	}
}
