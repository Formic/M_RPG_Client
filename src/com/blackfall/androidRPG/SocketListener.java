package com.blackfall.androidRPG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketListener implements Runnable {
	private Socket socket;
	private Client player;

	public SocketListener(Socket socket, Client player) {
		this.socket = socket;
		this.player = player;
	}

	@Override
	public void run() {
		BufferedReader listener = null;
		String line;
		try{
			listener = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			System.out.println("IO failed: " + e);
		}

		while(true){
			try{
				line = listener.readLine();
				if (line != null && line.length() > 0)
					player.handleRecieveData(line);				
			}catch (IOException e) {
				System.out.println("Read failed: " + e);
			}
		}

	}

}
