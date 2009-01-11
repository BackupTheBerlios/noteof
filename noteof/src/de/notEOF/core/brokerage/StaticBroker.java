package de.notEOF.core.brokerage;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventBroker;
import de.notEOF.core.interfaces.EventObserver;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.NotEOFClassFinder;

public class StaticBroker {
    private static EventBroker acceptor;

    public synchronized static void postEvent(Service service, NotEOFEvent event) throws ActionFailedException {
        // Class loaded - post event
        getBrokerInstance().postEvent(service, event);
    }

    public static void registerForEvents(EventObserver eventObserver, Long lastReceivedQueueId) {
        System.out.println("Registrieren will sich: " + eventObserver.getName());
        try {
            getBrokerInstance().registerForEvents(eventObserver, lastReceivedQueueId);
        } catch (ActionFailedException e) {
            LocalLog.warn("Observer konnte nicht am Broker registriert werden. Observer: " + eventObserver.getName(), e);
        }
    }

    public static void unregisterFromEvents(EventObserver eventObserver) {
        try {
            getBrokerInstance().unregisterFromEvents(eventObserver);
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

}
