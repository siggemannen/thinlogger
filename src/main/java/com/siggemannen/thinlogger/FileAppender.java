package com.siggemannen.thinlogger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.event.Level;

/**
 * Simplified file appender
 */
public class FileAppender implements Appender
{
    private final String pattern;
    private File prevFile = null;
    private String fpattPrev = null;
    /*
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .toFormatter();
            */
    private final ReentrantLock rl = new ReentrantLock();
    private static final byte[] LINE_SEPARATOR = System.lineSeparator().getBytes();
    private final Map<File, FileOutputWrapper> ros = new HashMap<>();

    public FileAppender(String pattern)
    {
        this.pattern = pattern;
    }

    @Override
    public boolean applicable(Level level)
    {
        return level.compareTo(Level.INFO) <= 0;
    }

    @Override
    public void append(Level level, Throwable throwable, String sb)
    {
        if (!applicable(level))
        {
            return;
        }
        rl.lock();
        try
        {
            BufferedOutputStream os = getOs(getFileToAppend()).getOs();
            os.write(sb.getBytes());
            os.write(LINE_SEPARATOR);
            if (throwable != null)
            {
                throwable.printStackTrace(new PrintStream(os));
                os.write(LINE_SEPARATOR);
            }
            os.flush();
        }
        catch (Exception exx)
        {
        }
        finally
        {
            rl.unlock();
        }
    }

    private FileOutputWrapper getOs(File fileToAppend)
    {
        if (!ros.containsKey(fileToAppend))
        {
            try
            {
                FileOutputWrapper x = new FileOutputWrapper(fileToAppend, true, 256);
                ros.put(fileToAppend, x);
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }
        return ros.get(fileToAppend);
    }

    private File getFileToAppend() throws IOException
    {
        String fpatt = pattern.replace("${byDay}", today());
        if (fpattPrev == null || !fpattPrev.equals(fpatt))
        {
            //Clean up prev
            FileOutputWrapper os = ros.remove(fpattPrev);
            if (os != null)
            {
                os.getOs().close();
            }
            fpattPrev = fpatt;
            File f = new File(fpatt);
            f.getParentFile().mkdirs();
            if (!f.exists())
            {
                f.createNewFile();
            }
            prevFile = f;
        }
        return prevFile;
    }

    private String today()
    {
        LocalDateTime now = LocalDateTime.now();
        return new StringBuilder().append(now.getYear())
                .append("-")
                .append(now.getMonthValue() < 10 ? "0" : "")
                .append(now.getMonthValue())
                .append("-")
                .append(now.getDayOfMonth() < 10 ? "0" : "")
                .append(now.getDayOfMonth())
                .toString();
    }
}
