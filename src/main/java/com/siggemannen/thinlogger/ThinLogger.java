package com.siggemannen.thinlogger;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.NormalizedParameters;
import org.slf4j.spi.LocationAwareLogger;

public class ThinLogger extends LegacyAbstractLogger
{
    private static final long serialVersionUID = -632788891211436180L;

    private static final long START_TIME = System.currentTimeMillis();

    protected static final int LOG_LEVEL_TRACE = LocationAwareLogger.TRACE_INT;
    protected static final int LOG_LEVEL_DEBUG = LocationAwareLogger.DEBUG_INT;
    protected static final int LOG_LEVEL_INFO = LocationAwareLogger.INFO_INT;
    protected static final int LOG_LEVEL_WARN = LocationAwareLogger.WARN_INT;
    protected static final int LOG_LEVEL_ERROR = LocationAwareLogger.ERROR_INT;

    static char SP = ' ';
    static final String TID_PREFIX = "tid=";

    // The OFF level can only be used in configuration files to disable logging.
    // It has
    // no printing method associated with it in o.s.Logger interface.
    protected static final int LOG_LEVEL_OFF = LOG_LEVEL_ERROR + 10;

    private static boolean INITIALIZED = false;
    //public static final DateFormat FORMAT_WITH_TIME_AND_MS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 /*
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral(' ')
            //.append(DateTimeFormatter.ISO_LOCAL_TIME)
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendLiteral('.')
            .appendFraction(NANO_OF_SECOND, 3, 3, false)
            .toFormatter();
*/
    private static final String CONFIGURATION_FILE = "logger.properties";

    private static final Properties properties = new Properties();

    static void lazyInit()
    {
        if (INITIALIZED)
        {
            return;
        }
        init();
        INITIALIZED = true;
    }

    /** The current log level */
    //protected int currentLogLevel = LOG_LEVEL_INFO;
    /** The short name of this simple log instance */
    private transient String shortLogName = null;

    /**
     * All system properties used by <code>SimpleLogger</code> start with this prefix
     */
    public static final String SYSTEM_PREFIX = "org.slf4j.simpleLogger.";

    public static final String LOG_KEY_PREFIX = ThinLogger.SYSTEM_PREFIX + "log.";

    public static final String CACHE_OUTPUT_STREAM_STRING_KEY = ThinLogger.SYSTEM_PREFIX + "cacheOutputStream";

    public static final String WARN_LEVEL_STRING_KEY = ThinLogger.SYSTEM_PREFIX + "warnLevelString";

    public static final String LEVEL_IN_BRACKETS_KEY = ThinLogger.SYSTEM_PREFIX + "levelInBrackets";

    public static final String LOG_FILE_KEY = ThinLogger.SYSTEM_PREFIX + "logFile";

    public static final String SHOW_SHORT_LOG_NAME_KEY = ThinLogger.SYSTEM_PREFIX + "showShortLogName";

    public static final String SHOW_LOG_NAME_KEY = ThinLogger.SYSTEM_PREFIX + "showLogName";

    public static final String SHOW_THREAD_NAME_KEY = ThinLogger.SYSTEM_PREFIX + "showThreadName";

    public static final String SHOW_THREAD_ID_KEY = ThinLogger.SYSTEM_PREFIX + "showThreadId";

    public static final String DATE_TIME_FORMAT_KEY = ThinLogger.SYSTEM_PREFIX + "dateTimeFormat";

    public static final String SHOW_DATE_TIME_KEY = ThinLogger.SYSTEM_PREFIX + "showDateTime";

    public static final String DEFAULT_LOG_LEVEL_KEY = ThinLogger.SYSTEM_PREFIX + "defaultLogLevel";

    private static final List<Appender> appenders = new ArrayList<>();

    private static ConsoleAppender consoleAppender;

    private static FileAppender fileAppender;

    /**
     * Package access allows only {@link ThinLoggerFactory} to instantiate SimpleLogger instances.
     */
    ThinLogger(String name)
    {
        this.name = name;
    }

