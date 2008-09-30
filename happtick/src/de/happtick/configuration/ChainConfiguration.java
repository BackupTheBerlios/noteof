package de.happtick.configuration;

import java.util.List;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.util.Util;
import de.notIOC.logging.LocalLog;

/**
 * Represents the configuration of a chain which is stored in the configuration
 * file.
 * 
 * @author Dirk
 * 
 */
public class ChainConfiguration {

    private Long chainId;
    private String nodeNameChain;
    private boolean depends;
    private boolean loop;
    private List<Link> linkList;

    /**
     * Simple constructor
     */
    public ChainConfiguration(Long chainId) {
        this.chainId = chainId;
    }

    /**
     * Using this constructor the class fills up itself with data by reading the
     * configuration.
     * 
     * @param nodeNameChain
     *            The xml path of the chains configuration (e.g.
     *            scheduler.chains.chain.chain1).
     * @param conf
     *            Object for reading access to the configuration
     */
    public ChainConfiguration(String nodeNameChain, NotEOFConfiguration conf) throws ActionFailedException {
        try {
            this.nodeNameChain = nodeNameChain;

            String node = "";

            // chainId
            // scheduler.chains.chain1
            node = "scheduler.chains." + nodeNameChain;
            chainId = Util.parseLong(conf.getAttribute(node, "chainId", "-1"), -1);
            // dependency
            depends = Util.parseBoolean(conf.getAttribute(node, "depends"), false);
            // loop
            loop = Util.parseBoolean(conf.getAttribute(node, "loop"), false);

            // list of chain links
            // scheduler.chains.chain1.link
            List<String> linkNames = conf.getTextList(node + ".link");
            if (null != linkNames) {
                for (String linkName : linkNames) {
                    node = "scheduler.chains." + nodeNameChain + "." + linkName;
                    // scheduler.chains.chain1.link0
                    Link link = new Link(node, conf);
                    linkList.add(link);
                }
            }

        } catch (Exception ex) {
            LocalLog.error("Konfiguration der Ketten konnte nicht fehlerfrei gelesen werden. Applikation: " + nodeNameChain, ex);
            throw new ActionFailedException(401, "Initialisierung ChainConfiguration", ex);
        }
    }

    /*
     * The parts of a chain
     */
    private class Link {
        private Long id;
        private String type;
        private Long conditionEventId;
        private Long preventEventId;
        private boolean skip;

        private Link(String node, NotEOFConfiguration conf) throws ActionFailedException {
            // sample node: scheduler.chains.chain1.link0
            id = Util.parseLong(conf.getAttribute(node, "id"), -1);
            type = conf.getAttribute(node, "type");
            conditionEventId = Util.parseLong(conf.getAttribute(node, "conditionEventId"), -1);
            preventEventId = Util.parseLong(conf.getAttribute(node, "preventEventId"), -1);
            skip = Util.parseBoolean(conf.getAttribute(node, "skip"), false);
        }

        private Link(Long id, String type, Long conditionEventId, Long preventEventId, boolean skip) {
            this.id = id;
            this.type = type;
            this.conditionEventId = conditionEventId;
            this.preventEventId = preventEventId;
            this.skip = skip;
        }

        /**
         * @return the skip
         */
        private boolean isSkip() {
            return skip;
        }

        /**
         * @return the preventEvendId
         */
        private Long getPreventEventId() {
            if (-1 != preventEventId)
                return preventEventId;
            return null;
        }

        /**
         * @return the conditionEventId
         */
        private Long getConditionEventId() {
            if (-1 != conditionEventId)
                return conditionEventId;
            return null;
        }

        /**
         * @return the id
         */
        private Long getId() {
            return id;
        }

        /**
         * @return the type
         */
        private String getType() {
            return type;
        }
    }

    /**
     * A chain consists of chain links. <br>
     * To get them and their details this strategy is helpful: <br>
     * - At first check how many links there are. <br>
     * - The informations of the link data can be get by the index of the link
     * and some methods: <br>
     * -- getLinkId() <br>
     * -- getLinkType() <br>
     * -- getLinkConditionEventId() <br>
     * -- getLinkPreventEvendId() <br>
     * -- getLinkSkip() <br>
     * 
     * @return
     */
    public int getLinkCount() {
        if (null != linkList) {
            return linkList.size();
        }
        return 0;
    }

