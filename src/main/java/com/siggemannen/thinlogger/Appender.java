package com.siggemannen.thinlogger;

import org.slf4j.event.Level;

/**
 * Simple i-face to append data
 */
public interface Appender
{
    /**
     * Returns true if this level should be loged
     * @param level level to check
     * @return true if this level is logged by this appender
     */
    boolean applicable(Level level);

    /**
     * Append message / exception
     * @param level level to append
     * @param throwable exception, can be null
     * @param sb message to append
     */
    void append(Level level, Throwable throwable, String sb);
}
