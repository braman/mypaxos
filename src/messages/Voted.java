package messages;

import node.Priest;
import util.Utils;

public class Voted implements Jsonable {
	
	public Integer ballotNo;
	public Priest priest;
	
	public Voted(Integer ballotNo, Priest priest) {
		this.ballotNo = ballotNo;
		this.priest = priest;
	}
	
	@Override
	public String toString() {
		return String.format("ballotNo:%s,priest:%s", ballotNo, priest); 
	}

	public String toJson() {
		return Utils.toJSON(this);
	}

	@Override
	public String getMessageType() {
		return MessageTypes.VOTED;
	}
}
