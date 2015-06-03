package util;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Utils {
	
	public static boolean isValidIp(String ip) {
		final String PATTERN = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		return ip != null && ip.matches(PATTERN);
	}
	
	public static String randomDecreeGenerator(String ... decrees) {
		Random r = new Random();
		int i = r.nextInt(decrees.length);

		return decrees[i];
	}
	
	public static InetAddress getLocalAddress(){
		try {
			Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
			while( b.hasMoreElements()){
				for ( InterfaceAddress f : b.nextElement().getInterfaceAddresses())
					if ( f.getAddress().isSiteLocalAddress())
						return f.getAddress();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static Integer genBallotNo(int from) {
		return from + 17;
	}
	
	
	public static String toJSON(Object o) {
		Gson g = getGSON();
		return g.toJson(o);
	}
	
	public static<T> T fromJSON(Class<T> c, String json) {
		Gson g = getGSON();
		return g.fromJson(json, c);
	}
	
	private static Gson getGSON() {
		GsonBuilder builder = new GsonBuilder();
		return builder.create();
	}
	
}


