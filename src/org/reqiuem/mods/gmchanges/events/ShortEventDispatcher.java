// 
// Decompiled by Procyon v0.5.30
// 

package org.reqiuem.mods.gmchanges.events;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class ShortEventDispatcher
{
    private static Logger logger;
    private static ArrayList<EventOnce> events;
    private static Timer timer;
    private static boolean running;
    
    static {
        ShortEventDispatcher.logger = Logger.getLogger(ShortEventDispatcher.class.getName());
        ShortEventDispatcher.events = new ArrayList<EventOnce>();
        ShortEventDispatcher.timer = null;
        ShortEventDispatcher.running = false;
    }
    
    private static void startPolling() {
        if (ShortEventDispatcher.timer != null) {
            ShortEventDispatcher.logger.info("startPolling(): Poller is already running");
            return;
        }
        (ShortEventDispatcher.timer = new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                poll();
            }
        }, 50L, 1L);
        ShortEventDispatcher.logger.info("stopPolling(): started");
    }
    
    private static void stopPolling() {
        if (ShortEventDispatcher.timer != null) {
            ShortEventDispatcher.logger.info("stopPolling(): stopped");
            ShortEventDispatcher.timer.cancel();
            ShortEventDispatcher.timer = null;
            return;
        }
        ShortEventDispatcher.logger.severe("stopPolling(): Poller was not running. Why call this, eh?");
    }
    
    public static void add(final EventOnce event) {
        if (event.getOriginalMilliSecondDelay() > 10000L) {
            throw new RuntimeException("Short events cannot exceed 10 seconds");
        }
        ShortEventDispatcher.events.add(event);
        if (ShortEventDispatcher.timer == null) {
            startPolling();
        }
    }
    
    private static void poll() {
        if (ShortEventDispatcher.running) {
            return;
        }
        if (ShortEventDispatcher.events.size() == 0) {
            stopPolling();
            return;
        }
        ShortEventDispatcher.running = true;
        final ArrayList<EventOnce> executed = new ArrayList<EventOnce>();
        final long ts = System.currentTimeMillis();
        final EventOnce[] eventsCopy = ShortEventDispatcher.events.toArray(new EventOnce[0]);
        EventOnce[] array;
        for (int length = (array = eventsCopy).length, i = 0; i < length; ++i) {
            final EventOnce event = array[i];
            if (event.getInvokeAt() < ts && event.invoke()) {
                executed.add(event);
            }
        }
        if (executed.size() > 0) {
            ShortEventDispatcher.events.removeAll(executed);
        }
        ShortEventDispatcher.running = false;
    }
}
