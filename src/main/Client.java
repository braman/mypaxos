package main;

import java.io.IOException;

import messages.Message;
import messages.Request;
import util.ConnectionManager;

public class Client {

	
	public static void main(String[] args) throws IOException {
		Request r = new Request("decree_1");
		r.decreeParams.put("param1", "value1");
		r.decreeParams.put("param2", "value2");
		r.decreeParams.put("param3", "value3");
		
		Message msg = new Message(r);
		
		
		ConnectionManager.getInstance("localhost").publish("priest_1", msg.toJson());
	}
	
}
