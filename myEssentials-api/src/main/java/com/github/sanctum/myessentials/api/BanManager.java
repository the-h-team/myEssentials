package com.github.sanctum.myessentials.api;

import com.github.sanctum.myessentials.model.ban.Ban;
import com.github.sanctum.myessentials.model.ban.IPBan;
import com.github.sanctum.myessentials.model.ban.NameBan;

import java.util.Collection;

/**
 * Issue new bans and manage existing bans.
 */
public interface BanManager {
    Collection<Ban> getAllBans();
    Collection<IPBan> getIPBans();
    Collection<NameBan> getNameBans();
}
