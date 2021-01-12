package org.mars.sim.console.chat.service;

import java.io.IOException;

public class RemoteApp {
	
	public static void main(String... args) throws InterruptedException {
    
		RemoteChatService service = new RemoteChatService(18080, new Credentials());
		try {
			service.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Thread.sleep(100000);
    }

}
