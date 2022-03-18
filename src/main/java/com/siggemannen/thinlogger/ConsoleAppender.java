package com.siggemannen.thinlogger;

import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.event.Level;

/**
 * Console appender
 * 
 * <P>Note! We put a lock so that the appender cannot be interleaved by threads</P>
 */
public class ConsoleAppender implements Appender
{
    private final ReentrantLock rl = new ReentrantLock();
    
    @Override
    public void append(Level level, Throwable throwable, String sb)
    {
        if (!applicable(level))
        {
            return;
        }
        PrintStream ps;
        if (level.compareTo(Level.INFO) >= 0)
        {
            ps = System.out;
        }
        else
        {
            ps = System.err;
        }
        try
        {
            rl.lock();
            ps.println(sb);
            if (throwable != null)
            {
                throwable.printStackTrace(ps);
            }
            ps.flush();
        }
        finally
        {
            rl.unlock();
        }
    }

    @Override
    public boolean applicable(Level level)
    {
        return true;
    }
}
