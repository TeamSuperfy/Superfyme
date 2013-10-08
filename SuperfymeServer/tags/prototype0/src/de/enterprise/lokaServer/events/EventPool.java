package de.enterprise.lokaServer.events;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import de.enterprise.lokaServer.LokaServer;
import de.enterprise.lokaServer.tools.PackageClassLoader;

public class EventPool {

	private final String EVENTS_PACKAGE = "de.enterprise.lokaServer.events.execution";

	private Queue<String[]> eventPool;
	private HashMap<String, ServerEvent> eventMap;
	private EventThread eventThread;
	private LokaServer lokaServer;

	private static final class Lock {
	}

	private final Object lock = new Lock();

	public EventPool(LokaServer lokaServer) {
		this.lokaServer = lokaServer;
		buildEventMap();
		eventPool = new LinkedList<String[]>();
		eventThread = new EventThread();
		eventThread.start();
	}

	private void buildEventMap() {
		eventMap = new HashMap<String, ServerEvent>();
		Class<?>[] classes = null;
		try {
			classes = PackageClassLoader.getClasses(EVENTS_PACKAGE);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (classes != null) {
			for (Class<?> _class : classes) {
				try {
					ServerEvent event = (ServerEvent) _class.newInstance();
					event.setLokaServer(lokaServer);
					eventMap.put(event.CMD, event);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void addEvent(String[] event) {
		eventPool.add(event);
		if (eventThread.state == eventThread.SLEEPING) {
			synchronized (lock) {
				eventThread.state = eventThread.WORKING;
				lock.notify();
			}
		}
	}

	private class EventThread extends Thread {

		int WORKING = 0, SLEEPING = 1;
		int state = WORKING;

		public void run() {
			synchronized (lock) {
				while (true) {
					handleEvents();
					try {
						state = SLEEPING;

						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}

		private void handleEvents() {
			while (!eventPool.isEmpty()) {
				String[] event = eventPool.poll();
				ServerEvent evt = eventMap.get(event[0]);
				if(event.length > 2){
					evt.execute(event[1], event[2]);
				}
				else{
					evt.execute(event[1]);
				}
			}
		}
	}

}
