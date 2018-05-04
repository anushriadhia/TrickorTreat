package client;

import java.rmi.RemoteException;

import assignments.util.inputParameters.AnAbstractSimulationParametersBean;
import stringProcessors.HalloweenCommandProcessor;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;
import util.trace.port.PerformanceExperimentEnded;
import util.trace.port.PerformanceExperimentStarted;

public class ClientParameterListener extends AnAbstractSimulationParametersBean{
	
	HalloweenCommandProcessor commandProcessor;
	NIOclient client;
	

	public ClientParameterListener(HalloweenCommandProcessor commandProcessor, NIOclient client) {
		this.commandProcessor = commandProcessor;
		this.client = client;
	}

	@Override
	public void atomicBroadcast(boolean newValue) {
		commandProcessor.setConnectedToSimulation(!newValue);
		setAtomicBroadcast(newValue);
		if(isBroadcastMetaState()) {
			System.out.println(getConsensusAlgorithm());
			if(getConsensusAlgorithm() == ConsensusAlgorithm.CENTRALIZED_ASYNCHRONOUS) {
				try {
					client.remoteRMIServer.setAtomicBroadcast(newValue, client.clientName);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			else if (getConsensusAlgorithm()==ConsensusAlgorithm.CENTRALIZED_SYNCHRONOUS) {
				try {
					client.remoteRMIServer.proposeAtomic(newValue, client.clientName);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void experimentInput() {		
		long startTime = System.currentTimeMillis();
		PerformanceExperimentStarted.newCase(this, startTime, 100);
		
		for(int i=0; i<100; i++) {
			client.clientSender.sendCommand("move 2 2", isAtomicBroadcast());
		}
		
		long endTime = System.currentTimeMillis();
		PerformanceExperimentEnded.newCase(this, startTime, endTime, endTime - startTime, 100);
		
		System.out.println("Time took: "+ (endTime-startTime));
		
	}

	@Override
	public void localProcessingOnly(boolean newValue) {
		setLocalProcessingOnly(newValue);
		System.out.println("Local processing now changed to " + getLocalProcessing());
	}

	@Override
	public void ipcMechanism(IPCMechanism newValue) {
		setIPCMechanism(newValue);
		if(isBroadcastMetaState()) {
			if(getConsensusAlgorithm()==ConsensusAlgorithm.CENTRALIZED_ASYNCHRONOUS) {
				try {
					client.remoteRMIServer.setIPCMechanism(newValue, client.clientName);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			else if(getConsensusAlgorithm()==ConsensusAlgorithm.CENTRALIZED_SYNCHRONOUS) {
				try {
					client.remoteRMIServer.proposeIPC(newValue, client.clientName);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	


	@Override
	public void waitForBroadcastConsensus(boolean newValue) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void waitForIPCMechanismConsensus(boolean newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void consensusAlgorithm(ConsensusAlgorithm newValue) {
		// TODO Auto-generated method stub
		setConsensusAlgorithm(newValue);
		
	}

	@Override
	public void quit(int aCode) {
		System.exit(aCode);
	}

	@Override
	public void simulationCommand(String aCommand) {
		commandProcessor.setInputString(aCommand);
	}
	
	public boolean getLocalProcessing() {
		return isLocalProcessingOnly();
	}


}
