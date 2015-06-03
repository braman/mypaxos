package messages;

import java.util.HashMap;
import java.util.Map;

import node.Priest;
import util.Utils;

public class LastVote implements Jsonable {
	
	public Integer ballotNo;
	public Priest  priest;
	public Integer lastBallotNo;
	public String  decree;
	public Map<String, String> decreeParams;
	
	public LastVote(Integer bNo, Priest p, Integer lbNo, String d) { 
		ballotNo 	 = bNo;
		priest 		 = p;
		lastBallotNo = lbNo;
		decree 		 = d;
		decreeParams = new HashMap<String, String>();
	}
	
	@Override
	public String toString() {
		return String.format("ballotNo:%s,priest:%s,lastBallotNo:%s,decree:%s", ballotNo, priest, lastBallotNo, decree); 
	}

	public String toJson() {
		return Utils.toJSON(this);
	}

	@Override
	public String getMessageType() {
		return MessageTypes.LAST_VOTE;
	}
	
}
