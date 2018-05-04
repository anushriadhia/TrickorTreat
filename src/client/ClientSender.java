package client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;

import inputport.nio.manager.NIOManagerFactory;
import stringProcessors.HalloweenCommandProcessor;
import util.misc.ThreadSupport;


public class ClientSender implements PropertyChangeListener {
	
	SocketChannel socketChannel;
	HalloweenCommandProcessor commandProcessor;
	ClientParameterListener paramListener;
	NIOclient client;
	
	public ClientSender(SocketChannel socketChannel, HalloweenCommandProcessor commandProcessor, 
			ClientParameterListener paramListener, NIOclient client) {
		this.socketChannel = socketChannel;
		this.commandProcessor = commandProcessor;
		this.paramListener = paramListener;
		this.client = client;
	}

	@Override
	public void propertyChange(PropertyChangeEvent anEvent){
		
		
		if (!anEvent.getPropertyName().equals("InputString")) return;

		String cmd = anEvent.getNewValue().toString();
		boolean isAtomic = paramListener.isAtomicBroadcast();
		
		if(!paramListener.getLocalProcessing()) {
			switch(paramListener.getIPCMechanism()) {
			case GIPC:
				client.remoteGIPCServer.gipcSimulationCommand(cmd, isAtomic, client.clientName);
				break;
			case NIO:
				writeToSocket(cmd, socketChannel, isAtomic);
				break;
			case RMI:
				try {
					client.remoteRMIServer.rmiSimulationCommand(cmd, isAtomic, client.clientName);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			default:
				client.remoteGIPCServer.gipcSimulationCommand(cmd, isAtomic, client.clientName);
				break;
			}	
		}
		paramListener.setDelay(0);
	}
	
	public void sendCommand(String cmd, boolean isAtomic) {
		
		if(paramListener.getLocalProcessing() || !paramListener.isAtomicBroadcast()) {
			client.commandProcessor.setInputString(cmd);
		}
		
		if(!paramListener.getLocalProcessing()) {
			switch(paramListener.getIPCMechanism()) {
			case GIPC:
				client.remoteGIPCServer.gipcSimulationCommand(cmd, isAtomic, client.clientName);
				break;
			case NIO:
				writeToSocket(cmd, socketChannel, isAtomic);
				break;
			case RMI:
				try {
					client.remoteRMIServer.rmiSimulationCommand(cmd, isAtomic, client.clientName);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			default:
				client.remoteGIPCServer.gipcSimulationCommand(cmd, isAtomic, client.clientName);
				break;
			} 
		}
					
		
		
	}
	
	private void writeToSocket(String cmd, SocketChannel socketChannel, boolean isAtomic) {
		int atomicBroadcast = (isAtomic) ? 0:1;
						
		ByteBuffer aMeaningByteBuffer = ByteBuffer.wrap((atomicBroadcast +cmd).getBytes());
		NIOManagerFactory.getSingleton().write(socketChannel, aMeaningByteBuffer);
	}

}
