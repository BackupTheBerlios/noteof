package de.happtick.core.enumeration;

/**
 * Happtick knows some alarm level.
 * <p>
 * Relevance ranking of alarm levels begins with 0 (small) and ends with 4 (high
 * priority).
 * 
 * @author Dirk
 * 
 */
// TODO Konzept noch nicht ausgereift.
public enum HapptickAlarmLevel {
    ALARM_LEVEL_MARGINAL, //
    ALARM_LEVEL_IMPORTANT,
    ALARM_LEVEL_SIGNIFICANT,
    ALARM_LEVEL_CRITICAL;
}
