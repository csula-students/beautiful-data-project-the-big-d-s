package datacollectortests;

public class MockStatus {
	private final String user;
	private final String text;
	private final String date;
	
	public MockStatus(String user, String text, String date){
		this.user = user;
		this.text = text;
		this.date = date;
	}

	public String getName() {
		return user;
	}

	public String getText() {
		return text;
	}

	public String getCreatedAt() {
		return date;
	}
	
}
