
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public final class PipeReader
        implements AutoCloseable
{
    private final DataInputStream sin;

    private final byte[] buffer = new byte[2 * 1024 * 1024];

    public PipeReader (final InputStream in)
    {
        this.sin = new DataInputStream(new BufferedInputStream(in));
    }

    @Override
    public void close ()
            throws Exception
    {
        sin.close();
    }

    public void read ()
            throws IOException
    {
        //final int magic = sin.readInt();
        //final int count = sin.readInt();

        long offset = 0;
        long count = 0;

        try
        {
            sin.readFully(buffer);
        }
        catch (EOFException ex)
        {
            // Pass, expected.
        }

        //System.out.println("Sum = " + sum);
    }

    public static void main (String[] args)
            throws FileNotFoundException,
                   IOException
    {

        while (true)
        {
            final PipeReader reader = new PipeReader(new FileInputStream(new File("/dev/shm/p")));
            reader.read();
        }
    }
}
