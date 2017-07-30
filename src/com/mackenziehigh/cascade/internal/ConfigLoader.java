package com.mackenziehigh.cascade.internal;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.Resources;
import com.mackenziehigh.sexpr.SList;
import com.mackenziehigh.sexpr.Sexpr;
import com.mackenziehigh.sexpr.SexprSchema;
import com.mackenziehigh.sexpr.annotations.Before;
import com.mackenziehigh.sexpr.annotations.Pass;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

/**
 * This class is used to read a configuration file
 * and recursively reads included configuration files.
 */
final class ConfigLoader
{
    /**
     * These are the files that have been loaded,
     * or are in the process of being loaded..
     */
    private final Set<URL> included = Sets.newHashSet();

    /**
     * These are the statements that have been successfully read.
     */
    private final ArrayDeque<Sexpr> statements = new ArrayDeque<>(128);

    /**
     * This schema describes the input files.
     */
    private final SexprSchema grammar;

    /**
     * This stack is used to make error messages more informative.
     */
    private final Stack<URL> backtrace = new Stack<>();

    /**
     * If loading was successful, then this is the result.
     */
    private SList result;

    /**
     * If loading was unsuccessful, this is the error-message.
     */
    private final StringBuilder errorMessage = new StringBuilder();

    /**
     * Sole Constructor.
     *
     * @throws IOException if the schema cannot be loaded.
     */
    public ConfigLoader ()
            throws IOException
    {
        this.grammar = SexprSchema.fromResource("/com/mackenziehigh/cascade/internal/Grammar.txt")
                .setFailureHandler(x -> reportParsingFailure(x))
                .pass("DEFAULT_PASS")
                .defineViaAnnotations(this)
                .build();
    }

    @Pass ("DEFAULT_PASS")
    @Before ("include_unsafe")
    public void visitIncludeUnsafe (final SList node)
    {
        try
        {
            final URL path = new URL(node.get(1).toAtom().content());
            include(path, null, null);
        }
        catch (IOException | RuntimeException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Pass ("DEFAULT_PASS")
    @Before ("include_MD5")
    public void visitIncludeMD5 (final SList node)
    {
        try
        {
            final URL path = new URL(node.get(1).toAtom().content());
            final String checksum = node.get(4).toAtom().content();
            include(path, checksum, x -> computeMD5(x));
        }
        catch (IOException | RuntimeException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Pass ("DEFAULT_PASS")
    @Before ("include_SHA1")
    public void visitIncludeSHA1 (final SList node)
    {
        try
        {
            final URL path = new URL(node.get(1).toAtom().content());
            final String checksum = node.get(4).toAtom().content();
            include(path, checksum, x -> computeSHA1(x));
        }
        catch (IOException | RuntimeException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Pass ("DEFAULT_PASS")
    @Before ("include_SHA256")
    public void visitIncludeSHA256 (final SList node)
    {
        try
        {
            final URL path = new URL(node.get(1).toAtom().content());
            final String checksum = node.get(4).toAtom().content();
            include(path, checksum, x -> computeSHA256(x));
        }
        catch (IOException | RuntimeException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Pass ("DEFAULT_PASS")
    @Before ("include_SHA512")
    public void visitIncludeSHA512 (final SList node)
    {
        try
        {
            final URL path = new URL(node.get(1).toAtom().content());
            final String checksum = node.get(4).toAtom().content();
            include(path, checksum, x -> computeSHA512(x));
        }
        catch (IOException | RuntimeException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Effectively, this method implements include-directives.
     *
     * @param path is the URL of the file to include.
     * @param checksum is the expected checksum, if any.
     * @param algorithm is the hashing algorithm to use.
     * @throws IOException if the file cannot be read.
     */
    private void include (final URL path,
                          final String checksum,
                          final Function<String, String> algorithm)
            throws IOException
    {
        /**
         * Never include the same file twice.
         */
        if (included.contains(path))
        {
            return;
        }
        else
        {
            included.add(path);
        }

        backtrace.push(path);
        {
            /**
             * Read the input.
             */
            final List<String> lines = Resources.readLines(path, Charset.forName("UTF-8"));
            final int capacity = lines.stream().mapToInt(x -> x.length()).sum() * 2;
            final StringBuilder content = new StringBuilder(capacity);
            lines.forEach(x -> content.append(x).append('\n'));
            final SList input = SList.parse(path.toString(), content.toString());

            /**
             * If the user specified a required checksum,
             * then compute the checksum using the given algorithm and
             * then verify that the result matches the expected checksum.
             */
            final String actualChecksum = checksum == null ? null : algorithm.apply(content.toString());
            if (checksum != null && checksum.equalsIgnoreCase(actualChecksum) == false)
            {
                final String error = String.format("Expected Checksum (%s) != Actual Checksum (%s)", checksum, actualChecksum);
                throw new RuntimeException(error);
            }

            /**
             * If the input is successfully described by the schema,
             * then implicitly execute every include-directive therein
             * and then add the statements of the input to the overall
             * list of statements.
             */
            if (grammar.match(input))
            {
                statements.addAll(input);
            }
        }
        backtrace.pop();
    }

    /**
     * This method recursively reads a configuration file.
     *
     * @param file is the path to the configuration file.
     * @return true, iff the read was successful.
     */
    public boolean load (final URL file)
            throws IOException
    {
        try
        {
            include(file, null, null);
            result = SList.copyOf(statements);
            return true;
        }
        catch (IOException | RuntimeException ex)
        {
            errorMessage.append(Throwables.getRootCause(ex).getMessage()).append('\n');
            final List<URL> reversed = new ArrayList<>(backtrace);
            Collections.reverse(reversed);
            reversed.forEach(x -> errorMessage.append("BackTrace: ").append(x).append('\n'));
            return false;
        }
    }

    public Optional<SList> result ()
    {
        return Optional.ofNullable(result);
    }

    public String errorMessage ()
    {
        return errorMessage.toString();
    }

    private static void reportParsingFailure (final Optional<Sexpr> last)
    {
        if (last.isPresent())
        {
            throw new RuntimeException("Parsing Failed At " + last.get().location().message());
        }
        else
        {
            throw new RuntimeException("Parsing Totally Failed");
        }
    }

    private static String computeMD5 (final String data)
    {
        final byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
        final String checksum = Hashing.md5().hashBytes(bytes).toString();
        return checksum;
    }

    private static String computeSHA1 (final String data)
    {
        final byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
        final String checksum = Hashing.sha1().hashBytes(bytes).toString();
        return checksum;
    }

    private static String computeSHA256 (final String data)
    {
        final byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
        final String checksum = Hashing.sha256().hashBytes(bytes).toString();
        return checksum;
    }

    private static String computeSHA512 (final String data)
    {
        final byte[] bytes = data.getBytes(Charset.forName("UTF-8"));
        final String checksum = Hashing.sha512().hashBytes(bytes).toString();
        return checksum;
    }
}
