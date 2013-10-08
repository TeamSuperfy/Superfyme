package de.enterprise.lokaServer.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import de.enterprise.lokaServer.db.DBQuery;

public class LokaServerSocket extends Thread {

	public static int SERVER_PORT = 8083;

	private ServerSocket serverSocket;
	private boolean running;
	private HashMap<Integer, ClientUser> clients;
	private DBQuery db;

	public LokaServerSocket(DBQuery db) {
		this.db = db;
		clients = new HashMap<Integer, ClientUser>();
		running = true;
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void run() {
		while (running) {
			try {
				Socket socket = serverSocket.accept();
				ClientUser client = new ClientUser(socket, db);
				clients.put(client.getId(), client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendToUser(int userID, String cmd, String message) {
		ClientUser client = clients.get(userID);
		if (client != null) {
			client.sendMessageToUser(cmd, message);
		}
	}
	
	public void sendToUser(int userID, String message) {
		ClientUser client = clients.get(userID);
		if (client != null) {
			client.sendMessageToUser(message);
		}
	}
	
	public void sendToUser(int userID, String cmd, String message, String extra) {
		ClientUser client = clients.get(userID);
		if (client != null) {
			client.sendMessageToUser(cmd, message, extra);
		}
	}


	public void close() {
		for (ClientUser client : clients.values()) {
			client.close();
		}
	}

}
