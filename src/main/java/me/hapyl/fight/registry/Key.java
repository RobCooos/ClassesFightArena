package me.hapyl.fight.registry;

import me.hapyl.fight.annotate.ForceLowercase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @see Key#ofString(String)
 * @see Key#ofStringOrNull(String)
 * @see Keyed
 */
public final class Key {

    public static final Pattern PATTERN = Pattern.compile("^[a-z0-9_]+$");

    private static Key EMPTY;

    private final String key;

    private Key(@Nonnull String key) {
        if (!PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Key %s does not match the pattern: %s!".formatted(key, PATTERN.pattern()));
        }

        this.key = key;
    }

    /**
     * Gets the actual {@link String} of this {@link Key}.
     *
     * @return the string of this key.
     */
    @Nonnull
    public String getKey() {
        return key;
    }

    /**
     * Gets the actual {@link String} of this {@link Key}.
     *
     * @return the string of this key.
     * @deprecated This is considered as a 'lazy' was of getting the key, prefer {@link #getKey()}.
     */
    @Override
    @Deprecated
    public String toString() {
        return key;
    }

    /**
     * Returns true if this key is empty.
     *
     * @return true if this key is empty.
     */
    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    /**
     * Returns true if the given {@link String} matches the {@link Key} exactly.
     *
     * @param string - String to check.
     * @return true if the given string matches the key exactly.
     */
    public boolean isKeyMatches(@Nonnull String string) {
        return key.equals(string);
    }

    /**
     * Returns true if the given {@link String} matches the {@link Key} ignoring the case.
     *
     * @param string - String to check.
     * @return true if the given string matches the key ignoring the case.
     */
    public boolean isKeyMatchesIgnoreCase(@Nonnull String string) {
        return key.equalsIgnoreCase(string);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final Key that = (Key) object;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    /**
     * A factory method for creating {@link Key}s.
     *
     * @param string - Id.
     * @return a new string.
     * @throws IllegalArgumentException if the given string does not match the {@link #PATTERN}.
     */
    @Nonnull
    public static Key ofString(@Nonnull String string) {
        return new Key(string);
    }

    /**
     * A factory method for creating {@link Key}s.
     *
     * @param string - Id.
     * @return a new string or null, if the string is invalid.
     */
    @Nullable
    public static Key ofStringOrNull(@Nonnull String string) {
        try {
            return ofString(string);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets an empty {@link Key}.
     * <br>
     * Keys exist for a reason, use {@link #empty()} only for testing or development!
     *
     * @return an empty ket.
     */
    @Nonnull
    @Deprecated
    public static Key empty() {
        if (EMPTY == null) {
            EMPTY = new Key("_");
        }

        return EMPTY;
    }
}