package de.happtick.configuration;

import java.util.HashMap;
import java.util.List;
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
public class RaiseConfigurationWrapper {

    private RaiseConfiguration raiseConfiguration;
    private Map<String, String> map;

    /**
     * This constructor creates a map with attribute-values of an
     * RaiseConfiguration.
     * <p>
     * The map with attributes is delivered by the method getMap() <br>
     * 
     * @param raiseConfiguration
     *            The Object to be wrapped into map and list format.
     */
    public RaiseConfigurationWrapper(RaiseConfiguration raiseConfiguration) throws ActionFailedException {
        this.raiseConfiguration = raiseConfiguration;
        createMap();
    }

    /**
     * This constructor creates an object of type {@link RaiseConfiguration}.
     * <p>
     * The map must contain all required / supported attributes of class
     * RaiseConfiguration. <br>
     * Ensure that one value of the map must contain the chainId!
     * 
     * @param map
     *            Map which contains the attributes, identified by keys which
     *            names are identical with the attribute names of the class.
     */
    public RaiseConfigurationWrapper(Map<String, String> map) {
        this.map = map;
        createRaiseConfiguration();
    }

    /**
     * Delivers the Map which was created by attributes of the
     * RaiseConfiguration (see construction of this wrapper).
     * 
     * @return The map or NULL if an error occured by the conversion.
     */
    public Map<String, String> getMap() {
        return map;
    }

    /**
     * Returns an RaiseConfiguration object which was created by the map values
     * (see construction of this wrapper).
     * 
     * @return Object of type {@link RaiseConfiguration}
     */
    public RaiseConfiguration getRaiseConfiguration() {
        return raiseConfiguration;
    }

    /*
     * wrap object to map
     */
    private void createMap() throws ActionFailedException {
        map = new HashMap<String, String>();
        map.put("raiseId", String.valueOf(raiseConfiguration.getRaiseId()));

        // number of links
        List<String> eventAliases = raiseConfiguration.getEventAliases();
        int numberOfAliases = eventAliases.size();
        map.put("aliasCount", String.valueOf(numberOfAliases));
        if (0 < numberOfAliases) {
            for (int i = 0; i < numberOfAliases; i++) {
                map.put("aliasName_" + i, String.valueOf(eventAliases.get(i)));
            }
        }

    }

    /*
     * wrap map to object
     */
    private void createRaiseConfiguration() {
        raiseConfiguration = new RaiseConfiguration(map.get("raiseId"));
        // atomize the vars to class attributes
        int numberOfAliases = Util.parseInt(map.get("aliasCount"), 0);
        if (0 < numberOfAliases) {
            for (int i = 0; i < numberOfAliases; i++) {
                String aliasName = map.get("aliasName_" + i);
                raiseConfiguration.addAlias(aliasName);
            }
        }
    }
}