    static void init()
    {
        loadProperties();
        consoleAppender = new ConsoleAppender();
        appenders.add(consoleAppender);
        //Get some system props...
        String filename = properties.getProperty("filepattern");
        if (filename != null)
        {
            fileAppender = new FileAppender(filename);
            appenders.add(fileAppender);
        }
    }

    private static void loadProperties()
    {
        // Add props from the resource simplelogger.properties
        try (InputStream in = AccessController.doPrivileged((PrivilegedAction<InputStream>) () ->
        {
            ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
            if (threadCL != null)
            {
                return threadCL.getResourceAsStream(CONFIGURATION_FILE);
            }
            else
            {
                return ClassLoader.getSystemResourceAsStream(CONFIGURATION_FILE);
            }
        }))
        {
            if (null != in)
            {
                properties.load(in);
            }
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * To avoid intermingling of log messages and associated stack traces, the two operations are done in a synchronized block.
     *
     * @param buf
     * @param t
     */
    void write(Level level, StringBuilder buf, Throwable t)
    {
        String lazyString = null;
        for (int i = 0; i < appenders.size(); i++)
        {
            Appender app = appenders.get(i);
            if (app.applicable(level))
            {
                if (lazyString == null)
                {
                    lazyString = buf.toString();
                }
                app.append(level, t, lazyString);
            }
        }
        /*
        PrintStream targetStream = CONFIG_PARAMS.outputChoice.getTargetPrintStream();
        
        synchronized (CONFIG_PARAMS) {
            targetStream.println(buf.toString());
            writeThrowable(t, targetStream);
            targetStream.flush();
        } */
    }

    /**
     * Returns formatted date
     * 
     * <P>This version avoids non-threadsafe formatters and friends
     * @return
     */
    private String getFormattedDate()
    {
        //Date now = new Date();

        //"yyyy-MM-dd HH:mm:ss.SSS"
        //synchronized (da /*CONFIG_PARAMS.dateFormatter*/)
        //        {
        //            dateText = FORMAT_WITH_TIME_AND_MS.format(now); /*CONFIG_PARAMS.dateFormatter.format(now);*/
        //        }
        LocalDateTime now = LocalDateTime.now();
        String ms = "" + now.getNano() / 1000_000;
        return new StringBuilder().append(now.getYear())
                .append("-")
                .append(now.getMonthValue() < 10 ? "0" : "")
                .append(now.getMonthValue())
                .append("-")
                .append(now.getDayOfMonth() < 10? "0":"")
                .append(now.getDayOfMonth())
                .append(" ")
                .append(now.getHour() < 10?"0":"")
                .append(now.getHour())
                .append(":")
                .append(now.getMinute() < 10?"0":"")
                .append(now.getMinute())
                .append(":")
                .append(now.getSecond() < 10 ? "0":"")
                .append(now.getSecond())
                .append(".")
                .append(("00"+ms).substring(ms.length()-1))
                .toString();
        //return FORMATTER.format(now);
    }

    private String computeShortName()
    {
        return name.substring(name.lastIndexOf(".") + 1);
    }

    // /**
    // * For formatted messages, first substitute arguments and then log.
    // *
    // * @param level
    // * @param format
    // * @param arg1
    // * @param arg2
    // */
    // private void formatAndLog(int level, String format, Object arg1, Object arg2) {
    // if (!isLevelEnabled(level)) {
    // return;
    // }
    // FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
    // log(level, tp.getMessage(), tp.getThrowable());
    // }

    // /**
    // * For formatted messages, first substitute arguments and then log.
    // *
    // * @param level
    // * @param format
    // * @param arguments
    // * a list of 3 ore more arguments
    // */
    // private void formatAndLog(int level, String format, Object... arguments) {
    // if (!isLevelEnabled(level)) {
    // return;
    // }
    // FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
    // log(level, tp.getMessage(), tp.getThrowable());
    // }

    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel is this level enabled?
     * @return whether the logger is enabled for the given level
     */
    protected boolean isLevelEnabled(int logLevel)
    {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return true;
        // return (logLevel >= currentLogLevel);
    }

    /** Are {@code trace} messages currently enabled? */
    @Override
    public boolean isTraceEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_TRACE);
    }

