package de.happtick.configuration;

import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.logging.LocalLog;

/**
 * Represents the configuration of raised events
 * 
 * @author Dirk
 * 
 */
public class RaiseConfiguration {

    private String raiseId;
    private String nodeNameRaise;
    private List<String> eventAliases = new ArrayList<String>();

    /**
     * Simple constructor
     */
    public RaiseConfiguration(String raiseId) {
        this.raiseId = raiseId;
    }

    /**
     * Using this constructor the class fills up itself with data by reading the
     * configuration.
     * 
     * @param nodeNameEvent
     *            The xml path of the events configuration (e.g.
     *            scheduler.events.raise0).
     * @param conf
     *            Object for reading access to the configuration
     */
    public RaiseConfiguration(String nodeNameRaise, NotEOFConfiguration conf) throws ActionFailedException {
        try {
            this.setNodeNameRaise(nodeNameRaise);

            String node = "";

            // eventId
            // scheduler.events.raise0
            node = "scheduler.events." + nodeNameRaise;
            eventAliases = conf.getTextList(node + ".eventAlias");

        } catch (Exception ex) {
            LocalLog.error("Konfiguration der Folge-Events konnte nicht fehlerfrei gelesen werden. Event: " + nodeNameRaise, ex);
            throw new ActionFailedException(401, "Initialisierung Folge-Events", ex);
        }
    }

    public List<String> getEventAliases() {
        return eventAliases;
    }

    public void setEventIds(List<String> eventAliases) {
        this.eventAliases = eventAliases;
    }

    public void addAlias(String eventAlias) {
        eventAliases.add(eventAlias);
    }

    /**
     * @param raiseId
     *            the raiseId to set
     */
    public void setRaiseId(String raiseId) {
        this.raiseId = raiseId;
    }

    /**
     * @return the raiseId
     */
    public String getRaiseId() {
        return raiseId;
    }

    /**
     * @param nodeNameRaise
     *            the nodeNameRaise to set
     */
    public void setNodeNameRaise(String nodeNameRaise) {
        this.nodeNameRaise = nodeNameRaise;
    }

    /**
     * @return the nodeNameRaise
     */
    public String getNodeNameRaise() {
        return nodeNameRaise;
    }
}
