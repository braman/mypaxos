package messages;

import util.Utils;

public class Success implements Jsonable {

	public String outcome;	
	
	public Success(String o) {
		outcome = o;
	}
	
	@Override
	public String toString() {
		return String.format("outcome:%s", outcome); 
	}
	
	public String toJson() {
		return Utils.toJSON(this);
	}

	@Override
	public String getMessageType() {
		return MessageTypes.SUCCESS;
	}
}
