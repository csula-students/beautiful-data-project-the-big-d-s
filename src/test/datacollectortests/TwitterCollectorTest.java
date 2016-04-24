package datacollectortests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.csula.datascience.acquisition.Collector;

public class TwitterCollectorTest implements Collector<MockStatus, MockStatus> {

	@Override
	public Collection<MockStatus> mungee(Collection<MockStatus> inList) {
		int dirty = 0;
		List<MockStatus> cleanList = new ArrayList<MockStatus>();
		for (MockStatus tweet: inList){
			if (tweet.getName() == null || tweet.getText() == null || tweet.getCreatedAt() == null){
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
	public void save(Collection<MockStatus> data) {
		
	}

}
