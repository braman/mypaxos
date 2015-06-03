package messages;
import util.Utils;

public class Message implements Jsonable {
	public String body;
	public String messageId;

	public Message(String messageId, Jsonable body) {
		this.messageId = messageId;
		this.body = body.toJson();
	}
	
	public Message(Jsonable json) {
		body = json.toJson();
		messageId = json.getMessageType();
	}
	
	@Override
	public String toString() {
		return String.format("body:%s,messageID:%s", body, messageId); 
	}
	
	public String toJson() {
		return Utils.toJSON(this);
	}

	@Override
	public String getMessageType() {
		return null;
	}

	
}
