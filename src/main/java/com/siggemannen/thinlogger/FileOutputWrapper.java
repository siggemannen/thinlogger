package com.siggemannen.thinlogger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileOutputWrapper
{
    private final FileOutputStream fos;
    private final BufferedOutputStream os;

    public FileOutputWrapper(File file, boolean append, long bufferSize) throws FileNotFoundException
    {
        fos = new FileOutputStream(file, append);
        this.os = new BufferedOutputStream(fos, (int) bufferSize);
    }

    public BufferedOutputStream getOs()
    {
        return os;
    }
}
