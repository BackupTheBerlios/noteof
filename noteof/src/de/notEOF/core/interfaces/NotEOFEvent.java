package de.notEOF.core.interfaces;

import java.util.Map;

import de.notEOF.core.enumeration.EventType;
import de.notEOF.core.exception.ActionFailedException;

public interface NotEOFEvent {

    /**
     * Returns the event type.
     * 
     * @see EventType
     */
    public EventType getEventType();

    /**
     * Attributes are very special to the different events. <br>
     * They contain additional informations (e.g. states, error values etc.). <br>
     * They help to interpret the exact meaning of the raised event. <br>
     * Attributes, their keys and the values depend to the implementation of
     * applications which use the framework. <br>
     * If there exists no documentation about the implementation of an event at
     * least the developer(s) should deliver descriptions with the function
     * getAttributeDescriptions().
     * 
     * @return A map which stores the attributes and values of an event.
     */
    public Map<String, String> getAttributes();

    /**
     * Attributes are very special to the different events. <br>
     * They contain additional informations (e.g. states, error values etc.). <br>
     * They help to interpret the exact meaning of the raised event. <br>
     * Attributes, their keys and the values depend to the implementation of
     * applications which use the framework. <br>
     * If there exists no documentation about the implementation of an event at
     * least the developer(s) should deliver descriptions with the function
     * getAttributeDescriptions().
     * 
     * @return The value of the attribute which is specified by the exactly used
     *         key or NULL if no attributes are stored or the searched key
     *         doesn't exists.
     */
    public String getAttribute(String key);

    /**
     * Delivers a map with descriptions for every attribute.
     * <p>
     * The keys of the map entries must match exactly to the keys of the
     * attributes map. <br>
     * So it is possible for a recipient to analyse the received event without
     * the knowledgement about the individual datas.
     * 
     * @return A map which stores the keys which are identical to the keys of
     *         the attributes map. The values are descriptions of the datas
     *         which are stored in the attributes map. If there is no special
     *         attribute the Map is NULL.
     */
    public Map<String, String> getAttributeDescriptions();

    /**
     * Add attribute and value to the Attribute map.
     * 
     * @param key
     *            The key of the attribute. This key later must be used to have
     *            access to the value.
     * @param value
     *            The value of the attribute which is defined by key.
     * @throws ActionFailedException
     *             The implementation of an event requires that the developer
     *             initializes the map which stores the attribute keys and
     *             descriptions of the attributes -> see
     *             getAttributeDescriptions(). <br>
     *             If an attribute must be set by using the method
     *             addAttribute() and there doesn't exists a description for the
     *             used key the exception is thrown. <br>
     *             Other exceptions like Null pointer can force the
     *             ActionFailedException too.
     */
    public void addAttribute(String key, String value) throws ActionFailedException;

    public void addAttributeDescription(String key, String description) throws ActionFailedException;

    public void setAttributes(Map<String, String> attributes);

    public void setDescriptions(Map<String, String> descriptions);

    public void setEventType(EventType eventType);

    public void setApplicationId(Long applicationId);

    public Long getApplicationId();

    public Long getQueueId();

    public void setQueueId(Long queueId);

    public Long getRequestQueueId();

    public void setRequestQueueId(Long queueId);

    public Long getTimeStampSend();

    public void setTimeStampSend();
}
