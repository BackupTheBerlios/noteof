package de.notIOC.configuration;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import de.notIOC.exception.NotIOCException;

/**
 * A ConfigProperty holds informations about one configuration part.
 * <p>
 * The object will be constructed by using the node of xml schema. After that
 * text or attribute values are available.
 * 
 * @author Dirk
 * 
 */
public class ConfigProperty {
    private List<Element> propertyList;

    /**
     * Initialization of the ConfigProperty by nodeName.
     * 
     * @param nodeName
     *            Is the xml path to a tag within the configuration. E.g.
     *            scheduler.chains.chain
     * @throws NotIOCException
     */
    public ConfigProperty(String nodeName) throws NotIOCException {
        propertyList = ConfigurationStore.getInstance().getElement(nodeName);
    }

    /**
     * Delivers the attribute value of the property.
     * 
     * @param attributeName
     *            Name of the attribute. E.g. chain here is the attribute < use
     *            chain="true" >< /use >
     * @param defaultValue
     *            If no value is found the default value will be returned.
     * @return Found attribute value or defaultValue
     */
    public String getAttribute(String attributeName, String defaultValue) {
        String value = getAttribute(attributeName);
        if (null == value) {
            return null;
        }
        return value;
    }

    /**
     * Delivers the text value of the property.
     * 
     * @param defaultValue
     *            If no value is found the default value will be returned.
     * @return Found text value or defaultValue
     */
    public String getText(String defaultValue) {
        String text = getText();
        if (null == text) {
            return null;
        }
        return text;
    }

    /**
     * Returns the attribute value of the attribute with attributeName. <br>
     * If there were found more elements than one for the config property the
     * first found attribute will be delivered here.
     * 
     * @param attributeName
     *            The name of the attribute <br>
     *            Sample: < param key="123" >< /param > // key is the attribute
     *            name
     * @return Value of the first found attribute with the attribute name. If no
     *         element contains such a attribute the method delivers null.
     */
    public String getAttribute(String attributeName) {
        if (null == propertyList)
            return null;
        for (Element element : propertyList) {
            String attributeValue = element.getAttributeValue(attributeName);
            if (null != attributeValue)
                return attributeValue;
        }
        return null;
    }

    /**
     * Returns the attribute text of the attribute. <br>
     * If there were found more elements than one for the config property the
     * first elements text will be delivered here.
     * 
     * @return The simple text of the first element. If no element contains a
     *         text the method returns null.
     */
    public String getText() {
        if (null == propertyList)
            return null;
        for (Element element : propertyList) {
            String textValue = element.getText();
            if (null != textValue)
                return textValue;
        }
        return null;
    }

    /**
     * Returns a list with the values of the attribute with attributeName.
     * 
     * @param attributeName
     *            The name of the attribute <br>
     *            Sample: < param key="123" >< /param > // key is the attribute
     *            name
     * @return List with all attributes with the attribute name. Values for not
     *         existing attributes are a String with size 0 ("").
     */
    @SuppressWarnings("unchecked")
    public List<String> getAttributeList(String attributeName) {
        if (null == propertyList)
            return null;
        List attributeList = new ArrayList<String>();
        for (Element element : propertyList) {
            String attributeValue = element.getAttributeValue(attributeName);
            if (null == attributeValue)
                attributeValue = "";
            attributeList.add(attributeValue);
        }
        return attributeList;
    }

    /**
     * Returns a list with the text of every element.
     * 
     * @return List with text for all elements. If an element has no text a
     *         String with size 0 ("") will be inserted.
     */
    @SuppressWarnings("unchecked")
    public List<String> getTextList() {
        if (null == propertyList)
            return null;
        List textList = new ArrayList<String>();
        for (Element element : propertyList) {
            String textValue = element.getText();
            if (null == textValue)
                textValue = "";
            textList.add(textValue);
        }
        return textList;
    }
}
