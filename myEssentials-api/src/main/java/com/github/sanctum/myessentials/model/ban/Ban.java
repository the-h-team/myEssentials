package com.github.sanctum.myessentials.model.ban;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Base class for Bans.
 */
@SuppressWarnings("serial")
public abstract class Ban implements Serializable {
    protected final UUID uid;

    protected Ban(OfflinePlayer player) {
        this.uid = player.getUniqueId();
    }
    protected Ban() {
        this.uid = null;
    }

    /**
     * Get the unique id of the banned player.
     *
     * @return unique id of banned player
     */
    @Nullable
    public UUID getUniqueId() {
        return uid;
    }

    /**
     * Get the creation of this ban.
     *
     * @return creation of this ban
     */
    public abstract LocalDateTime getCreation();

    /**
     * Get a description of the source of this ban.
     *
     * @return description of source of this ban
     */
    public abstract String source();

    /**
     * Get the expiration of this ban.
     * <p>
     * Returns empty for permas.
     *
     * @return an optional describing the expiration of this ban
     */
    public abstract Optional<LocalDateTime> getExpiration();

    /**
     * Get the reason provided for the ban.
     *
     * @return reason provided for this ban
     */
    public abstract String getReason();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ban ban = (Ban) o;
        return Objects.equals(uid, ban.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}
