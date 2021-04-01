package com.github.sanctum.myessentials.model.ban;

import org.bukkit.BanEntry;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Base class for Bans.
 */
@SuppressWarnings("serial")
public abstract class Ban implements Serializable {
    protected final UUID uid;
    protected final String source;
    protected final LocalDateTime creation;
    protected final LocalDateTime expiration;
    protected final String reason;

    protected Ban(@NotNull String source, @NotNull OfflinePlayer player, String reason) {
        this(source, player, reason, null);
    }
    protected Ban(@NotNull String source, @NotNull OfflinePlayer player, String reason, LocalDateTime expiration) {
        this.uid = player.getUniqueId();
        this.source = source;
        this.creation = LocalDateTime.now();
        this.expiration = expiration;
        this.reason = reason;
    }
    protected Ban(BanEntry entry) {
        this.uid = null;
        this.source = entry.getSource();
        this.creation = LocalDateTime.ofInstant(entry.getCreated().toInstant(), ZoneId.systemDefault());
        this.expiration = Optional.ofNullable(entry.getExpiration())
                .map(ex -> LocalDateTime.ofInstant(ex.toInstant(), ZoneId.systemDefault()))
                .orElse(null);
        this.reason = entry.getReason();
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
    public @NotNull LocalDateTime getCreation() {
        return creation;
    }

    /**
     * Get a description of the source of this ban.
     *
     * @return description of source of this ban
     */
    public @NotNull String source() {
        return source;
    }

    /**
     * Get the expiration of this ban.
     * <p>
     * Returns empty for permas.
     *
     * @return an optional describing the expiration of this ban
     */
    public Optional<LocalDateTime> getExpiration() {
        return Optional.ofNullable(expiration);
    }

    /**
     * Get the reason provided for the ban.
     *
     * @return reason provided for this ban
     */
    public @Nullable String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ban ban = (Ban) o;
        return Objects.equals(uid, ban.uid) &&
                source.equals(ban.source) &&
                creation.equals(ban.creation) &&
                Objects.equals(expiration, ban.expiration) &&
                Objects.equals(reason, ban.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, source, creation, expiration, reason);
    }

}
