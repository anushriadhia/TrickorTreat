package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import client.CommandObject;
import client.RMIClientProxy;
import client.ClientProxyImpl;
import util.interactiveMethodInvocation.IPCMechanism;

public interface RMIServerProxy extends Remote {
	void rmiSimulationCommand (String cmd, boolean isAtomic, String clientName) throws RemoteException;
	void registerToRMIServer (RMIClientProxy client, String clientName) throws RemoteException;
	void setAtomicBroadcast(boolean isAtomic, String clientName) throws RemoteException;
	void setIPCMechanism(IPCMechanism ipc, String clientName) throws RemoteException;
	void proposeAtomic(boolean isAtomic, String clientName) throws RemoteException;
	void proposeIPC(IPCMechanism ipc, String clientName) throws RemoteException;
}
