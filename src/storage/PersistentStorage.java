package storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import main.Constants;

public class PersistentStorage implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5337394889133152667L;
	
	public String  outcome;
	public Integer lastTried;
	public Integer prevBal;
	public String  prevDec;
	public Integer nextBal;
	
	public PersistentStorage(Integer lastT, Integer pBal, Integer nBal, String o, String pDec) {
		outcome = o;
    	lastTried = lastT;
    	prevBal = pBal;
    	prevDec = pDec;
    	nextBal = nBal;
	}
	
	public PersistentStorage(Integer lastT) {
		outcome = null;
    	lastTried = lastT;
    	prevBal = -1;
    	prevDec = null;
    	nextBal = -1;
	}
	
	public boolean save() {
		try {
	         FileOutputStream fileOut = new FileOutputStream(Constants.getPersistentStorageFilePath());
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(this);
	         out.close();
	         fileOut.close();
	         System.out.println("Serialized data is saved in " + Constants.getPersistentStorageFilePath());
	      }catch(IOException i) {
	          i.printStackTrace();
	      }
		return true;
	}
	
	public PersistentStorage load() {
		try {
	         FileInputStream fileIn = new FileInputStream(Constants.getPersistentStorageFilePath());
	         ObjectInputStream inO = new ObjectInputStream(fileIn);
	         PersistentStorage p = (PersistentStorage) inO.readObject();
	         if(p.lastTried != null)
	        	 this.lastTried = p.lastTried;
	         if(p.prevBal != null)
	        	 this.prevBal = p.prevBal;
	         if(p.outcome != null)
	        	 this.outcome = p.outcome;
	         if(p.prevDec != null)
	        	 this.prevDec = p.prevDec;
	         if(p.nextBal != null)
	        	 this.nextBal = p.nextBal;
	         inO.close();
	         fileIn.close();
	      } catch(IOException i) {
	         i.printStackTrace();
	      } catch(ClassNotFoundException c) {
	         System.out.println("Persistent class not found");
	         c.printStackTrace();
	      }
		
		return this;
	}
}
