package messages;

public class LastData {
	public Integer lastTried;
	public Integer nextBal;
	public Integer prevVote;
	
	
	@Override
	public String toString() {
		return String.format("lastTried:%s,nextBal:%s,prevVote:%s", lastTried, nextBal,prevVote); 
	}
}
