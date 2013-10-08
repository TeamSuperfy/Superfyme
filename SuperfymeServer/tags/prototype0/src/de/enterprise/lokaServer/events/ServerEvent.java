package de.enterprise.lokaServer.events;

import de.enterprise.lokaServer.LokaServer;

public abstract class ServerEvent{
	
	public String CMD;
	protected LokaServer lokaServer;
	
	protected abstract void execute(String json);
	protected abstract void execute(String json, String extra);
	
	public void setLokaServer(LokaServer lokaServer){
		this.lokaServer = lokaServer;
	}
	
}
