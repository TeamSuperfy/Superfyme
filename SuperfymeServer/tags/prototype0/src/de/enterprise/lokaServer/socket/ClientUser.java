package de.enterprise.lokaServer.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import de.enterprise.lokaServer.db.DBQuery;

public class ClientUser {

	private Socket socket;
	private int id;
	private PrintWriter out;
	private DBQuery db;

	public ClientUser(Socket socket, DBQuery db) {
		this.socket = socket;
		openOutputStream();
		this.db = db;
		init();
	}

	private void openOutputStream() {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		out.close();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getId() {
		return id;
	}
	
	public void sendMessageToUser(String cmd, String message){
		System.out.println("sending message "+message+" to user "+id);
		out.println(cmd+"#"+message);
	}
	
	public void sendMessageToUser(String message){
		System.out.println("sending message "+message+" to user "+id);
		out.println(message);
	}
	
	public void sendMessageToUser(String cmd, String message, String extra){
		System.out.println("sending message "+message+" to user "+id);
		out.println(cmd+"#"+message+"#"+extra);
	}

	private void init() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String ln = in.readLine();
			if(ln.equals("null")){
				id = db.insertNewUser();
				out.println(id);
			}
			else{
				id = Integer.parseInt(ln);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
