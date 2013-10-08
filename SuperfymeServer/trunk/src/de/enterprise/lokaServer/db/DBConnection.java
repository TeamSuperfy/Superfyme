package de.enterprise.lokaServer.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DBConnection {
	
	private Context ctx;
	private DataSource ds;

	public DBConnection() {
		try {
			ctx = new InitialContext();
			Context envCtx = (Context) ctx.lookup("java:comp/env");

			ds = (DataSource) envCtx
					.lookup("jdbc/Superfy");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection(){
		if (ds != null) {
			Connection conn;
			try {
				conn = ds.getConnection();
				
				if (conn != null) {
					return conn;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

}
