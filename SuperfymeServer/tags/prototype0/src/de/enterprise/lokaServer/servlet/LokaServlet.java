package de.enterprise.lokaServer.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sun.misc.BASE64Decoder;
import de.enterprise.lokaServer.events.EventPool;

@WebServlet("/lokaServlet")
public class LokaServlet extends HttpServlet {
	
	EventPool eventPool;
	
	public void init(){
		eventPool = (EventPool) this.getServletContext().getAttribute("eventPool");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if(request.getParameter("cmd") != null){
			System.out.println("received new event");
			if(request.getParameter("img") != null){
				eventPool.addEvent(new String[]{request.getParameter("cmd"), request.getParameter("json"), request.getParameter("img")});
			}
			else{
				eventPool.addEvent(new String[]{request.getParameter("cmd"), request.getParameter("json")});
			}
		}
	}

}
