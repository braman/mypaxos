package messages;

import util.Utils;

public class NextBallot implements Jsonable {
	
	public Integer ballotNo;	
	public String senderIP;
	
	public NextBallot(Integer bNo, String sIP) {
		ballotNo = bNo;
		senderIP = sIP;
	}
	
	@Override
	public String toString()  {
		return String.format("ballotNo:%s", ballotNo); 
	}
	
	public String toJson() {
		return Utils.toJSON(this);
	}

	@Override
	public String getMessageType() {
		return MessageTypes.NEXT_BALLOT;
	}
}
