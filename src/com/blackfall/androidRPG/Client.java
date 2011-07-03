package com.blackfall.androidRPG;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
	private GraphicsEngine graphicsEngine;
	private Socket socket;
	private PrintWriter sender;
	private int clientId;
	private static Client instance;
	
	enum Commands {
		Login (0),
		Move (1),
		AddPlayer (2),
		RemovePlayer (3);
		
		private int code;
		Commands(int code) {
			this.code = code;
		}
		
		static public Commands intToCommand(int i) {
			switch (i) {
				case 0: return Login;
				case 1: return Move;
				case 2: return AddPlayer;
			}
			return null;
		}
	}
	
	private Client(GraphicsEngine graphicsEngine) {
		this.graphicsEngine = graphicsEngine;
		this.clientId = -1;
		connectToServer();
	}
	
	public static Client getInstance(GraphicsEngine graphicsEngine) {
		if (instance == null)
			instance = new Client(graphicsEngine);
		else 
			instance.graphicsEngine.redrawAllCharacters();
		return instance;
	}
	
	public void connectToServer() {
		try {
			socket = new Socket("24.10.219.59", 2001);
			socket.setTcpNoDelay(true);
		} catch(Exception e) {
			System.out.println(e);
		}
		try {
			sender = new PrintWriter(socket.getOutputStream(), true);
			SocketListener listener = new SocketListener(socket, this);
			Thread listenerThread = new Thread(listener);
			listenerThread.start();
		} catch(Exception e) {
			System.out.println("Failed to listen: " + e);
		}
	}
	
	public void handleRecieveData(String data) {
		ArrayList<String> response = processMessage(data);
		int command = Integer.parseInt(response.get(0));
		
		switch (Commands.intToCommand(command)) {
			case Login: this.clientId = Integer.parseInt(response.get(1)); //fall through to add player
			
			case AddPlayer: graphicsEngine.AddCharacter(Integer.parseInt(response.get(1)), 
														Float.parseFloat(response.get(2)), 
														Float.parseFloat(response.get(3)), 
														Integer.parseInt(response.get(4))); break;
														
			case Move: graphicsEngine.MoveCharacter(Integer.parseInt(response.get(1)), 
												    Float.parseFloat(response.get(2)), 
												    Float.parseFloat(response.get(3))); break;
			case RemovePlayer: graphicsEngine.RemoveCharacter(Integer.parseInt(response.get(1))); break;
		}
	}
	
	public void sendMove(float xVelocity, float yVelocity) {
		sender.println(formatMessage(Commands.Move.code, xVelocity, yVelocity));
	}
	
	protected void finalize() throws Throwable{
		try{
			socket.close();
		} catch (IOException e) {
			System.out.println("Could not close socket");
		} finally {
	        super.finalize();
	    }
	}
	
	private String formatMessage(Object ...args) {
		String message = "";
		for (Object o : args)
			message += "<" + o + ">";
		return message;
	}
	
	private ArrayList<String> processMessage(String message) {
		Pattern pattern = Pattern.compile("<([^>]*)>");
		Matcher matcher = pattern.matcher(message);
		ArrayList<String> matches = new ArrayList<String>();
		int index = 0;
		while(matcher.find(index)) {
			matches.add(matcher.group(1));
			index = matcher.end();
		}
		return matches;
	}

	public int getClientId() {
		return clientId;
	}
}
