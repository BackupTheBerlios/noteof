package de.happtick.configuration;

import java.util.ArrayList;
import java.util.List;

import de.notEOF.core.exception.ActionFailedException;
import de.notEOF.core.interfaces.NotEOFConfiguration;
import de.notEOF.core.logging.LocalLog;
import de.notEOF.core.util.Util;

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
    private List<ChainLink> linkList = new ArrayList<ChainLink>();

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
                    ChainLink link = new ChainLink(this.chainId, node, conf);
                    linkList.add(link);
                }
            }

        } catch (Exception ex) {
            LocalLog.error("Konfiguration der Ketten konnte nicht fehlerfrei gelesen werden. Chain: " + nodeNameChain, ex);
            throw new ActionFailedException(401, "Initialisierung ChainConfiguration", ex);
        }
    }

    public List<ChainLink> getChainLinkList() {
        return linkList;
    }

    public void addLink(ChainLink chainLink) {
        linkList.add(chainLink);
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
