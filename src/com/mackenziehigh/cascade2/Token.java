package com.mackenziehigh.cascade2;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A token is a series of one or more textual keys, which ideally are human-readable.
 *
 * <p>
 * Tokens are optimized for efficient equality testing via the use of MD5 hashing.
 * The hash is not intended to be used for cryptological purposes,
 * rather the hash is simple used for efficiency.
 * </p>
 *
 * <p>
 * This class is optimized for speed, rather than memory, efficiency.
 * </p>
 */
public final class Token
        implements Comparable<Token>
{

    private final ImmutableList<String> keys;

    private final String prefix;

    private final String suffix;

    private final String name;

    private final Optional<Token> parent;

    private final long highMD5;

    private final long lowMD5;

    private final byte[] hashBytes;

    private final String hashString;

    private final int hashCode;

    private Token (final Token parent,
                   final String suffix)
    {
        this.parent = Optional.ofNullable(parent);
        this.prefix = parent == null ? "" : parent.name();
        this.suffix = suffix;
        this.name = prefix.isEmpty() ? suffix : prefix + '.' + suffix;
        this.hashString = Hashing.md5().hashString(name, Charset.forName("ASCII")).toString();
        this.hashBytes = Hashing.md5().hashString(name, Charset.forName("ASCII")).asBytes();
        Verify.verify(hashBytes.length == 128 / 8); // 128 bits to bytes
        this.highMD5 = Longs.fromByteArray(Arrays.copyOfRange(hashBytes, 8, 16));
        this.lowMD5 = Longs.fromByteArray(Arrays.copyOfRange(hashBytes, 0, 8));
        Verify.verify(Arrays.equals(hashBytes, Bytes.concat(Longs.toByteArray(lowMD5), Longs.toByteArray(highMD5))));
        this.hashCode = HashCode.fromBytes(hashBytes).hashCode();
        this.keys = parent == null
                ? ImmutableList.of(name)
                : ImmutableList.<String>builder().addAll(parent.keys()).add(suffix).build();
    }

    /**
     * Use this method to create a new token, derived from this token,
     * which has one additional suffix appended thereto.
     *
     * <p>
     * The name must be ASCII encoded.
     * </p>
     *
     * @param key will become the new suffix.
     * @return the new token.
     */
    public Token append (final String key)
    {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkArgument(key.contains(".") == false, "Invalid Key: " + key);
        return new Token(this, key);
    }

    /**
     * Getter.
     *
     * @return all of the keys in this token (in proper order).
     */
    public List<String> keys ()
    {
        return keys;
    }

    /**
     * Getter.
     *
     * @return the prefix token.
     */
    public Optional<Token> parent ()
    {
        return parent;
    }

    /**
     * Getter.
     *
     * @return the name of the prefix token.
     */
    public String prefix ()
    {
        return prefix;
    }

    /**
     * Getter.
     *
     * @return the last key in the name.
     */
    public String suffix ()
    {
        return suffix;
    }

    /**
     * Getter.
     *
     * @return the full-name of this token.
     */
    public String name ()
    {
        return name;
    }

    /**
     * Getter.
     *
     * @return the textual representation of the MD5 hash.
     */
    public String hashString ()
    {
        return hashString;
    }

    /**
     * Getter.
     *
     * @param out will receive the binary representation of the MD5 hash.
     * @return this.
     */
    public Token hashBytes (final byte[] out)
    {
        System.arraycopy(hashBytes, 0, out, 0, hashBytes.length);
        return this;
    }

    /**
     * Predicate.
     *
     * @param other may be covered by this token.
     * @return true, iff this token is a prefix of the other token.
     */
    public boolean isPrefixOf (final Token other)
    {
        if (other == null)
        {
            return false;
        }
        else if (keys.size() > other.keys.size())
        {
            return false;
        }
        else
        {
            for (int i = 0; i < keys.size(); i++)
            {
                if (keys.get(i).equals(other.keys.get(i)) == false)
                {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Predicate.
     *
     * @param other may equal this token.
     * @return true, iff this token is definitely equal to the the other token.
     */
    public boolean isStrictlyEqualTo (final Token other)
    {
        return isWeaklyEqualTo(other) && name.equals(other.name);
    }

    /**
     * Predicate.
     *
     * @param other may be equal to this token.
     * @return true, iff this token is equal to the other token,
     * unless a hash-collision has silently occurred (extremely unlikely).
     */
    public boolean isWeaklyEqualTo (final Token other)
    {
        return (other != null) && (lowMD5 == other.lowMD5) && (highMD5 == other.highMD5);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode ()
    {
        return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals (final Object other)
    {
        if (other == null)
        {
            return false;
        }
        else if (hashCode != other.hashCode())
        {
            return false;
        }
        else if (other instanceof Token == false)
        {
            return false;
        }
        else
        {
            return isWeaklyEqualTo((Token) other); // Very Fast and Good Enough!
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo (final Token other)
    {
        Preconditions.checkNotNull(other, "other");
        return name.compareTo(other.name);
    }

    /**
     * {@inheritDoc}
     *
     * @return name().
     */
    @Override
    public String toString ()
    {
        return name;
    }

    /**
     * Factory Method.
     *
     * <p>
     * The name must be ASCII encoded.
     * </p>
     *
     * @param name is a dot-delimited list of keys that will be converted to a token.
     * @return the newly created token object based on the given full-name.
     */
    public static Token create (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(name.matches("([^.]+[.])*[^.]+"), "Invalid Name: " + name);

        final String[] parts = name.split("\\.");
        Token token = null;

        for (String part : parts)
        {
            token = new Token(token, part);
        }

        return token;
    }

}
