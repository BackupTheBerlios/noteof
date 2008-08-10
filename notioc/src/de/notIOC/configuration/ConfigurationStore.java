package de.notIOC.configuration;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Read and (in a later version) write XML files.
 * 
 * @author Dirk
 * 
 */
public class ConfigurationStore {

    private static List<String> configurationFiles;
    private static ConfigurationStore theStore = new ConfigurationStore();

    private ConfigurationStore() {

    }

    public ConfigurationStore getInstance() {
        return theStore;
    }

    /**
     * The master file contains the single xml files.
     * 
     * @param fileName
     */
    public void setMasterXmlFile(String fileName) {

    }

    public void addXmlFile(String fileName) {
        configurationFiles.add(fileName);
    }

    @SuppressWarnings("unused")
    private Element readXmlFile(Document doc, String elementName) {
        Element root = doc.getRootElement();

        Element child = searchChildRecursive(root, elementName);
        return child;
    }

    private Element searchChildRecursive(Element parentElement, String elementName) {
        Element searchedChild = null;
        if (elementName.contains(".")) {
            String childName = elementName.substring(0, elementName.indexOf("."));
            elementName = elementName.substring(elementName.indexOf(".") + 1);
            Element parent = parentElement.getChild(childName);
            if (null != parent) {
                searchedChild = searchChildRecursive(parent, elementName);
            }
        } else {
            searchedChild = parentElement.getChild("elementName");
        }

        return searchedChild;
    }
}
