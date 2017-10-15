
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author mackenzie
 */
public class Main
{
    private static final long random = System.currentTimeMillis() % 64;

    public static byte[] create ()
    {
        return new byte[512 * 1024];
    }

    public static byte mutate (final byte[] array)
    {
        array[0] = (byte) random;
        return array[0];
    }

    public static void main (String[] args)
    {
        final Stopwatch watch = Stopwatch.createStarted();

        byte sum = 0;

        for (int i = 0; i < 1000 * 1000; i++)
        {
            if (i % 5000 == 0)
            {
                System.out.println(i);
            }
            sum += mutate(create());
        }

        System.out.println(watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(sum);
    }
}
