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
        private String type;
        private Long id;
        private Long conditionEventId;
        private Long preventEvendId;
        private boolean skip;

        public Link(String node, NotEOFConfiguration conf) throws ActionFailedException {
            // sample node: scheduler.chains.chain1.link0
            type = conf.getAttribute(node, "type");
            id = Util.parseLong(conf.getAttribute(node, "id"), -1);
            conditionEventId = Util.parseLong(conf.getAttribute(node, "conditionEventId"), -1);
            preventEvendId = Util.parseLong(conf.getAttribute(node, "preventEvendId"), -1);
            skip = Util.parseBoolean(conf.getAttribute(node, "skip"), false);
        }

        /**
         * @return the skip
         */
        public boolean isSkip() {
            return skip;
        }

        /**
         * @return the preventEvendId
         */
        public Long getPreventEvendId() {
            return preventEvendId;
        }

        /**
         * @return the conditionEventId
         */
        public Long getConditionEventId() {
            return conditionEventId;
        }

        /**
         * @return the id
         */
        public Long getId() {
            return id;
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

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
