package datacollector;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
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
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.common.collect.Lists;

public class Demo{
	private static YouTube youtube;
	//private static YouTube youtubeComments;
	private static String apiKey = "AIzaSyCerMe9K8HTxXV_-LO52Vbe7miAWyC1-08";
	private static DateTime videoDate = new DateTime(new Date());
	
	public static void main(String[] args){
		 youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
             public void initialize(HttpRequest request) throws IOException {
             }
         }).setApplicationName("youtube-cmdline-search-sample").build();
		 /*
		 youtubeComments = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
             public void initialize(HttpRequest request) throws IOException {
             }
         }).setApplicationName("youtube-cmdline-commentthreads-sample").build();
         */
		 
		 
		 String queryTerm = "Donald Trump";
		 try {
			YouTube.Search.List search = youtube.search().list("id, snippet");
			search.setKey(apiKey);
			search.setQ(queryTerm);
			search.setType("video");
			search.setOrder("date");
			//search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url");
			search.setMaxResults((long) 1);
			DateTime curDate = videoDate;
			//search.setPublishedBefore(curDate);
			
			SearchListResponse searchResponse = search.execute();
			List<SearchResult> searchResultList = searchResponse.getItems();
			print(searchResultList.iterator(), queryTerm);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void print(Iterator<SearchResult> iteratorSearchResults, String query){
		System.out.println("You searched for: " + query);
		while (iteratorSearchResults.hasNext()){
			SearchResult singleVideo = iteratorSearchResults.next();
			ResourceId rId = singleVideo.getId();
			System.out.println("Video ID: " + rId.getVideoId());
			System.out.println("Title: " + singleVideo.getSnippet().getTitle());
			System.out.println("Created Date : " + singleVideo.getSnippet().getPublishedAt());
			
			Long dateValue = singleVideo.getSnippet().getPublishedAt().getValue() - 1;
			videoDate = new DateTime(dateValue);
			
			
			//List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");
			
			
		
		if (rId.getKind().equals("youtube#video")){
			try {
				CommentThreadListResponse videoCommentsListResponse = youtube.commentThreads()
						.list("snippet")
						.setKey(apiKey)
						.setVideoId(rId.getVideoId())
						.setTextFormat("plainText")
						.execute();
				List<CommentThread> videoComments = videoCommentsListResponse.getItems();
				if (!videoComments.isEmpty()){
					for (CommentThread comment : videoComments){
						CommentSnippet snip = comment.getSnippet().getTopLevelComment().getSnippet();
						System.out.println("User: " + snip.getAuthorDisplayName());
						System.out.println("Comment: " + snip.getTextDisplay());
						
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
		
		}
	}
}
