package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import client.ClientProxyImpl;
import client.GIPCClientProxy;
import client.RMIClientProxy;
import util.interactiveMethodInvocation.IPCMechanism;

public class ServerProxyImpl extends UnicastRemoteObject implements RMIServerProxy, GIPCServerProxy {
	
	HashMap<String, RMIClientProxy> RMIclientMap = new HashMap<String, RMIClientProxy>();
	HashMap<String, GIPCClientProxy> GIPCclientMap = new HashMap<String, GIPCClientProxy>();
	
//	ArrayList<CommandProcessorProxy> clientList = new ArrayList<CommandProcessorProxy>();

	protected ServerProxyImpl() throws RemoteException {
		super();
	}

	@Override
	public void gipcSimulationCommand(String cmd, boolean isAtomic, String clientName) {
		GIPCclientMap.forEach((k,v) -> {
			if(isAtomic ||!k.equals(clientName)) {
				v.processCommand(cmd);
			}
		});
	}
	
	@Override
	public void rmiSimulationCommand(String cmd, boolean isAtomic, String clientName) throws RemoteException {
		RMIclientMap.forEach((k,v) -> {
			if(isAtomic || !isAtomic && k!=clientName) {
				try {
					v.processCommand(cmd);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void registerToRMIServer(RMIClientProxy client, String clientName) throws RemoteException {
		RMIclientMap.put(clientName, client);
		
	}
	
	@Override
	public void registerToGIPCServer(GIPCClientProxy client, String clientName) {
		GIPCclientMap.put(clientName, client);
	}

	@Override
	public void setAtomicBroadcast(boolean isAtomic, String clientName) throws RemoteException {
		RMIclientMap.forEach((k,v)-> {
			if(k!=clientName) {
				try {
					v.setAtomicBroadcast(isAtomic);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void setIPCMechanism(IPCMechanism ipc, String clientName) throws RemoteException {
		RMIclientMap.forEach((k,v)-> {
			if(k!=clientName) {
				try {
					v.setIPCMechanism(ipc);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	

	@Override
	public void proposeAtomic(boolean isAtomic, String clientName) throws RemoteException {
		boolean changeAtomic = true;
		for(String name: RMIclientMap.keySet()) {
			if(name!=clientName) {
				if(!RMIclientMap.get(name).atomicRequest()) {
					changeAtomic = false;
					break;
				}
			}
		}
		if(changeAtomic) {
			try {
				setAtomicBroadcast(isAtomic, clientName);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void proposeIPC(IPCMechanism ipc, String clientName) throws RemoteException {
		boolean changeIPC = true;
		for(String name: RMIclientMap.keySet()) {
			if(name!=clientName) {
				if(!RMIclientMap.get(name).ipcRequest()) {
					changeIPC = false;
					break;
				}
			}
		}
		if(changeIPC) {
			try {
				setIPCMechanism(ipc, clientName);
			} catch (RemoteException e) {
				e.printStackTrace();
			}		
		}
	}

	

}
