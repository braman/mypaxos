package node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import main.Constants;
import messages.LastVote;
import messages.PriestHeartBeat;
import storage.PersistentStorage;
import util.ConnectionManager;
import util.Utils;

public class Node {
	
	public enum StatusType{
		IDLE, TRYING, POLLING
	}
	
	public PersistentStorage persistence;
	
	public StatusType 	  status;
	public List<LastVote> prevVotes;
	public List<Priest>   quorum;
	public List<Priest>   voters;
	
	private Set<Priest>  all;
	
	public String 	decree;
	public Map<String, String> decreeParams;
	public Integer 	maxBallot;
	public String 	maxDecree;
	public Priest 	priest;
	
	public Node(Integer priestNo) {
		this("", priestNo);
	}
	
	
	public Node(String ip, Integer mypriestNo) {
		ip = "priest_" + Integer.valueOf(mypriestNo);
		
		priest 	= new Priest(ip, mypriestNo);
    	all 	= new LinkedHashSet<Priest>();
    	
    	/*outcome = null;
    	lastTried = all.size()-mypriest.priest+1;
    	prevBal = -1;
    	prevDec = null;
    	nextBal = -1;*/
    	
    	status 		= StatusType.IDLE;
    	prevVotes 	= new Vector<LastVote>();
    	quorum 		= new ArrayList<Priest>();
    	voters 		= new ArrayList<Priest>();
    	decree 		= null;
    	maxBallot 	= -1;
    	maxDecree 	= null;
    	
    	decreeParams = new HashMap<String, String>();
    	
    	pingThread(mypriestNo);
    	pongThread(mypriestNo);
    	
    }
	
	public void pingThread(Integer nodeId) {
		final PriestHeartBeat np = new PriestHeartBeat("priest_" + nodeId, nodeId);
		
		new Thread(new Runnable() { 
			public void run() {
				
				try {
					String json = Utils.toJSON(np);
					
					while(true) {
						ConnectionManager.getInstance("localhost").publishToTopic(Constants.getPriestsQueueName(), json);
						
						System.out.print(".");
						
						//System.out.println("Sending heart beat message:" + json);
						
						Thread.sleep(5000); //send every 5 seconds
					}
				
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			
				
				
			}
		}).start();;
		
		
	}
	
	
	public void pongThread(final Integer currentNodeId) {
		new Thread(new Runnable() {
			public void run() {
				try {
					ConnectionManager.getInstance("localhost").consumeTopic(Constants.getPriestsQueueName(), new ConnectionManager.Consumer() {
						
						@Override
						public boolean callback(String message) {
							
							PriestHeartBeat np = null;

							try {
								np = Utils.fromJSON(PriestHeartBeat.class, message);
							} catch (Exception e) {
								System.err.println("Not a NewPriest message");
								return false;
							}
							
							
							Priest p = new Priest(np.ip,  np.priestNo);
							p.lastSignal = System.currentTimeMillis();
							
							
							Priest oldPriest = findPriestByNo(np.priestNo);
							
							boolean notMeMyself = currentNodeId != null && !currentNodeId.equals(p.priestNo);
							
							if (notMeMyself) {
								if (oldPriest == null) {
									System.out.println("We have a new priest: " + message);
									all.add(p);
								} else {
									oldPriest.lastSignal = System.currentTimeMillis();
								}								
							}
							
							return false;
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private Priest findPriestByNo(Integer priestNo) {
		for (Priest p: all) {
			if (p.priestNo.equals(priestNo)) {
				return p;
			}
		}
		return null;
	}
	
	public int getAllPriestsCount() {
		int c = 0;
		
		synchronized (all) {
			for (Iterator<Priest> iterator = all.iterator(); iterator.hasNext(); ) {
			    Priest p = iterator.next();
			    
			    if (System.currentTimeMillis() - p.lastSignal > Constants.MAX_IDLE_TIMEOUT) {
			    	iterator.remove();
			    	
			    	
				} else {
					c++;
				}
			}
		}
		
		return c;
	}

	public synchronized Set<Priest> getAllActivePriests() {
		for (Iterator<Priest> iterator = all.iterator(); iterator.hasNext(); ) {
		    Priest p = iterator.next();
		    
		    if (System.currentTimeMillis() - p.lastSignal > Constants.MAX_IDLE_TIMEOUT) {
		    	iterator.remove();
			}
		}
		
		return all;
	}

	
	public boolean amILeader() {
		Integer lowestNo = null;
		
		for (Priest p: all) {
			if (lowestNo > p.priestNo) {
				lowestNo = p.priestNo;
			}
		}
		
		if (lowestNo.equals(priest.priestNo)) {
			return true;
		}
		
		return false;
	}
	
	public Priest getLeaderPriest() {
		Integer lowestNo = null;
		
		Priest leader = null;
		
		for (Priest p: all) {
			if (lowestNo > p.priestNo) {
				lowestNo = p.priestNo;
				leader = p;
			}
		}
		
		return leader;
	}
	
}
