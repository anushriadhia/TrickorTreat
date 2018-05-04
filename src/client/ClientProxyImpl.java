package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import stringProcessors.HalloweenCommandProcessor;
import util.interactiveMethodInvocation.IPCMechanism;

public class ClientProxyImpl implements RMIClientProxy, GIPCClientProxy {
	
	HalloweenCommandProcessor commandProcessor;
	String clientName;
	ClientParameterListener paramListener;
	
	public ClientProxyImpl(HalloweenCommandProcessor commandProcessor, String clientName, ClientParameterListener paramListener) throws RemoteException {
		super();
		this.commandProcessor = commandProcessor;
		this.clientName = clientName;
		this.paramListener = paramListener;
	}

	@Override
	public void processCommand(String cmd) {
		commandProcessor.processCommand(cmd);
	}
	
	public String getName(){
		return clientName;
	}

	@Override
	public void setAtomicBroadcast(boolean isAtomic) throws RemoteException {
		if(!paramListener.isWaitForBroadcastConsensus()) {
			paramListener.setAtomicBroadcast(isAtomic);
		}
		
	}

	@Override
	public void setIPCMechanism(IPCMechanism ipc) throws RemoteException {
		if(!paramListener.isWaitForIPCMechanismConsensus()) {
			paramListener.setIPCMechanism(ipc);
		}
		
	}

	@Override
	public boolean atomicRequest() throws RemoteException {
		paramListener.setWaitForBroadcastConsensus(true);
		return !paramListener.isRejectMetaStateChange();
	}

	@Override
	public boolean ipcRequest() throws RemoteException {
		paramListener.setWaitForIPCMechanismConsensus(true);
		return !paramListener.isRejectMetaStateChange();
	}

	@Override
	public void acceptAtomicRequest(boolean isAtomic) throws RemoteException {
		paramListener.setWaitForBroadcastConsensus(false);
		try {
			setAtomicBroadcast(isAtomic);
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void acceptIPCRequest(IPCMechanism ipc) throws RemoteException {
		paramListener.setWaitForIPCMechanismConsensus(false);
		try {
			setIPCMechanism(ipc);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
