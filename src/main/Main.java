package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import messages.BeginBallot;
import messages.LastVote;
import messages.Message;
import messages.MessageTypes;
import messages.NextBallot;
import messages.Request;
import messages.Success;
import messages.Voted;
import node.Node;
import node.Node.StatusType;
import node.Priest;
import storage.PersistentStorage;
import util.ConnectionManager;
import util.Utils;

public class Main {

	public static Node currentNode;

	public static void main(String[] args) throws IOException, InterruptedException {
		final Scanner in = new Scanner(System.in);
		
		while (true) {
			System.out.println("Enter node id: ");
			try {
				
				int n = Integer.parseInt(in.nextLine());
				Constants.setNodeId(n);

				break;
			} catch (Exception e) {
				System.err.println("Enter correct node id!!!");
			}
		}
		
		
		
		currentNode = new Node(Constants.getNodeId());
		
		PersistentStorage p = new PersistentStorage(currentNode.getAllPriestsCount() - currentNode.priest.priestNo + 1);
		p.save();
		p = p.load();
		currentNode.persistence = p;
		
		System.out.println("Starting from: " + currentNode.persistence.lastTried+", last successful outcome: " + currentNode.persistence.outcome);
		
		final Main mainProgram = new Main();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {

					System.out.println("Do you want to try a new ballot? [Y/N]");
					String instruction = in.nextLine();
					System.out.println();

					if ("Y".equalsIgnoreCase(instruction)) {
						
						currentNode.persistence.lastTried = (currentNode.persistence.lastTried + currentNode.getAllPriestsCount() + 1);
						currentNode.status 	  = StatusType.TRYING;
						currentNode.prevVotes = new ArrayList<LastVote>();
						currentNode.maxBallot = -1;
						currentNode.maxDecree = null;
						currentNode.persistence.outcome = null;

						NextBallot nextB = new NextBallot(currentNode.persistence.lastTried, currentNode.priest.ip);
						Message msg = new Message(nextB);
						
						for (Priest p : currentNode.getAllActivePriests()) {
							try {
								mainProgram.send(msg, "localhost", p.ip);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
						currentNode.persistence.save();
					}
				}
			}
		}).start();
		
		
		mainProgram.consume("localhost");
	}

	public void send(Message msg, String host, String queue) throws Exception {
		
		ConnectionManager.getInstance(host).publish(queue, msg.toJson());
		
		System.out.println("[" + msg.messageId + "] is sent to " + queue);
		
	}

	public void consume(String host) throws IOException {
		
		ConnectionManager.getInstance(host).consume(currentNode.priest.ip, new ConnectionManager.Consumer() {
			
			@Override
			public boolean callback(String message) {
				
				System.out.println("Received message: " + message);
				
				Message msg = Utils.fromJSON(Message.class, message);
				
				if (MessageTypes.NEXT_BALLOT.equals(msg.messageId)) {
					NextBallot b = Utils.fromJSON(NextBallot.class, msg.body);
					
					processNextBallotAsync(b);
				} else if (MessageTypes.BEGIN_BALLOT.equals(msg.messageId)) {
					BeginBallot b = Utils.fromJSON(BeginBallot.class, msg.body);
					
					processBeginBallotAsync(b);
				} else if (MessageTypes.LAST_VOTE.equals(msg.messageId)) {
					LastVote b = Utils.fromJSON(LastVote.class, msg.body);
					
					processLastVoteAsync(b);
				} else if (MessageTypes.VOTED.equals(msg.messageId)) {
					Voted b = Utils.fromJSON(Voted.class, msg.body);
					
					processVotedAsync(b);
				} else if (MessageTypes.SUCCESS.equals(msg.messageId)) {
					Success b = Utils.fromJSON(Success.class, msg.body);
					
					processSuccessAsync(b);
				} else if (MessageTypes.REQUEST.equals(msg.messageId)) {
					Request r = Utils.fromJSON(Request.class, msg.body);
					
					processRequestAsync(r);
				}
				
				return false;
			}
		});
		
	}

	
	protected void startBalloting() {
		NextBallot nextB = new NextBallot(currentNode.persistence.lastTried, currentNode.priest.ip);
		Message msg = new Message(nextB);
		
		for (Priest p : currentNode.getAllActivePriests()) {
			try {
				send(msg, "localhost", p.ip);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void processRequestAsync(final Request req) {
		new Thread(new Runnable() {
			public void run() {
				try {
					if (currentNode.amILeader()) {
						
					} else {
						Priest leader = currentNode.getLeaderPriest();
						
						Message forwardRequest = new Message(req);
						
						send(forwardRequest, "localhost", leader.ip);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	
	
	
	protected void processNextBallotAsync(final NextBallot b) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				if (b.ballotNo >= currentNode.persistence.nextBal) {
					currentNode.persistence.nextBal = b.ballotNo;
					
					if (currentNode.persistence.nextBal > currentNode.persistence.prevBal) {
						System.out.println("You agreed to participate in ballot " + b.ballotNo);
					
						LastVote lastVote = new LastVote(b.ballotNo, currentNode.priest, currentNode.persistence.prevBal, currentNode.persistence.prevDec);
						Message msg = new Message(lastVote);
						
						try {
							send(msg, "localhost", b.senderIP);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					currentNode.persistence.save();
				}
			}
		}).start();
	}

	protected void processLastVoteAsync(final LastVote v) {
		
		new Thread(new Runnable() {
		
			@Override
			public void run() {
				//System.out.println("ballotNo "+v.ballotNo + " lastTried "+me.lastTried+" status "+me.status +" and "+me.prevVotes.size()+" aaaan  "+me.quorum);
				if (v.ballotNo == currentNode.persistence.lastTried && currentNode.status == StatusType.TRYING){// && me.all.contains(v.priest)) {
					currentNode.prevVotes.add(v);
					currentNode.quorum.add(v.priest);
					
					if (v.lastBallotNo > currentNode.maxBallot) {
						currentNode.maxBallot = v.lastBallotNo;
						currentNode.maxDecree = v.decree;
					}
				}
				
				if (currentNode.status == StatusType.TRYING && (currentNode.getAllPriestsCount() + 1) / 2 <= currentNode.quorum.size()) {
					
					currentNode.status = StatusType.POLLING;
					currentNode.voters = new ArrayList<Priest>();
					
					if (currentNode.maxDecree == null) {
						System.out.println("You can choose a decree for	ballot " + currentNode.persistence.lastTried + ". Type decree:");
						currentNode.decree = currentNode.maxDecree;
						System.out.println();
					} else {
						currentNode.decree = currentNode.maxDecree;
					}
				}
				
				if (currentNode.status == StatusType.POLLING) {
					BeginBallot beginB = new BeginBallot(currentNode.persistence.lastTried, currentNode.decree, currentNode.priest.ip);
					Message msg = new Message(beginB);
					
					for (Priest p : currentNode.quorum) {
						try {
							send(msg, "localhost", p.ip);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

	protected void processBeginBallotAsync(final BeginBallot b) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
			
				if (b.ballotNo == currentNode.persistence.nextBal && currentNode.persistence.nextBal > currentNode.persistence.prevBal) {
					System.out.println("You agreed to vote in ballot " + b.ballotNo + " for decree " + b.decree);
					currentNode.persistence.prevBal = b.ballotNo;
					currentNode.persistence.prevDec = b.decree;
					
					Voted voted = new Voted(b.ballotNo, currentNode.priest);
					Message msg = new Message(voted);
					
					try {
						send(msg, "localhost", b.senderIP);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					currentNode.persistence.save();
				}
			}
		}).start();
	}

	protected void processVotedAsync(final Voted v) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (v.ballotNo == currentNode.persistence.lastTried && currentNode.status == StatusType.POLLING) {
					currentNode.voters.add(v.priest);
				}
				if (currentNode.status == StatusType.POLLING && currentNode.quorum.size() <= currentNode.voters.size() && currentNode.persistence.outcome == null) {
					currentNode.persistence.outcome = currentNode.decree;
					
					Success success = new Success(currentNode.persistence.outcome);
					Message msg = new Message(success);
					
					for (Priest p : currentNode.getAllActivePriests()) {
						try {
							send(msg, "localhost", p.ip);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				currentNode.persistence.save();
			}
		}).start();
	}

	protected void processSuccessAsync(final Success s) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (currentNode.persistence.outcome == null) {
					currentNode.persistence.outcome = s.outcome;
					currentNode.persistence.save();
				}
			}
		}).start();
	}
	
}
