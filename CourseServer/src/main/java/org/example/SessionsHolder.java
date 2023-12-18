package org.example;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;

public class SessionsHolder implements Serializable {

    public SessionsHolder(Properties properties) {
        float minutesToLive = Float.parseFloat(properties.getProperty("sessionTimeToLiveMin"));
        this.sessionTimeToLive = (long) (minutesToLive * 60_000);
    }
    public synchronized void copyHolder(SessionsHolder other){
        sessions.clear();
        this.sessions.putAll(other.sessions);
    }
    private HashMap<Long, QuSession> sessions = new HashMap<>();

    synchronized void addSession(long sessionId, QuSession session){
        sessions.put(sessionId, session);
    }

    synchronized QuSession getSession(long sessionId) {
        return sessions.get(sessionId);
    }

    synchronized void removeSession(long sessionId) { sessions.remove(sessionId); }

    private final long sessionTimeToLive;

    synchronized void update(){
        for (var items = sessions.entrySet().iterator(); items.hasNext();) {    // итератор избегает concurrent modification exception
            var item = items.next();
            long delta = System.currentTimeMillis() - item.getValue().lastUpdated;
            if (delta > sessionTimeToLive){
                items.remove();
                System.out.println("removed " + item.getKey());
            }
        }
    }

    synchronized void clear(){
        sessions.clear();
    }

}
