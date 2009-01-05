package de.notEOF.core.brokerage;

import de.notEOF.configuration.LocalConfiguration;
import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.EventBroker;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.interfaces.NotEOFEvent;
import de.notEOF.core.interfaces.Service;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.server.Server;
import de.notEOF.core.util.NotEOFClassFinder;

public class EventDistributor {
    private static EventBroker acceptor;

    @SuppressWarnings("unchecked")
    public synchronized static void postEvent(Service service, NotEOFEvent event) throws ActionFailedException {
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
                acceptor = new EventBroadcaster();
            } catch (IllegalAccessException e) {
                LocalLog.warn("Zugriff auf Konstruktor der Klasse nicht moeglich: " + acceptorClassName + "; Default-Klasse wird verwendet: "
                        + "de.notEOF.core.util.EventBroker", e);
                acceptor = new EventBroadcaster();
            }

            LocalLog.info("Verwende jetzt als Broker: " + acceptor.getClass().getCanonicalName());
        }

        // Class loaded - post event
        acceptor.postEvent(service, event);
    }
}
