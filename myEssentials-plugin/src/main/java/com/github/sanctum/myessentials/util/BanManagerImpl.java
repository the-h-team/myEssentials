package com.github.sanctum.myessentials.util;

import com.github.sanctum.myessentials.Essentials;
import com.github.sanctum.myessentials.api.BanManager;
import com.github.sanctum.myessentials.model.ban.Ban;
import com.github.sanctum.myessentials.model.ban.IPBan;
import com.github.sanctum.myessentials.model.ban.NameBan;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Optional;
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
                .map(entry -> new IPBan(entry) {
                    private static final long serialVersionUID = -2467970354227392725L;
                    private final LocalDateTime creation = LocalDateTime.ofInstant(entry.getCreated().toInstant(), ZoneId.systemDefault());

                    @Override
                    public LocalDateTime getCreation() {
                        return creation;
                    }

                    @Override
                    public String source() {
                        return entry.getSource();
                    }

                    @Override
                    public Optional<LocalDateTime> getExpiration() {
                        return Optional.ofNullable(entry.getExpiration())
                                .map(ex -> LocalDateTime.ofInstant(ex.toInstant(), ZoneId.systemDefault()));
                    }

                    @Override
                    public String getReason() {
                        return entry.getReason();
                    }
                }).collect(Collectors.toSet());
    }

    @Override
    public Collection<NameBan> getNameBans() {
        return Bukkit.getBanList(BanList.Type.NAME).getBanEntries()
                .stream()
                .map(entry -> new NameBan(entry) {
                    private static final long serialVersionUID = -2467970354227392725L;
                    private final LocalDateTime creation = LocalDateTime.ofInstant(entry.getCreated().toInstant(), ZoneId.systemDefault());

                    @Override
                    public LocalDateTime getCreation() {
                        return creation;
                    }

                    @Override
                    public String source() {
                        return entry.getSource();
                    }

                    @Override
                    public Optional<LocalDateTime> getExpiration() {
                        return Optional.ofNullable(entry.getExpiration())
                                .map(ex -> LocalDateTime.ofInstant(ex.toInstant(), ZoneId.systemDefault()));
                    }

                    @Override
                    public String getReason() {
                        return entry.getReason();
                    }
                }).collect(Collectors.toSet());
    }
}
