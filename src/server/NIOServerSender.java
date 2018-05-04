package server;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;

import client.CommandObject;
import inputport.nio.manager.NIOManagerFactory;

public class NIOServerSender implements Runnable{
	
	ArrayBlockingQueue<CommandObject> boundedBuffer;
	HashSet<SocketChannel> openChannels;
	public static final String READ_THREAD_NAME = "Read Thread";
	
	
	public NIOServerSender(ArrayBlockingQueue<CommandObject> boundedBuffer, HashSet<SocketChannel> openChannels) {
		this.boundedBuffer = boundedBuffer;
		this.openChannels = openChannels;
	}

	@Override
	public void run() {
		Thread.currentThread().setName(READ_THREAD_NAME);
		while(true) {
			try {
				CommandObject nextCommand = boundedBuffer.take();
				boolean isBroadcasted = (nextCommand.getMessage().get(0) == 49)? false : true;
				
				for(SocketChannel socket: openChannels) {
					if(isBroadcasted) {
						NIOManagerFactory.getSingleton().write(socket, nextCommand.getMessage());
					} else if(!nextCommand.getSocketChannel().equals(socket)) {
						NIOManagerFactory.getSingleton().write(socket, nextCommand.getMessage());
					}
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
