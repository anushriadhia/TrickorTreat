package server;


import client.ClientProxyImpl;
import client.GIPCClientProxy;

public interface GIPCServerProxy {
	void gipcSimulationCommand (String cmd, boolean isAtomic, String clientName);
	void registerToGIPCServer (GIPCClientProxy client, String clientName);

}
