package de.happtick.configuration;

import java.util.HashMap;
import java.util.Map;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.util.Util;

/**
 * Used to create attributes of an ChainConfiguration object by a map or vice
 * versa
 * 
 * @author Dirk
 * 
 */
public class EventConfigurationWrapper {

    private EventConfiguration eventConfiguration;
    private Map<String, String> map;

    /**
     * This constructor creates a map with attribute-values of an
     * EventConfiguration.
     * <p>
     * The map with attributes is delivered by the method getMap() <br>
     * 
     * @param eventConfiguration
     *            The Object to be wrapped into map and list format.
     */
    public EventConfigurationWrapper(EventConfiguration eventConfiguration) throws ActionFailedException {
        this.eventConfiguration = eventConfiguration;
        createMap();
    }

    /**
     * This constructor creates an object of type {@link EventConfiguration}.
     * <p>
     * The map must contain all required / supported attributes of class
     * EventConfiguration. <br>
     * Ensure that one value of the map must contain the chainId!
     * 
     * @param map
     *            Map which contains the attributes, identified by keys which
     *            names are identical with the attribute names of the class.
     */
    public EventConfigurationWrapper(Map<String, String> map) {
        this.map = map;
        createEventConfiguration();
    }

    /**
     * Delivers the Map which was created by attributes of the
     * EventConfiguration (see construction of this wrapper).
     * 
     * @return The map or NULL if an error occured by the conversion.
     */
    public Map<String, String> getMap() {
        return map;
    }

    /**
     * Returns an EventConfiguration object which was created by the map values
     * (see construction of this wrapper).
     * 
     * @return Object of type {@link EventConfiguration}
     */
    public EventConfiguration getEventConfiguration() {
        return eventConfiguration;
    }

    /*
     * wrap object to map
     */
    private void createMap() throws ActionFailedException {
        // all data except the map of calling arguments (parameters)
        // as one big list
        map = new HashMap<String, String>();
        map.put("eventId", String.valueOf(eventConfiguration.getEventId()));
        map.put("eventClassName", String.valueOf(eventConfiguration.getEventClassName()));
        map.put("keyName", String.valueOf(eventConfiguration.getKeyName()));
        map.put("keyValue", String.valueOf(eventConfiguration.getKeyValue()));
        map.put("applicationId", String.valueOf(eventConfiguration.getApplicationId()));
        map.put("addresseeId", String.valueOf(eventConfiguration.getAddresseeId()));
        map.put("addresseeType", String.valueOf(eventConfiguration.getAddresseeType()));
        map.put("action", String.valueOf(eventConfiguration.getAction()));
        map.put("raiseId", String.valueOf(eventConfiguration.getRaiseId()));
    }

    /*
     * wrap map to object
     */
    private void createEventConfiguration() {
        eventConfiguration = new EventConfiguration(Long.valueOf(map.get("eventId")));
        // atomize the vars to class attributes
        eventConfiguration.setEventClassName(map.get("eventClassName"));
        eventConfiguration.setKeyName(map.get("keyName"));
        eventConfiguration.setKeyValue(map.get("keyValue"));
        eventConfiguration.setApplicationId(Util.parseLong(map.get("applicationId"), -1));
        eventConfiguration.setAddresseeId(Util.parseLong(map.get("addresseeId"), -1));
        eventConfiguration.setAddresseeType(map.get("addresseeType"));
        eventConfiguration.setAction(map.get("action"));
        eventConfiguration.setRaiseId(map.get("raiseId"));
    }
}
