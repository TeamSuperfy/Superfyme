package de.enterprise.lokaServer.servlet;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.EventPool;

@javax.servlet.annotation.WebServlet(
	    // servlet name
	    name = "LokaServlet",
	    // servlet url pattern
	    value = {"/LokaServlet"},
	    // async support needed
	    asyncSupported = true
	)
@MultipartConfig
public class LokaServlet extends HttpServlet {

	private static final long serialVersionUID = -5515894318774912780L;

	public static final Logger log = Logger.getLogger(LokaServlet.class.getName());
	
	EventPool eventPool;

	@Override
	public void init() {
		eventPool = (EventPool) this.getServletContext().getAttribute(
				"eventPool");
		log.log(Level.INFO, "servlet initialized");
	}
	
	@Override
	public void service(ServletRequest req, final ServletResponse res){
		AsyncContext ctx = req.startAsync();
		try {
			req.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		if(!ServletFileUpload.isMultipartContent((HttpServletRequest) req)){
		ctx.setTimeout(30000);
			if (req.getParameter("cmd") != null) {
					System.out.println(new Date(System.currentTimeMillis())+" received new event: "
							+ req.getParameter("cmd")
							+ req.getParameter("json"));
				eventPool.addEvent(new CommandEvent(new String[] { req.getParameter("cmd"),
						req.getParameter("json") }, ctx));
			}
		}else{
			try {
				if (((HttpServletRequest)req).getHeader("cmd") != null){
					System.out.println(new Date(System.currentTimeMillis())+" received new event: "
							+ ((HttpServletRequest)req).getHeader("cmd")
							+ " json");
				eventPool.addEvent(new CommandEvent(new String[] { ((HttpServletRequest)req).getHeader("cmd"),
						null }, ctx));
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

}
