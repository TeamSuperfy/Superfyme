package de.enterprise.lokaServer.events;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import de.enterprise.lokaServer.LokaServer;
import de.enterprise.lokaServer.tools.PackageClassLoader;

public class EventPool {

	private final String EVENTS_PACKAGE = "de.enterprise.lokaServer.events.execution";

	private Queue<CommandEvent> eventPool;
	private HashMap<String, ServerEvent> eventMap;
	private EventThread eventThread;
	private LokaServer lokaServer;

	private static final class Lock {
	}

	private final Object lock = new Lock();

	public EventPool(LokaServer lokaServer) {
		this.lokaServer = lokaServer;
		buildEventMap();
		eventPool = new LinkedList<CommandEvent>();
		eventThread = new EventThread("Event Thread");
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

	public void addEvent(CommandEvent event) {
		eventPool.add(event);
		if (eventThread.state == eventThread.SLEEPING) {
			synchronized (lock) {
				eventThread.state = eventThread.WORKING;
				lock.notify();
			}
		}
	}
	
	public void close(){
		eventThread.finish = true;
		eventThread.interrupt();
		eventThread = null;
	}

	private class EventThread extends Thread {
		int WORKING = 0, SLEEPING = 1;
		int state = WORKING;
		public boolean finish = false;
		
		public EventThread(String name){
			super(name);
		}

		public void run() {
			synchronized (lock) {
				while (true) {
					handleEvents();
					try {
						state = SLEEPING;

						lock.wait();
					} catch (InterruptedException e) {
					}
					
					if(finish){
						System.out.println("event thread stopped");
						return;
					}
				}
			}

		}

		private void handleEvents() {
			while (!eventPool.isEmpty()) {
				final CommandEvent cmdEvt = eventPool.poll();
				String[] event = cmdEvt.getCmd();
				ServerEvent evt = eventMap.get(event[0]);
				try {
					final ServerEvent newEvent = evt.getClass().newInstance();
					newEvent.setLokaServer(evt.getLokaServer());
					new Runnable(){
						@Override
						public void run() {
							try{
								newEvent.execute(cmdEvt);
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}.run();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
