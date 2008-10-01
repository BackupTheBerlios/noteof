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
public class ChainConfigurationWrapper {

    private ChainConfiguration chainConfiguration;
    private Map<String, String> map;

    /**
     * This constructor creates a map with attribute-values of an
     * ChainConfiguration.
     * <p>
     * The map with attributes is delivered by the method getMap() <br>
     * 
     * @param chainConfiguration
     *            The Object to be wrapped into map and list format.
     */
    public ChainConfigurationWrapper(ChainConfiguration chainConfiguration) throws ActionFailedException {
        this.chainConfiguration = chainConfiguration;
        createMap();
    }

    /**
     * This constructor creates an object of type {@link ChainConfiguration}.
     * <p>
     * The map must contain all required / supported attributes of class
     * ChainConfiguration. <br>
     * Ensure that one value of the map must contain the chainId!
     * 
     * @param map
     *            Map which contains the attributes, identified by keys which
     *            names are identical with the attribute names of the class.
     */
    public ChainConfigurationWrapper(Map<String, String> map) {
        this.map = map;
        createChainConfiguration();
    }

    /**
     * Delivers the Map which was created by attributes of the
     * ChainConfiguration (see construction of this wrapper).
     * 
     * @return The map or NULL if an error occured by the conversion.
     */
    public Map<String, String> getMap() {
        return map;
    }

    /**
     * Returns an ChainConfiguration object which was created by the map values
     * (see construction of this wrapper).
     * 
     * @return Object of type {@link ChainConfiguration}
     */
    public ChainConfiguration getChainConfiguration() {
        return chainConfiguration;
    }

    /*
     * wrap object to map
     */
    private void createMap() throws ActionFailedException {
        // all data except the map of calling arguments (parameters)
        // as one big list
        map = new HashMap<String, String>();
        map.put("chainId", String.valueOf(chainConfiguration.getChainId()));
        map.put("depends", String.valueOf(chainConfiguration.isDepends()));
        map.put("loop", String.valueOf(chainConfiguration.isLoop()));

        // number of links
        List<ChainLink> chainLinks = chainConfiguration.getChainLinkList();
        int numberOfLinks = chainLinks.size();
        map.put("linkCount", String.valueOf(numberOfLinks));
        if (0 < numberOfLinks) {
            for (int i = 0; i < numberOfLinks; i++) {
                map.put("linkId_" + i, String.valueOf(chainLinks.get(i).getId()));
                map.put("linkType_" + i, String.valueOf(chainLinks.get(i).getType()));

                Long condId = chainLinks.get(i).getConditionEventId();
                if (null != condId)
                    map.put("linkConditionEventId_" + i, String.valueOf(condId));

                Long preventId = chainLinks.get(i).getPreventEventId();
                if (null != preventId)
                    map.put("linkPreventEventId_" + i, String.valueOf(preventId));

                map.put("linkSkip_" + i, String.valueOf(chainLinks.get(i).isSkip()));
            }
        }
    }

    /*
     * wrap map to object
     */
    private void createChainConfiguration() {
        chainConfiguration = new ChainConfiguration(Long.valueOf(map.get("chainId")));

        // atomize the vars to class attributes
        chainConfiguration.setDepends(Util.parseBoolean(map.get("depends"), false));
        chainConfiguration.setLoop(Util.parseBoolean(map.get("loop"), false));

        int numberOfLinks = Util.parseInt(map.get("linkCount"), 0);
        if (0 < numberOfLinks) {
            for (int i = 0; i < numberOfLinks; i++) {
                Long linkId = Util.parseLong(map.get("linkId_" + i), 0);
                String type = map.get("linkType_" + i);
                Long condId = Util.parseLong(map.get("linkConditionEventId_" + i), -1);
                if (-1 == condId)
                    condId = null;
                Long preventId = Util.parseLong(map.get("linkPreventEventId_" + i), -1);
                if (-1 == preventId)
                    preventId = null;
                boolean skip = Util.parseBoolean(map.get("linkSkip_" + i), false);

                ChainLink link = new ChainLink(linkId, type, condId, preventId, skip);
                chainConfiguration.addLink(link);
            }
        }
    }
}