    /** Are {@code debug} messages currently enabled? */
    @Override
    public boolean isDebugEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_DEBUG);
    }

    /** Are {@code info} messages currently enabled? */
    @Override
    public boolean isInfoEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_INFO);
    }

    /** Are {@code warn} messages currently enabled? */
    @Override
    public boolean isWarnEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }

    /** Are {@code error} messages currently enabled? */
    @Override
    public boolean isErrorEnabled()
    {
        return isLevelEnabled(LOG_LEVEL_ERROR);
    }

    /**
     * This is our internal implementation for logging regular (non-parameterized) log messages.
     *
     * @param level One of the LOG_LEVEL_XXX constants defining the log level
     * @param message The message itself
     * @param t The exception whose stack trace should be logged
     */
    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable t)
    {

        List<Marker> markers = null;

        if (marker != null)
        {
            markers = new ArrayList<>();
            markers.add(marker);
        }

        innerHandleNormalizedLoggingCall(level, markers, messagePattern, arguments, t);
    }

    private void innerHandleNormalizedLoggingCall(Level level, List<Marker> markers, String messagePattern, Object[] arguments, Throwable t)
    {

        StringBuilder buf = new StringBuilder(32);

        // Append date-time if so configured
        if (true/*CONFIG_PARAMS.showDateTime*/)
        {
            if (true/*CONFIG_PARAMS.dateFormatter != null*/)
            {
                buf.append(getFormattedDate());
                buf.append(SP);
            }
            else
            {
                buf.append(System.currentTimeMillis() - START_TIME);
                buf.append(SP);
            }
        }

        // Append current thread name if so configured
        if (true/*CONFIG_PARAMS.showThreadName*/)
        {
            buf.append('[');
            buf.append(Thread.currentThread().getName());
            buf.append("] ");
        }

        if (true/*CONFIG_PARAMS.showThreadId*/)
        {
            buf.append(TID_PREFIX);
            buf.append(Thread.currentThread().getId());
            buf.append(SP);
        }

        if (true/*CONFIG_PARAMS.levelInBrackets*/)
        {
            buf.append('[');
        }

        // Append a readable representation of the log level
        String levelStr = level.name();
        buf.append(levelStr);
        if (true/*CONFIG_PARAMS.levelInBrackets*/)
        {
            buf.append(']');
        }
        buf.append(SP);

        // Append the name of the log instance if so configured
        if (true/*CONFIG_PARAMS.showShortLogName*/)
        {
            if (shortLogName == null)
            {
                shortLogName = computeShortName();
            }
            buf.append(String.valueOf(shortLogName)).append(" - ");
        }
        else if (true/*CONFIG_PARAMS.showLogName*/)
        {
            buf.append(String.valueOf(name)).append(" - ");
        }

        if (markers != null)
        {
            buf.append(SP);
            for (Marker marker : markers)
            {
                buf.append(marker.getName()).append(SP);
            }
        }

        String formattedMessage = MessageFormatter.basicArrayFormat(messagePattern, arguments);

        // Append the message
        buf.append(formattedMessage);

        write(level, buf, t);
    }

    public void log(LoggingEvent event)
    {
        int levelInt = event.getLevel().toInt();

        if (!isLevelEnabled(levelInt))
        {
            return;
        }

        NormalizedParameters np = NormalizedParameters.normalize(event);

        innerHandleNormalizedLoggingCall(event.getLevel(), event.getMarkers(), np.getMessage(), np.getArguments(), event.getThrowable());
    }

    @Override
    protected String getFullyQualifiedCallerName()
    {
        return null;
    }

}
