package com.ds.gossip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import com.ds.gossip.GossipProto.Clock;
import com.ds.gossip.GossipProto.GossipMessage;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;

public class MyServer implements GossipInterface{

	public static String newFileName;
	public static int numServers;
	public static int myClock;
	public static int id;
	public static int[] myLocalClock;
	public static Registry registry;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		id = Integer.parseInt(args[0]);
		numServers = Integer.parseInt(args[1]);
		myClock=0;
		myLocalClock = new int[numServers];
		for (int j = 0; j < numServers; j++) {
			myLocalClock[j]=0;
		}
		bindToRegistry(id);
		
		
		System.out.println("Hi I am Server "+id);
		System.out.println("Total servers "+numServers);
		
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if(args.length==4)
		{
			System.out.println("I am a generator Process");
			String fileName = args[3];
			writeNewFile(fileName, id);
			sendMessages();
		}else
		{
			System.out.println("I am not a generator Process");
		}
		
		
		
		

	}
	
	
	private static void sendMessages() {
		// TODO Auto-generated method stub
		
		GossipMessage.Builder Msg3 = GossipMessage.newBuilder();
		int cnt=0;
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(newFileName));
			String line;
			while((line = buff.readLine())!=null)
			{
				cnt++;
				
				
				myClock++;
				myLocalClock[id-1]=myClock;
				Clock.Builder clk = Clock.newBuilder();
				clk.setCount(myClock);
				clk.setPID(id);
				
				GossipMessage.Builder gMsg = GossipMessage.newBuilder();
				gMsg.setClock(clk);
				gMsg.setMsg(line);
						
				/* Convert to byte array */
				GossipMessage finalMsg = gMsg.build();
			    
			    processGossip(finalMsg);
			    
			    if(cnt==3)
			    {
			    	Msg3=gMsg;
			    }
			    
//			    processMessage(result);
			    
				
			}
			
			buff.close();
			
			//Uncomment following to send out of order
			/*GossipMessage finalMsg = Msg3.build();
		    
		    processGossip(finalMsg); */
			
			deleteFile(newFileName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		
	}

	
	public static void processGossip(GossipMessage finalMsg)
	{
		
		
		/* Convert to byte array */
		byte[] result = new byte[finalMsg.getSerializedSize()];
	    final CodedOutputStream output = CodedOutputStream.newInstance(result);
	    try {
			finalMsg.writeTo(output);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    output.checkNoSpaceLeft();
		
		
	    int random;
	    int random2;
	 /*   do
	    {
	    	random = getRandom(id);
	    }while(random==finalMsg.getClock().getPID());
	    
	    do
		{
			random2 = getRandom(id);
		}while(random==random2 || random2==finalMsg.getClock().getPID());*/
		
	    random = getRandom(id);
	   
	    
	Runnable r = new Runnable() {
				     public void run() {
				    		try {
				    			GossipInterface stub=null;
				    			stub = (GossipInterface) registry.lookup("Gossip_"+random);
				    			
				    			stub.hearGossip(result);
							} catch (RemoteException | NotBoundException e) {
								// TODO Auto-generated catch block
//								e.printStackTrace();
								System.out.println("Ignore");
							}
				     }
				 };
	
				
				 
				 
		 Runnable r1 = new Runnable() {
		     public void run() {
		    		try {
		    			GossipInterface stub1=null;
		    			
		    			int random2;
		    			 do
		    				{
		    					random2 = getRandom(id);
		    				}while(random==random2);
		    			
		    			stub1 = (GossipInterface) registry.lookup("Gossip_"+random2);
		    			
		    			stub1.hearGossip(result);
					} catch (RemoteException | NotBoundException e) {
						// TODO Auto-generated catch block
						System.out.println("Ignore");
					}
		     }
		 };
		 		 new Thread(r).start();
				 new Thread(r1).start();
	}
	
	
	
	
	

	static void bindToRegistry(int id)
	{
		MyServer obj = new MyServer();
		try {
			Registry register;
			GossipInterface stub = (GossipInterface) UnicastRemoteObject.exportObject(obj,0);
		
			try{
				
				register = LocateRegistry.createRegistry(10000);
			}catch(Exception e)
			{
				register = LocateRegistry.getRegistry("127.0.0.1",10000);
			}
			
			registry=register;
			register.rebind("Gossip_"+id, stub);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void writeNewFile(String fileName,int id)
	{
		
		try {
			
			BufferedReader buff = new BufferedReader(new FileReader(fileName));
			String line;
			
			PrintWriter pw = null;
			StringBuilder sb = new StringBuilder();
		
			
			
			while((line = buff.readLine())!=null)
			{

				sb.append(id+":"+line);	
				sb.append("\n");
					
			}
			
			buff.close();
			newFileName = fileName+id;
			pw = new PrintWriter(new FileWriter(newFileName));
	        pw.write(sb.toString());
	        pw.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


	@Override
	public void hearGossip(byte[] result) throws RemoteException {
		// TODO Auto-generated method stub
		
//
//		myClock++;
//		myLocalClock[id-1]=myClock;
		
		try {
			GossipMessage msg = GossipMessage.parseFrom(result);
			int clock = msg.getClock().getCount();
			int pid = msg.getClock().getPID();
			
			
//			System.out.println("My clock for same pid "+ myLocalClock[pid-1]);
//			System.out.println("Message,clock,pid ( "+ msg.getMsg()+" , "+clock +" , "+pid+" )");
//			
			
			try {

				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			if(myLocalClock[pid-1]==clock)
			{
				//duplicate message
				if(pid!=id)
					System.out.println("Duplicate "+msg.getMsg());
				
			}
			else if(myLocalClock[pid-1]<clock)
			{

				System.out.println("Accept "+msg.getMsg());
				myLocalClock[pid-1]=clock;
				
				processGossip(msg);
				
				
				
			}else
			{
				//invalid message
				System.out.println("Reject "+msg.getMsg());
			}
			
			
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	
	public static int getRandom(int pid)
	{
		int random;
		Random rand =new Random();
		int min=1;
		do
		{
			random = rand.nextInt((numServers - min) + 1) + min;
		}
	    while(random==pid) ;
		return random;
	}
	
	
	
	public static void processMessage(byte[] result)
	{
			
		
		int random = getRandom(id);
		int random2;
		
		try {
			
			do
			{
				random2 = getRandom(id);
			}while(random==random2);
			
			System.out.println("Sending message to "+random +" and "+random2);
			
			
			GossipInterface stub=null;
			GossipInterface stub1=null;
			try {
				stub = (GossipInterface) registry.lookup("Gossip_"+random);
				stub1 = (GossipInterface) registry.lookup("Gossip_"+random2);
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			stub.hearGossip(result);
			stub1.hearGossip(result);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Getting the ssssregistry faild");
		}
	}

	public static void deleteFile(String fileName)
	{
		boolean success = (new File(fileName)).delete();
		
		if(!success)
		{
			System.out.print("Failed to delete");
		}
	}
}