package server;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;

import assignments.util.MiscAssignmentUtils;
import client.CommandObject;
import inputport.nio.manager.listeners.SocketChannelReadListener;

public class NIOServerReceiver implements SocketChannelReadListener{
	
	ArrayBlockingQueue<CommandObject> boundedBuffer;
	
	public NIOServerReceiver(ArrayBlockingQueue<CommandObject> boundedBuffer, HashSet<SocketChannel> openChannels) {
		this.boundedBuffer = boundedBuffer;
	}

	public void socketChannelRead(SocketChannel aSocketChannel, ByteBuffer aMessage, int aLength) {
		CommandObject incomingMessage = new CommandObject(aSocketChannel, MiscAssignmentUtils.deepDuplicate(aMessage), aLength);
		addToBuffer(incomingMessage);
	}
	
	protected void addToBuffer(CommandObject message) {
		try {
			boundedBuffer.add(message);
		} catch(Exception e) {
			System.out.println("Not enough space");
		}
	}
	
}