package org.mars_sim.networking;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class SocketClientHandler implements Runnable {

  private Socket client;

  public SocketClientHandler(Socket client) {
	this.client = client;
  }

  @Override
  public void run() {
     try {
		System.out.println("Thread started with name : " + Thread.currentThread().getName());
		//readResponse();
/*
		Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Mars Simulation Project");
		alert.setHeaderText("Multiplayer Host");
		alert.setContentText("A client has just established the connection with you");
		alert.showAndWait();
*/
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        out.println(new Date().toString());

     } catch (IOException e) { e.printStackTrace();}

   }

   private void readResponse() throws IOException, InterruptedException {
		String userInput;
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
		while ((userInput = stdIn.readLine()) != null) {
			if(userInput.equals("TIME?")){
				System.out.println("REQUEST TO SEND TIME RECEIVED. SENDING CURRENT TIME");
				sendTime();
				break;
			}
			System.out.println(userInput);
		}
	}

    private void sendTime() throws IOException, InterruptedException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		writer.write(new Date().toString());
		writer.flush();
		writer.close();
    }

}