    /**
     * Adds a link to the chain.
     * 
     * @param id
     *            Id like set in configuration.
     * @param type
     *            Type ('application' or 'chain').
     * @param conditionEventId
     *            Id of an event which must be raised before executing the link.
     * @param preventEventId
     *            Id of an event which mustn't be thrown.
     * @param skip
     *            If skip is TRUE and the PreventEvent was thrown the link is
     *            skipped and the next link will be executed.
     */
    public void addToLinkList(Long id, String type, Long conditionEventId, Long preventEventId, boolean skip) {
        Link link = new Link(id, type, conditionEventId, preventEventId, skip);
        linkList.add(link);
    }

    /**
     * Delivers the id of the chain link. <br>
     * 
     * @param linkListIndex
     *            The link data are encapsulated in a list of the class
     *            ChainConfiguration. To get them you must use the index of this
     *            list. The order of the links is the same as stored in
     *            configuration. <br>
     *            The linkListIndex is - like in all java lists - 0 based. <br>
     *            The method getLinkCount() shows how many entries the list has.
     * @return The id of the link.
     * @throws ActionFailedException
     *             When used Index is out of bound or internal list is empty
     *             (getLinkCount() has delivered 0).
     */
    public Long getLinkId(int linkListIndex) throws ActionFailedException {
        return linkList.get(linkListIndex).getId();
    }

    /**
     * Delivers the type of the chain link ('application' or 'chain'). <br>
     * 
     * @param linkListIndex
     *            The link data are encapsulated in a list of the class
     *            ChainConfiguration. To get them you must use the index of this
     *            list. The order of the links is the same as stored in
     *            configuration. <br>
     *            The method getLinkCount() shows how many entries the list has.
     *            The linkListIndex is - like in all java lists - 0 based.
     * @return The type of the link or 'application' as default.
     * @throws ActionFailedException
     *             When used Index is out of bound or internal list is empty
     *             (getLinkCount() has delivered 0).
     */
    public String getLinkType(int linkListIndex) throws ActionFailedException {
        String type = linkList.get(linkListIndex).getType();
        if (null != type)
            return type;
        return "application";
    }

    /**
     * Delivers the event which must be raised by the pre executed link. <br>
     * 
     * @param linkListIndex
     *            The link data are encapsulated in a list of the class
     *            ChainConfiguration. To get them you must use the index of this
     *            list. The order of the links is the same as stored in
     *            configuration. <br>
     *            The method getLinkCount() shows how many entries the list has.
     *            The linkListIndex is - like in all java lists - 0 based.
     * @return The id of an event which must be raised as a condition to perform
     *         this link or NULL.
     * @throws ActionFailedException
     *             When used Index is out of bound or internal list is empty
     *             (getLinkCount() has delivered 0).
     */
    public Long getLinkConditionEventId(int linkListIndex) throws ActionFailedException {
        return linkList.get(linkListIndex).getConditionEventId();
    }

    /**
     * Delivers the event which prevents the execution of this link. <br>
     * 
     * @param linkListIndex
     *            The link data are encapsulated in a list of the class
     *            ChainConfiguration. To get them you must use the index of this
     *            list. The order of the links is the same as stored in
     *            configuration. <br>
     *            The method getLinkCount() shows how many entries the list has.
     *            The linkListIndex is - like in all java lists - 0 based.
     * @return The id of an event which may not be raised by the link before. If
     *         not configured this method delivers NULL.
     * @throws ActionFailedException
     *             When used Index is out of bound or internal list is empty
     *             (getLinkCount() has delivered 0).
     */
    public Long getLinkPreventEventId(int linkListIndex) throws ActionFailedException {
        return linkList.get(linkListIndex).getPreventEventId();
    }

    /**
     * Tells if the link must be ignored by execution when the prevent event was
     * raised before. <br>
     * 
     * @param linkListIndex
     *            The link data are encapsulated in a list of the class
     *            ChainConfiguration. To get them you must use the index of this
     *            list. The order of the links is the same as stored in
     *            configuration. <br>
     *            The method getLinkCount() shows how many entries the list has.
     *            The linkListIndex is - like in all java lists - 0 based.
     * @return TRUE or FALSE. If not configured it is FALSE (default).
     * @throws ActionFailedException
     *             When used Index is out of bound or internal list is empty
     *             (getLinkCount() has delivered 0).
     */
    public boolean getLinkSkip(int linkListIndex) throws ActionFailedException {
        return linkList.get(linkListIndex).isSkip();
    }

    public Long getChainId() {
        return chainId;
    }

    public String getNodeNameChain() {
        return nodeNameChain;
    }

    public void setNodeNameChain(String nodeNameChain) {
        this.nodeNameChain = nodeNameChain;
    }

    public boolean isDepends() {
        return depends;
    }

    public void setDepends(boolean depends) {
        this.depends = depends;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }
}
