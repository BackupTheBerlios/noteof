package de.notEOF.core.brokerage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventBroker;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.NotEOFClassFinder;
import de.notEOF.core.util.Statistics;

public class StaticBroker {
    private static EventBroker acceptor;

    public synchronized static void postEvent(Service service, NotEOFEvent event) throws ActionFailedException {
        // Class loaded - post event
        Long beginTime = new Date().getTime();
        getBrokerInstance().postEvent(service, event);
        Statistics.setEventDuration(new Date().getTime() - beginTime);
    }

    public static void registerForEvents(EventObserver eventObserver, Long lastReceivedQueueId) {
        try {
            getBrokerInstance().registerForEvents(eventObserver, lastReceivedQueueId);
            Statistics.addNewObserver();
        } catch (ActionFailedException e) {
            LocalLog.warn("Observer konnte nicht am Broker registriert werden. Observer: " + eventObserver.getName(), e);
        }
    }

    public static void unregisterFromEvents(EventObserver eventObserver) {
        try {
            getBrokerInstance().unregisterFromEvents(eventObserver);
            Statistics.addFinishedObserver();
        } catch (ActionFailedException e) {
            LocalLog.warn("Observer konnte nicht vom Broker abgemeldet werden. Observer: " + eventObserver.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static EventBroker getBrokerInstance() throws ActionFailedException {
        // class can be configured
        if (null == acceptor) {
            NotEOFConfiguration conf = new LocalConfiguration();

            String acceptorClassName;
            try {
                acceptorClassName = conf.getAttribute("brokerage.EventBroker", "class");
            } catch (ActionFailedException e) {
                // default class
                LocalLog.warn("Konfiguration fuer Broker-Klasse konnte nicht gelesen werden; Default-Klasse wird verwendet: "
                        + "de.notEOF.core.util.EventBroker");
                acceptorClassName = "de.notEOF.core.util.EventBroker";
            }

            Class<EventBroker> classAcceptor = (Class<EventBroker>) NotEOFClassFinder.getClass(Server.getApplicationHome(), acceptorClassName);

            try {
                acceptor = classAcceptor.newInstance();

            } catch (InstantiationException e) {
                LocalLog.warn("Es konnte keine Instanz fuer die Broker-Klasse gebildet werden: " + acceptorClassName + "; Default-Klasse wird verwendet: "
                        + "de.notEOF.core.util.EventBroker", e);
                acceptor = new EventBroadcast();
            } catch (IllegalAccessException e) {
                LocalLog.warn("Zugriff auf Konstruktor der Klasse nicht moeglich: " + acceptorClassName + "; Default-Klasse wird verwendet: "
                        + "de.notEOF.core.util.EventBroker", e);
                acceptor = new EventBroadcast();
            }

            LocalLog.info("Verwende jetzt als Broker: " + acceptor.getClass().getCanonicalName());
        }

        return acceptor;
    }

    public static List<EventObserver> getSystemInfoObservers() {
        if (null == acceptor || null == acceptor.getEventObservers())
            return null;
        List<EventObserver> systemObservers = new ArrayList<EventObserver>();
        for (EventObserver observer : acceptor.getEventObservers()) {
            for (EventType type : observer.getObservedEvents()) {
                if (type.equals(EventType.EVENT_SYSTEM_INFO)) {
                    systemObservers.add(observer);
                    break;
                }
            }
        }
        return systemObservers;
    }
}
