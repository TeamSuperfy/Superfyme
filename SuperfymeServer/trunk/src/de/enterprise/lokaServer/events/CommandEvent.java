package de.enterprise.lokaServer.events;

import javax.servlet.AsyncContext;

public class CommandEvent {

	private String[] cmd;
	private AsyncContext ctx;
	
	public CommandEvent(String[] cmd, AsyncContext ctx){
		this.cmd = cmd;
		this.ctx = ctx;
	}
	
	public String[] getCmd() {
		return cmd;
	}
	public void setCmd(String[] cmd) {
		this.cmd = cmd;
	}

	public AsyncContext getResponse() {
		return ctx;
	}

	public void setResponse(AsyncContext response) {
		this.ctx = response;
	}
}
