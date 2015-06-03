package messages;

import java.util.Map;

import util.Utils;

public class Request implements Jsonable {

	public String decree;
	public Map<String, String> decreeParams;
	
	public Request(String decree) {
		this.decree = decree;
	}
	
	@Override
	public String toJson() {
		return Utils.toJSON(this);
	}

	@Override
	public String getMessageType() {
		return MessageTypes.REQUEST;
	}
}
