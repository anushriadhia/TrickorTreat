 package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import util.interactiveMethodInvocation.IPCMechanism;

public interface RMIClientProxy extends Remote {
	void setAtomicBroadcast(boolean isAtomic) throws RemoteException;
	void setIPCMechanism(IPCMechanism ipc) throws RemoteException;
	boolean atomicRequest() throws RemoteException;
	boolean ipcRequest() throws RemoteException;
	void acceptAtomicRequest(boolean isAtomic) throws RemoteException;
	void acceptIPCRequest(IPCMechanism ipc) throws RemoteException;
	void processCommand(String cmd) throws RemoteException;

}
