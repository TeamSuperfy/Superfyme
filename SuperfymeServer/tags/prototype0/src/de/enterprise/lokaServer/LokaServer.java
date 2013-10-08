package de.enterprise.lokaServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import de.enterprise.lokaServer.db.DBQuery;
import de.enterprise.lokaServer.events.EventPool;
import de.enterprise.lokaServer.socket.LokaServerSocket;

@WebListener
public class LokaServer implements ServletContextListener{
	
	private DBQuery dbQuery;
	private EventPool eventPool;
	private LokaServerSocket serverSocket;
	

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		dbQuery.close();
		serverSocket.close();
		serverSocket = null;
		eventPool = null;
	}

	@Override
	public void contextInitialized(ServletContextEvent evt) {
		dbQuery = new DBQuery();
		serverSocket = new LokaServerSocket(dbQuery);
		serverSocket.start();
		eventPool = new EventPool(this);
		evt.getServletContext().setAttribute("eventPool", eventPool);
	}
	
	public DBQuery getDBQuery(){
		return dbQuery;
	}
	
	public LokaServerSocket getServerSocket(){
		return serverSocket;
	}
	
}
