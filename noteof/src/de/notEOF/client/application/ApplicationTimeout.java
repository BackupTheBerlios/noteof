package de.notEOF.client.application;

import de.notEOF.core.communication.BaseTimeout;

/**
 * Additional to communication timeout and connection timeout the application
 * needs a timeout interval for sending lifesigns.
 * 
 * @author Dirk
 */
public class ApplicationTimeout extends BaseTimeout {

    private int lifeTimeIntervalMillis = 30000;

    /**
     * All lifeTimeIntervalMillis the client will send a signal to the service.
     * 
     * @param lifeTimeIntervalMillis
     *            The interval which is used for sending alive messages.
     */
    public void setLifeTimeIntervalMillis(int lifeTimeIntervalMillis) {
        this.lifeTimeIntervalMillis = lifeTimeIntervalMillis;
    }

    /**
     * All lifeTimeIntervalMillis the client will send a signal to the service.
     * 
     * @param lifeTimeIntervalMillis
     *            The interval which is used for sending alive messages.
     */
    public int getLifeTimeIntervalMillis() {
        return lifeTimeIntervalMillis;
    }
}
