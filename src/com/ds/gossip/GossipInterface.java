package com.ds.gossip;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GossipInterface extends Remote{
	
	public void hearGossip(byte[] result) throws RemoteException;
}
