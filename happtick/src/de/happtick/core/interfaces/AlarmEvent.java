package de.happtick.core.interfaces;

import de.notEOF.core.interfaces.NotEOFEvent;

/**
 * Interface for exchange of alarm events.
 * 
 * @author Dirk
 * 
 */
public interface AlarmEvent extends NotEOFEvent {

    /**
     * Set alarm type.
     * 
     * @param alarmType
     *            The type of alarm. Later this can decide how the alarm will be
     *            processed (e.g. a target system, a hardware, what ever).
     */
    public void setType(int type);

    /**
     * Set alarm level.
     * 
     * @param level
     *            The importance of the alarm.
     */
    public void setLevel(int level);

    /**
     * Set alarm description.
     * 
     * @param alarmDescription
     *            An additional information which can be helpful to solve the
     *            problem.
     */
    public void setDescription(String description);

    /**
     * Get alarm type.
     * 
     * @return The type of alarm. Later this can decide how the alarm will be
     *         processed (e.g. a target system, a hardware, what ever).
     */
    public int getType();

    /**
     * Get alarm level.
     * 
     * @return The importance of the alarm.
     */
    public int getLevel();

    /**
     * Get alarm description.
     * 
     * @return An additional information which can be helpful to solve the
     *         problem.
     */
    public String getDescription();
}
