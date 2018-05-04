package server;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;

import assignments.util.mainArgs.ServerArgsProcessor;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.nio.SocketChannelBound;
import util.trace.port.rpc.rmi.RMIObjectRegistered;
import util.trace.port.rpc.rmi.RMIRegistryLocated;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.factories.classes.AReadingAcceptCommandFactory;
import inputport.nio.manager.factories.selectors.AcceptCommandFactorySelector;

import assignments.util.mainArgs.ServerPort;
import client.CommandObject;
import examples.mvc.rmi.duplex.ADistributedInheritingRMICounter;
import inputport.nio.manager.listeners.SocketChannelAcceptListener;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import port.ATracingConnectionListener;
import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.SERVER})

public class NIOServer implements ServerPort, SocketChannelAcceptListener{
	NIOServerReceiver receiver;
	NIOServerSender sender;
	ServerSocketChannel serverSocketChannel;
	ArrayBlockingQueue<CommandObject> boundedBuffer;
	HashSet<SocketChannel> openChannels;
	static GIPCRegistry gipcRegistry;
	
	protected static int BUFFER_SIZE = 5000;
	
	public NIOServer() {

	}

	protected void createCommunicationObjects() {
		receiver = new NIOServerReceiver(boundedBuffer, openChannels);
		sender = new NIOServerSender(boundedBuffer, openChannels);
	}
	
	protected void setFactories() {
		AcceptCommandFactorySelector.setFactory(new AReadingAcceptCommandFactory());
		
	}

	protected void makeServerConnectable(int aServerPort) {
		NIOManagerFactory.getSingleton().enableListenableAccepts(serverSocketChannel, this);
	}
	
	protected void initializeBoundedBuffer() {
		boundedBuffer = new ArrayBlockingQueue<CommandObject>(BUFFER_SIZE);
	}
	
	protected void initializeChannelManager() {
		openChannels= new HashSet<SocketChannel>();
	}
	
	public void initialize(int aServerPort) {
		initializeBoundedBuffer();
		initializeChannelManager();
		
		
		setFactories();		
		serverSocketChannel = createSocketChannel(aServerPort);
		makeServerConnectable(aServerPort);
		createCommunicationObjects();

		
		new Thread(sender).start();
		
	}

	protected ServerSocketChannel createSocketChannel(int aServerPort) {
		try {
			ServerSocketChannel retVal = ServerSocketChannel.open();
			InetSocketAddress isa = new InetSocketAddress(aServerPort);
			retVal.socket().bind(isa);
			SocketChannelBound.newCase(this, retVal, isa);
			return retVal;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	protected void addListeners(SocketChannel aSocketChannel) {
		NIOManagerFactory.getSingleton().addReadListener(aSocketChannel, receiver);
	}
//	@Override
	public void socketChannelAccepted(ServerSocketChannel aServerSocketChannel, SocketChannel aSocketChannel) {
		addListeners(aSocketChannel);
		if(!openChannels.contains(aSocketChannel)) {
			openChannels.add(aSocketChannel);
		}
	}
	
	

	public static void main(String[] args) {
		FactoryTraceUtility.setTracing();
		NIOTraceUtility.setTracing();
		BeanTraceUtility.setTracing();// not really needed, but does not hurt
		NIOServer aServer = new NIOServer();
		aServer.initialize(ServerArgsProcessor.getServerPort(args));
		
		//RMI
		try {
			Registry rmiRegistry = LocateRegistry.getRegistry();
			ServerProxyImpl remoteRMIServer = new ServerProxyImpl();
//			RMIRegistryLocated.newCase(remoteServer, "server", 1, rmiRegistry);
			
			rmiRegistry.rebind("server", remoteRMIServer);
//			RMIObjectRegistered.newCase(aSource, anObjectName, anObject, rmiRegistry);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
		
		//GIPC
		gipcRegistry = GIPCLocateRegistry.createRegistry(ServerArgsProcessor.getGIPCServerPort(args));
		try {
			GIPCServerProxy remoteGIPCServer = new ServerProxyImpl();
			gipcRegistry.rebind("GIPCServer", remoteGIPCServer);	
			gipcRegistry.getInputPort().addConnectionListener(new ATracingConnectionListener(gipcRegistry.getInputPort()));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	

	}
}