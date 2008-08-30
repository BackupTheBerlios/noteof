package de.notIOC.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import de.notIOC.exception.NotIOCException;
import de.notIOC.util.Util;

/**
 * Read and (in a later version) write XML files.
 * 
 * @author Dirk
 * 
 */
public class ConfigurationStore {

    private static SAXBuilder saxBuilder = new SAXBuilder();
    private String masterXmlFile;
    private List<String> configurationFiles;
    private static ConfigurationStore theStore = new ConfigurationStore();
    private Map<String, Document> docMap;

    // singleton
    private ConfigurationStore() {

    }

    public static ConfigurationStore getInstance() {
        return theStore;
    }

    /**
     * The master file contains the single xml files.
     * 
     * @param fileName
     */
    public static void setMasterXmlFile(String xmlFileName) throws NotIOCException {
        theStore.setXmlFile(xmlFileName, false);
    }

    /**
     * Returns an node element of the xml configuration.
     * <p>
     * 
     * @param elementName
     *            Is the node description. <br>
     *            E.g. myNode or allNodes.myNode
     * @return A list with found elements with 1 or more elements.
     * @throws NotIOCException
     */
    public synchronized List<Element> getElement(String elementName) throws NotIOCException {
        List<Element> foundElement = null;
        for (String sourceFileName : configurationFiles) {
            foundElement = readXmlFile(sourceFileName, elementName, false);
            if (null != foundElement)
                break;
        }
        return foundElement;
    }

    /*
     * Add an xml file to the configuration.
     */
    private void addXmlFile(String fileName) {
        if (null == configurationFiles)
            configurationFiles = new ArrayList<String>();
        configurationFiles.add(fileName);
    }

    /**
     * Refreshes the complete Configuration. Is only allowed if the method
     * setXmlFile() was used ago. If setXmlFile() wasn't called at an earlier
     * time point this method throws an exception.
     * 
     * @throws NotIOCException
     */
    public synchronized void refresh() throws NotIOCException {
        if (null == this.masterXmlFile) {
            throw new NotIOCException(7L, "Konfiguration wurde bisher nicht initialisiert.");
        }
        setXmlFile(this.masterXmlFile, true);
    }

    /*
     * Initialize the whole configuration by adding the single configuration
     * files which are stored in a master file.
     */
    private void setXmlFile(String xmlFileName, boolean refresh) throws NotIOCException {
        if (!refresh && null != this.masterXmlFile)
            throw new NotIOCException(6L, "Xml Datei: " + xmlFileName + "Bereits initialisiert mit: " + this.masterXmlFile);

        this.masterXmlFile = xmlFileName;

        File masterXmlFile = new File(xmlFileName);
        String confPath = masterXmlFile.getParent();

        List<Element> confFileElements = readXmlFile(xmlFileName, "configuration.xml", false);
        if (null != confFileElements) {
            for (Element confFileElement : confFileElements) {
                String confFileName = confPath + "/" + confFileElement.getAttributeValue("fileName");
                if (!Util.isEmpty(confFileName)) {
                    try {
                        File confFile = new File(confFileName);
                        addXmlFile(confFile.getCanonicalPath());
                    } catch (IOException e) {
                        throw new NotIOCException(5L, "File Name: " + xmlFileName, e);
                    }
                }
            }
        }
    }

    /*
     * Read an xml file and search for elements.
     */
    private synchronized List<Element> readXmlFile(String sourceFileName, String elementName, boolean refresh) throws NotIOCException {
        // look for the document in the map...
        // should optimize the performance becaus read on every xml file is
        // needed only one time
        if (refresh || null == docMap) {
            docMap = new HashMap<String, Document>();
        }
        Document doc = docMap.get(sourceFileName);
        if (null == doc) {
            try {
                doc = saxBuilder.build(new File(sourceFileName));
                docMap.put(sourceFileName, doc);
            } catch (Exception e) {
                e.printStackTrace();
                throw new NotIOCException(5L, "Generieren des XML-Documents", e);
            }
        }

        Element root = doc.getRootElement();
        List<Element> children = searchChildrenRecursive(root, elementName);
        return children;
    }

    /*
     * Recursively navigate through the xml tree and search for elements.
     */
    @SuppressWarnings("unchecked")
    private List<Element> searchChildrenRecursive(Element parentElement, String elementName) {
        List<Element> searchedChildren = null;
        if (elementName.contains(".")) {
            String childName = elementName.substring(0, elementName.indexOf("."));
            elementName = elementName.substring(elementName.indexOf(".") + 1);

            List<Element> parents = parentElement.getChildren(childName);
            if (null != parents && parents.size() > 0) {
                for (Element parent : parents) {
                    if (null != searchedChildren)
                        break;
                    searchedChildren = searchChildrenRecursive(parent, elementName);
                }
            } else {
                Element parent = parentElement.getChild(childName);
                if (null != parent)
                    searchedChildren = searchChildrenRecursive(parent, elementName);
            }

        } else {
            List<Element> children = parentElement.getChildren(elementName);
            if (null != children && children.size() > 0) {
                return children;
            } else {
                Element child = parentElement.getChild(elementName);
                if (null != child) {
                    searchedChildren = new ArrayList();
                    searchedChildren.add(child);
                }
            }
        }

        return searchedChildren;
    }
}
