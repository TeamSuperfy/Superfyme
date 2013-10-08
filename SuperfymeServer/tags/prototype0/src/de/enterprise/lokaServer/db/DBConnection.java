package de.enterprise.lokaServer.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBConnection {

	private Connection connect = null;
	private Statement statement = null;
	
	public DBConnection(){
		try {
			openConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openConnection() throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/"+SQLInfo.DATABASE+"?"
							+ "user=root&password=admin");
			statement = connect.createStatement();
		} catch (Exception e) {
			throw e;
		}

	}
	
	public Statement getStatement(){
		return statement;
	}
	
	public void close() {
		try {
			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {

		}
	}
	
	public static void main(String[] args) {
		new DBConnection();
	}
	
	

}
