package com.mackenziehigh.cascade.dev2;

import java.io.File;

public final class Data
{
    public final File file;

    public final byte[] data;

    public Data (File file,
                 byte[] data)
    {
        this.file = file;
        this.data = data;
    }
}
