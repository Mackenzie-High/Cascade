package com.mackenziehigh.cascade;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A token is a series of one or more textual keys, which form a name.
 *
 * <p>
 * Tokens are optimized for efficient equality testing via the use of SHA-256 hashing.
 * The hash is <b>not</b> intended to be used for cryptological purposes,
 * rather the hash is simple used for efficiency.
 * </p>
 *
 * <p>
 * This class is optimized for speed, rather than memory, efficiency.
 * </p>
 */
public final class CascadeToken
        implements Comparable<CascadeToken>
{

    private final ImmutableList<String> keys;

    private final String prefix;

    private final String suffix;

    private final String name;

    private final Optional<CascadeToken> parent;

    private final long hash1;

    private final long hash2;

    private final long hash3;

    private final long hash4;

    private final String hashString;

    private final BigInteger hashInt;

    private final int hashCode;

    private CascadeToken (final CascadeToken parent,
                          final String suffix)
    {
        Preconditions.checkArgument(CharMatcher.ascii().matchesAllOf(suffix), "Non-ASCII Token");
        this.parent = Optional.ofNullable(parent);
        this.prefix = parent == null ? "" : parent.name();
        this.suffix = suffix;
        this.name = prefix.isEmpty() ? suffix : prefix + '.' + suffix;
        this.hashString = Hashing.sha256().hashString(name, Charset.forName("ASCII")).toString();
        final byte[] hashBytes = Hashing.sha256().hashString(name, Charset.forName("ASCII")).asBytes();
        this.hashInt = new BigInteger(hashBytes);
        Verify.verify(hashBytes.length == 256 / 8); // 256 bits to bytes
        this.hash4 = Longs.fromByteArray(Arrays.copyOfRange(hashBytes, 24, 32));
        this.hash3 = Longs.fromByteArray(Arrays.copyOfRange(hashBytes, 16, 24));
        this.hash2 = Longs.fromByteArray(Arrays.copyOfRange(hashBytes, 8, 16));
        this.hash1 = Longs.fromByteArray(Arrays.copyOfRange(hashBytes, 0, 8));
        Verify.verify(Arrays.equals(hashBytes,
                                    Bytes.concat(Longs.toByteArray(hash1),
                                                 Longs.toByteArray(hash2),
                                                 Longs.toByteArray(hash3),
                                                 Longs.toByteArray(hash4))));
        this.hashCode = HashCode.fromBytes(hashBytes).hashCode();
        this.keys = parent == null
                ? ImmutableList.of(name)
                : ImmutableList.<String>builder().addAll(parent.keys()).add(suffix).build();
    }

    /**
     * Create a new token with a random name based on a UUID.
     *
     * @return the new random token.
     */
    public static CascadeToken random ()
    {
        return token(UUID.randomUUID().toString());
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
    public CascadeToken append (final String key)
    {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkArgument(key.contains(".") == false, "Invalid Key: " + key);
        return new CascadeToken(this, key);
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
    public Optional<CascadeToken> parent ()
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
     * @return the textual representation of the SHA-256 hash.
     */
    public String hashString ()
    {
        return hashString;
    }

    /**
     * Getter.
     *
     * @return the first series of eight bytes of the binary hash.
     */
    public long hash1 ()
    {
        return hash1;
    }

    /**
     * Getter.
     *
     * @return the second series of eight bytes of the binary hash.
     */
    public long hash2 ()
    {
        return hash2;
    }

    /**
     * Getter.
     *
     * @return the third series of eight bytes of the binary hash.
     */
    public long hash3 ()
    {
        return hash3;
    }

    /**
     * Getter.
     *
     * @return the fourth series of eight bytes of the binary hash.
     */
    public long hash4 ()
    {
        return hash4;
    }

    /**
     * Getter.
     *
     * @param out will receive the binary representation of the SHA-256 hash.
     * @return this.
     */
    public CascadeToken toHashBytes (final byte[] out)
    {
        if (out.length < 8 * 4)
        {
            throw new IllegalArgumentException("out is too small.");
        }
        else
        {
            out[0] = (byte) (hash1 & (0xFF));
            out[1] = (byte) (hash1 & (0xFF << 1));
            out[2] = (byte) (hash1 & (0xFF << 2));
            out[3] = (byte) (hash1 & (0xFF << 3));
            out[4] = (byte) (hash1 & (0xFF << 4));
            out[5] = (byte) (hash1 & (0xFF << 5));
            out[6] = (byte) (hash1 & (0xFF << 6));
            out[7] = (byte) (hash1 & (0xFF << 7));
            out[8] = (byte) (hash1 & (0xFF << 8));
            // TODO
            return this;
        }
    }

    /**
     * Getter.
     *
     * @return the binary representation of the SHA-256 hash.
     */
    public byte[] toHashBytes ()
    {
        final byte[] bytes = new byte[4 * 8];
        toHashBytes(bytes);
        return bytes;
    }

    /**
     * Getter.
     *
     * @return the cached integer representation of the SHA-256 hash.
     */
    public BigInteger toHashInt ()
    {
        return hashInt;
    }

    /**
     * Getter.
     *
     * @return true, if this token denotes a full-name.
     */
    public boolean isFullName ()
    {
        return !isSimpleName();
    }

    /**
     * Getter.
     *
     * @return true, if this token denotes a simple-name.
     */
    public boolean isSimpleName ()
    {
        return prefix.isEmpty();
    }

    /**
     * Predicate.
     *
     * @param other may be covered by this token.
     * @return true, iff this token is a prefix of the other token.
     */
    public boolean isPrefixOf (final CascadeToken other)
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
    public boolean isStrictlyEqualTo (final CascadeToken other)
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
    public boolean isWeaklyEqualTo (final CascadeToken other)
    {
        return (other != null)
               && (hash1 == other.hash1)
               && (hash2 == other.hash2)
               && (hash3 == other.hash3)
               && (hash4 == other.hash4);
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
        else if (other instanceof CascadeToken == false)
        {
            return false;
        }
        else
        {
            return isWeaklyEqualTo((CascadeToken) other); // Very Fast and *Good Enough*!
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo (final CascadeToken other)
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
    public static CascadeToken token (final String name)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(name.matches("([^.]+[.])*[^.]+"), "Invalid Name: " + name);

        final String[] parts = name.split("\\.");
        CascadeToken token = null;

        for (String part : parts)
        {
            token = new CascadeToken(token, part);
        }

        return token;
    }

}
