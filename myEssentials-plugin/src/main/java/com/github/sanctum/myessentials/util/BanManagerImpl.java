package com.github.sanctum.myessentials.util;

import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.BanManager;
import com.github.sanctum.myessentials.model.ban.Ban;
import com.github.sanctum.myessentials.model.ban.IPBan;
import com.github.sanctum.myessentials.model.ban.NameBan;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BanManagerImpl implements BanManager {
    private final Essentials essentials;

    public BanManagerImpl(Essentials essentials) {
        this.essentials = essentials;
    }

    @Override
    public Collection<Ban> getAllBans() {
        return Stream.concat(getIPBans().stream(), getNameBans().stream()).collect(Collectors.toSet());
    }

    @Override
    public Collection<IPBan> getIPBans() {
        return Bukkit.getBanList(BanList.Type.IP).getBanEntries()
                .stream()
                .map(IPBan::new)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<NameBan> getNameBans() {
        return Bukkit.getBanList(BanList.Type.NAME).getBanEntries()
                .stream()
                .map(NameBan::new)
                .collect(Collectors.toSet());
    }
}
