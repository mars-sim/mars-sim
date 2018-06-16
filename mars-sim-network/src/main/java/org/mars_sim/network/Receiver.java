package org.mars_sim.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import javafx.scene.control.TextArea;

// Modified from http://mrbool.com/how-to-create-chat-application-in-java/26778
public class Receiver implements Runnable {
	Thread activity = new Thread(this);
	MulticastSocket so;
	TextArea txt;

	public Receiver(MulticastSocket sock, TextArea txtAr) {
		so = sock;
		txt = txtAr;
		activity.start();
	}

	public void run() {
        byte[] data = new byte[1024];
        while(true)
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length);
            so.receive(packet);
            String mess = new String(data, 0, packet.getLength());
            txt.appendText(mess + "\n");
        }   catch(IOException e) {
        	break;
        }
    }

}

