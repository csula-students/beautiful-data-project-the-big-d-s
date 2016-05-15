package datacollectortests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import twitter4j.Status;

import com.google.common.collect.Lists;

import datacollector.TwitterCollectorD;
import edu.csula.datascience.acquisition.Collector;
import edu.csula.datascience.acquisition.MockCollector;
import edu.csula.datascience.acquisition.MockData;
import edu.csula.datascience.acquisition.MockSource;
import edu.csula.datascience.acquisition.SimpleModel;
import edu.csula.datascience.acquisition.Source;

public class TwitterTest {
	 private Collector<MockStatus, MockStatus> collector;
	    private Source<MockData> source;

	    @Before
	    public void setup() {
	        collector = new TwitterCollectorTest();
	        source = new MockSource();
	    }
	    
	    @Test
	    public void mungee() throws Exception {
	        List<MockStatus> dataList = Lists.newArrayList(
	            new MockStatus("John", "hello", "2015"),
	            new MockStatus("Alex", "goodbye", "2014"),
	            new MockStatus("Tammy", null, "2014")
	        );
	        List<MockStatus> list = (List<MockStatus>) collector.mungee(dataList);
	        Assert.assertEquals(list.size(), 2);
	        Assert.assertEquals(list.get(0).getName(), "John");
	        
	    }
	    
	    @Test
	    public void mungee2() throws Exception {
	        List<MockStatus> dataList = Lists.newArrayList(
	            new MockStatus("John", "hello", "2015"),
	            new MockStatus("Alex", "goodbye", "2014"),
	            new MockStatus("Tammy", "hi", "2014")
	        );
	        List<MockStatus> list = (List<MockStatus>) collector.mungee(dataList);
	        Assert.assertEquals(list.size(), 3);
	        Assert.assertEquals(list.get(2).getText(), "hi");
	        
	    }

}
