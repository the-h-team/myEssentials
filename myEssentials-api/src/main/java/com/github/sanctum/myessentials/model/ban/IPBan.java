package com.github.sanctum.myessentials.model.ban;

import org.bukkit.BanEntry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Describes an IP-based ban.
 */
public class IPBan extends Ban {
    private static final long serialVersionUID = -3264544256297773790L;
    protected final InetSocketAddress address;

    protected IPBan(@NotNull Player player, @NotNull String source, String reason) {
        super(source, player, reason);
        this.address = player.getAddress();
    }
    protected IPBan(@NotNull Player player, @NotNull String source, String reason, LocalDateTime expiration) {
        super(source, player, reason, expiration);
        this.address = player.getAddress();
    }

    public IPBan(BanEntry banEntry) {
        super(banEntry);
        this.address = InetSocketAddress.createUnresolved(banEntry.getTarget(), 0);
    }

    /**
     * Get the IP Address of the banned player.
     *
     * @return the IP address of the banned player
     */
    public InetSocketAddress getIPAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IPBan ipBan = (IPBan) o;
        return address.equals(ipBan.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), address);
    }
}
