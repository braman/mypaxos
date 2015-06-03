package messages;

import java.util.HashMap;
import java.util.Map;

import util.Utils;

public class BeginBallot implements Jsonable {
	public Integer ballotNo;
	public String decree;
	public Map<String, String> decreeParams;
	public String senderIP;
	
	public BeginBallot(Integer bNo, String d, String sIP) {
		ballotNo = bNo;
		decree = d;
		senderIP = sIP;
		decreeParams = new HashMap<String, String>();
	}
	
	@Override
	public String toString() {
		return String.format("ballotNo:%s,decree:%s,senderIP:%s", ballotNo, decree, senderIP); 
	}
	
	public String toJson() {
		return Utils.toJSON(this);
	}

	@Override
	public String getMessageType() {
		return MessageTypes.BEGIN_BALLOT;
	}
}
