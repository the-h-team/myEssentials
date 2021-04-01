package com.github.sanctum.myessentials.model.ban;

import org.bukkit.BanEntry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Describes a name-based ban.
 */
public class NameBan extends Ban {
    private static final long serialVersionUID = -8535167820363495565L;
    protected final String name;

    protected NameBan(@NotNull Player player, @NotNull String source, String reason) {
        super(source, player, reason);
        this.name = player.getName();
    }
    protected NameBan(@NotNull Player player, @NotNull String source, String reason, LocalDateTime expiration) {
        super(source, player, reason, expiration);
        this.name = player.getName();
    }

    public NameBan(BanEntry banEntry) {
        super(banEntry);
        this.name = banEntry.getTarget();
    }
    /**
     * Get the username of the banned player.
     *
     * @return username of banned player
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NameBan nameBan = (NameBan) o;
        return Objects.equals(name, nameBan.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
