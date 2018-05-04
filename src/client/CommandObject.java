package client;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class CommandObject {
	SocketChannel socketChannel;
	ByteBuffer message;
	int length;
	
	public CommandObject(SocketChannel socketChannel, ByteBuffer message, int length) {
		this.socketChannel = socketChannel;
		this.message = message;
		this.length = length;
		
	}
	
	public ByteBuffer getMessage() {
		return message;
	}
	
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

}
