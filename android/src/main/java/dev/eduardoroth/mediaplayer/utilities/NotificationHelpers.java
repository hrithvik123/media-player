package dev.eduardoroth.mediaplayer.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NotificationHelpers {
    private static NotificationHelpers _instance;
    private final HashMap<String, ArrayList<RunnableHelper>> registeredObjects;

    private NotificationHelpers() {
        registeredObjects = new HashMap<String, ArrayList<RunnableHelper>>();
    }

    public static synchronized NotificationHelpers defaultCenter() {
        if (_instance == null) _instance = new NotificationHelpers();
        return _instance;
    }

    public synchronized void addMethodForNotification(String notificationName, RunnableHelper r) {
        ArrayList<RunnableHelper> list = registeredObjects.computeIfAbsent(notificationName, k -> new ArrayList<RunnableHelper>());
        list.add(r);
    }

    public synchronized void removeMethodForNotification(String notificationName, RunnableHelper r) {
        ArrayList<RunnableHelper> list = registeredObjects.get(notificationName);
        if (list != null) {
            list.remove(r);
        }
    }

    public synchronized void removeAllNotifications() {
        for (Iterator<Map.Entry<String, ArrayList<RunnableHelper>>> entry = registeredObjects.entrySet().iterator(); entry.hasNext();) {
            Map.Entry<String, ArrayList<RunnableHelper>> e = entry.next();
            String key = e.getKey();
            ArrayList<RunnableHelper> value = e.getValue();
            removeMethodForNotification(key, value.get(0));
            entry.remove();
        }
    }

    public synchronized void postNotification(String notificationName, Map<String, Object> _info) {
        ArrayList<RunnableHelper> list = registeredObjects.get(notificationName);
        if (list != null) {
            for (RunnableHelper r : new ArrayList<>(list)) {
                r.setInfo(_info);
                r.run();
            }
        }
    }
}
