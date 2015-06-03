package main;

import java.io.File;

public class Constants {
	
	public static final String PERSISTENT_STORAGE_FOLDER = "tmp";
	public static final String PERSISTENT_STORAGE_FILE_NAME = "backup.dat";
	public static Integer nodeId = 0;
	
	public static final int MAX_IDLE_TIMEOUT = 10000; //10 sec 
	
	public static void setNodeId(Integer id) {
		nodeId = id;
	}
	
	public static Integer getNodeId() {
		return nodeId;
	}
	
	public static final String getPersistentStorageFilePath() {
		return Constants.PERSISTENT_STORAGE_FOLDER + File.separator + nodeId + "_" + Constants.PERSISTENT_STORAGE_FILE_NAME;
	}
	
	
	public static String getPriestsQueueName() {
		return "broadcast_priests_queue";
	}
}
