package com.siggemannen.thinlogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * Logger factory
 */
public class ThinLoggerFactory implements ILoggerFactory
{
    Map<String, Logger> loggerMap;

    public ThinLoggerFactory()
    {
        loggerMap = new ConcurrentHashMap<>();
        ThinLogger.lazyInit();
    }

    /**
     * Return an appropriate {@link ThinLogger} instance by name.
     */
    @Override
    public Logger getLogger(String name)
    {
        Logger simpleLogger = loggerMap.get(name);
        if (simpleLogger != null)
        {
            return simpleLogger;
        }
        else
        {
            Logger newInstance = new ThinLogger(name);
            Logger oldInstance = loggerMap.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }
}
