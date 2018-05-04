package client;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ArrayBlockingQueue;

import assignments.util.inputParameters.ASimulationParametersController;
import assignments.util.mainArgs.ClientArgsProcessor;
import util.interactiveMethodInvocation.SimulationParametersController;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.factories.classes.AReadingWritingConnectCommandFactory;
import inputport.nio.manager.factories.selectors.ConnectCommandFactorySelector;
import inputport.nio.manager.listeners.SocketChannelConnectListener;
import inputport.rpc.ACachingAbstractRPCProxyInvocationHandler;
import inputport.rpc.GIPCLocateRegistry;
import inputport.rpc.GIPCRegistry;
import main.BeauAndersonFinalProject;
import port.ATracingConnectionListener;
import server.RMIServerProxy;
import server.ServerProxyImpl;
import server.GIPCServerProxy;
import stringProcessors.HalloweenCommandProcessor;
import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.CLIENT})

public class NIOclient implements SocketChannelConnectListener{
	
	String clientName;	
	
	ClientSender clientSender;
	ClientReceiver clientReceiver;
	ClientProcessor processor;
	
//	DistributedCommandProcessor remoteServer;
	GIPCServerProxy remoteGIPCServer;
	RMIServerProxy remoteRMIServer;
	RMIClientProxy remoteClient;
	GIPCClientProxy remoteGIPCClient;
	
	SocketChannel socketChannel;
	HalloweenCommandProcessor commandProcessor;
	SimulationParametersController parameterController;
	
	ClientParameterListener paramListener;
	
	ArrayBlockingQueue<CommandObject> boundedBuffer;
	
	private static String[] mainArgs;
		
	private static int BUFFER_SIZE = 5000;
	
	public NIOclient(String aClientName) {
		clientName = aClientName;	}

	
	protected void setFactories() {		
		ConnectCommandFactorySelector.setFactory(new AReadingWritingConnectCommandFactory());
	}
	
	
	public void initialize(String aServerHost, int aServerPort) throws RemoteException{	
		
		commandProcessor = createSimulation(Simulation.SIMULATION1_PREFIX);	
				
		makeBuffer();

		setFactories();
		socketChannel = createSocketChannel();
		createCommunicationObjects();
		addListeners();
		connectToServer(aServerHost, aServerPort);
		
		RMIstartup();
		GIPCstartup();
		
		new Thread(processor).start();
		
		parameterController.processCommands();		
	}


	public void connectToServer(String aServerHost, int aServerPort) {
		connectToSocketChannel(aServerHost, aServerPort);
	}

	protected void connectToSocketChannel(String aServerHost, int aServerPort) {
		try {
			InetAddress aServerAddress = InetAddress.getByName(aServerHost);
			NIOManagerFactory.getSingleton().connect(socketChannel, aServerAddress, aServerPort, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected SocketChannel createSocketChannel() {
		try {
			SocketChannel retVal = SocketChannel.open();
			return retVal;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void connected(SocketChannel aSocketChannel) {
		System.out.println("Ready to send messages to server");
	}
	protected void createCommunicationObjects() {
		
		paramListener = new ClientParameterListener(commandProcessor, this);
		
		clientSender = new ClientSender(socketChannel, commandProcessor, paramListener, this);
		clientReceiver = new ClientReceiver(boundedBuffer, paramListener);
		
		processor = new ClientProcessor(boundedBuffer, commandProcessor);
		parameterController = new ASimulationParametersController();
		
	}
	
	protected void makeBuffer() {
		boundedBuffer = new ArrayBlockingQueue<CommandObject>(BUFFER_SIZE);
	}
	
	protected void addListeners() {
		NIOManagerFactory.getSingleton().addReadListener(socketChannel, clientReceiver);
		commandProcessor.addPropertyChangeListener(clientSender);
		parameterController.addSimulationParameterListener(paramListener);

	}

	
	@Override
	public void notConnected(SocketChannel aSocketChannel, Exception e) {
		System.err.println("Could not connect:" +aSocketChannel);
		if (e != null)
		   e.printStackTrace();
	}
	
	public static HalloweenCommandProcessor createSimulation(String aPrefix) {
		return 	BeauAndersonFinalProject.createSimulation(
					aPrefix,
					Simulation.SIMULATION1_X_OFFSET, 
					Simulation.SIMULATION_Y_OFFSET, 
					Simulation.SIMULATION_WIDTH, 
					Simulation.SIMULATION_HEIGHT, 
					Simulation.SIMULATION1_X_OFFSET, 
					Simulation.SIMULATION_Y_OFFSET);
	}
	
	//RMI
	
	public void RMIstartup() throws RemoteException {
		
		try {
			Registry rmiRegistry = LocateRegistry.getRegistry();
			remoteClient = new ClientProxyImpl(commandProcessor, clientName, paramListener);
			
			remoteRMIServer = (RMIServerProxy) rmiRegistry.lookup("server");
			
//			RMIObjectLookedUp.newCase(aSource, anObject, remoteC, rmiRegistry);
			
			UnicastRemoteObject.exportObject(remoteClient, 0);
			remoteRMIServer.registerToRMIServer(remoteClient, clientName);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void GIPCstartup() {
		ACachingAbstractRPCProxyInvocationHandler.setInvokeObjectMethodsRemotely(false);
		GIPCRegistry gipcRegistry = GIPCLocateRegistry.getRegistry(ClientArgsProcessor.getServerHost(mainArgs), 
				ClientArgsProcessor.getGIPCPort(mainArgs),
				"GIPCServer");
		if (gipcRegistry == null) {
			System.exit(-1);
		}
		
		try {
			remoteGIPCClient = new ClientProxyImpl(commandProcessor, clientName, paramListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		remoteGIPCServer = (GIPCServerProxy) gipcRegistry.lookup(ServerProxyImpl.class, "GIPCServer");	
		gipcRegistry.getInputPort().addConnectionListener(new ATracingConnectionListener(gipcRegistry.getInputPort()));
		
		remoteGIPCServer.registerToGIPCServer(remoteGIPCClient, clientName);
	}
	
	public static void launchClient(String aServerHost, int aServerPort,
			String aClientName, String[] args) throws RemoteException {
		
		mainArgs = args;
		/*
		 * Put these two in your clients also
		 */
		FactoryTraceUtility.setTracing();
		BeanTraceUtility.setTracing();
		NIOTraceUtility.setTracing();
		NIOclient aClient = new NIOclient(aClientName);
		aClient.initialize(aServerHost, aServerPort);		
	}
	

	public static void main(String[] args) throws RemoteException {	
		launchClient(ClientArgsProcessor.getServerHost(args),
				ClientArgsProcessor.getServerPort(args),
				ClientArgsProcessor.getClientName(args), args);
	}


}

