package launchers;

import java.rmi.RemoteException;

import assignments.util.mainArgs.ClientArgsProcessor;
import client.NIOclient;
import util.trace.port.nio.NIOTraceUtility;

public class Client3 {

	public static void main(String[] args) throws RemoteException {
		// TODO Auto-generated method stub
		NIOTraceUtility.setTracing();
		NIOclient.launchClient(ClientArgsProcessor.getServerHost(args), 
				ClientArgsProcessor.getServerPort(args), 
				"Client3",
				ClientArgsProcessor.getGIPCPort(args));	

	}

}
