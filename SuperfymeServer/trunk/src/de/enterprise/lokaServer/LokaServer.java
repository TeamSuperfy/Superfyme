package de.enterprise.lokaServer;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import de.enterprise.lokaServer.db.DBQuery;
import de.enterprise.lokaServer.events.EventPool;
import de.enterprise.lokaServer.servlet.LokaServlet;

@WebListener
public class LokaServer implements ServletContextListener{
	
	public static final Logger log = Logger.getLogger(LokaServlet.class.getName());
	
	private DBQuery dbQuery;
	private EventPool eventPool;
	

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		dbQuery.close();
		eventPool.close();
		System.gc();
		System.out.println("context destroyed");
	}

	@Override
	public void contextInitialized(ServletContextEvent evt) {
		log.log(Level.INFO, "context initialized");
		dbQuery = new DBQuery();
		eventPool = new EventPool(this);
		evt.getServletContext().setAttribute("eventPool", eventPool);
	}
	
	public DBQuery getDBQuery(){
		return dbQuery;
	}
	
}
