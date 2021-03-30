package com.github.sanctum.myessentials.model.ban;

import org.bukkit.BanEntry;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Describes a name-based ban.
 */
public abstract class NameBan extends Ban {
    private static final long serialVersionUID = -8535167820363495565L;
    protected final String name;

    protected NameBan(Player player) {
        super(player);
        this.name = player.getName();
    }

    protected NameBan(BanEntry banEntry) {
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
