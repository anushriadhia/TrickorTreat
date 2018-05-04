package client;

import java.util.concurrent.ArrayBlockingQueue;
import stringProcessors.HalloweenCommandProcessor;

public class ClientProcessor implements Runnable {
	
	ArrayBlockingQueue<CommandObject> boundedBuffer;
	HalloweenCommandProcessor commandProcessor;
	public static final String READ_THREAD_NAME = "Read Thread";

		
	public ClientProcessor(ArrayBlockingQueue<CommandObject> boundedBuffer, HalloweenCommandProcessor commandProcessor) {
		this.boundedBuffer = boundedBuffer;
		this.commandProcessor = commandProcessor;

	}

	@Override
	public void run() {
		Thread.currentThread().setName(READ_THREAD_NAME);
		while(true) {
			try {
				CommandObject nextCommand = boundedBuffer.take();
				String inputStr = new String(nextCommand.message.array(), nextCommand.message.position(),nextCommand.length);
								
				commandProcessor.processCommand(removeBroadcastConcat(inputStr));	
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String removeBroadcastConcat(String cmd) {
		if(cmd.charAt(0)=='0' || cmd.charAt(0)== '1') {
			return cmd.substring(1);
		}
		return cmd;
	}

}
