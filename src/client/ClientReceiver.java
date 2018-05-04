package client;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

import assignments.util.MiscAssignmentUtils;
import inputport.nio.manager.listeners.SocketChannelReadListener;

public class ClientReceiver implements SocketChannelReadListener{
	
	ClientParameterListener paramListener;
	ArrayBlockingQueue<CommandObject> boundedBuffer;
	
	public ClientReceiver(ArrayBlockingQueue<CommandObject> boundedBuffer, ClientParameterListener paramListener) {
		this.boundedBuffer = boundedBuffer;
		this.paramListener = paramListener;
	}



	@Override
	public void socketChannelRead(SocketChannel aSocketChannel, ByteBuffer aMessage, int aLength) {				
		CommandObject incomingMessage = new CommandObject(aSocketChannel, MiscAssignmentUtils.deepDuplicate(aMessage), aLength);
		if(!paramListener.getLocalProcessing()) {
			addToBuffer(incomingMessage);	
		} 
	}
	
	
	
	protected void addToBuffer(CommandObject message) {
		try {
			boundedBuffer.add(message);
		} catch(Exception e) {
			System.out.println("Not enough space");
		}
	}

}
