package de.enterprise.lokaServer.events.execution;

import de.enterprise.lokaServer.events.CommandEvent;
import de.enterprise.lokaServer.events.ServerEvent;
import de.enterprise.lokaServer.pojos.NotificationPojo;
import de.enterprise.lokaServer.tools.JSONConverter;

public class DeleteNotificationsEvent extends ServerEvent {

	public DeleteNotificationsEvent(){
		CMD = "DN";
	}
	
	@Override
	protected void execute(CommandEvent evt)  throws Exception {
		String json = evt.getCmd()[1];
		NotificationPojo[] notifsArr = (NotificationPojo[]) JSONConverter.fromJSONArray(json, NotificationPojo[].class.getName());
		lokaServer.getDBQuery().deleteNotifications(notifsArr);
		sendResponse(evt.getResponse(), null);
	}

}
