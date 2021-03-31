package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.library.SkullItem;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * An object that encapsulates a source object and locates a bukkit player
 */
public class PlayerSearch {

	private final UUID uuid;

	protected PlayerSearch(OfflinePlayer target) {
		this.uuid = target.getUniqueId();
	}

	protected PlayerSearch(UUID uuid) {
		this.uuid = uuid;
	}

	protected PlayerSearch(String name) {
		OfflinePlayer search = Arrays.stream(Bukkit.getOfflinePlayers()).filter(p -> Objects.equals(p.getName(), name)).findFirst().orElse(null);
		this.uuid = search != null ? search.getUniqueId() : null;
	}

	/**
	 * Get {@link Player} based options from the provided source.
	 * <p>
	 * {@link PlayerSearch#ban(String)}, {@link PlayerSearch#kick()}, {@link PlayerSearch#unban()} or various other
	 * options.
	 *
	 * @param target The source object.
	 * @return A bukkit player search.
	 */
	public static PlayerSearch look(OfflinePlayer target) {
		return new PlayerSearch(target);
	}

	/**
	 * Get {@link Player} based options from the provided source.
	 * <p>
	 * {@link PlayerSearch#ban(String)}, {@link PlayerSearch#kick()}, {@link PlayerSearch#unban()} or various other
	 * options.
	 *
	 * @param target The source object.
	 * @return A bukkit player search.
	 */
	public static PlayerSearch look(Player target) {
		return new PlayerSearch(target);
	}

	/**
	 * Get {@link Player} based options from the provided source.
	 * <p>
	 * {@link PlayerSearch#ban(String)}, {@link PlayerSearch#kick()}, {@link PlayerSearch#unban()} or various other
	 * options.
	 *
	 * @param uuid The source object.
	 * @return A bukkit player search.
	 */
	public static PlayerSearch look(UUID uuid) {
		return new PlayerSearch(uuid);
	}

	/**
	 * Get {@link Player} based options from the provided source.
	 * <p>
	 * {@link PlayerSearch#ban(String)}, {@link PlayerSearch#kick()}, {@link PlayerSearch#unban()} or various other
	 * options.
	 *
	 * @param name The source object.
	 * @return A bukkit player search.
	 */
	public static PlayerSearch look(String name) {
		return new PlayerSearch(name);
	}

	/**
	 * Get the head of the user.
	 *
	 * @return The player's head {@link ItemStack} or null if no user was found.
	 */
	public @Nullable
	synchronized ItemStack getHead() {
		return SkullItem.Head.find(uuid);
	}

	/**
	 * Check if the user is valid, i.e is found and available
	 * within the bukkit player log.
	 *
	 * @return true if the desired player is valid.
	 */
	public boolean isValid() {
		return uuid != null;
	}

	/**
	 * Get the unique id of the player object.
	 *
	 * @return The player's UUID or null if not valid.
	 */
	public @Nullable UUID getId() {
		return uuid;
	}

	/**
	 * Check if the player is online.
	 *
	 * @return false if the player is not online or is not valid.
	 */
	public boolean isOnline() {
		return uuid != null && Bukkit.getOfflinePlayer(uuid).isOnline();
	}

	/**
	 * Get the player object.
	 *
	 * @return The player otherwise null if not valid.
	 */
	public @Nullable Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	/**
	 * Get the offline player object.
	 *
	 * @return The offline player otherwise null if not valid.
	 */
	public @Nullable OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(uuid);
	}

	/**
	 * Check if the desired player is banned.
	 * <p>
	 * Uses the internal {@link Bukkit} {@link BanList}
	 *
	 * @return false if the user isn't banned or is not valid.
	 */
	public boolean isBanned() {
		if (uuid == null) {
			return false;
		}
		return Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName());
	}

	/**
	 * Attempt banning the desired player providing the banning source.
	 *
	 * @param source The source of removal (Name of banner)
	 * @return false if the user is already banned or is not valid.
	 */
	public boolean ban(String source) {
		if (uuid == null) {
			return false;
		}
		if (Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		if (isOnline()) {
			kick();
		}
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), "No reason.", null, source);
		return true;
	}

	/**
	 * Attempt banning the desired player providing the banning source and reason for removal.
	 *
	 * @param source The source of removal. (Name of banner)
	 * @param reason The reason they're getting banned.
	 * @return false if the user is already banned or is not valid.
	 */
	public boolean ban(String source, String reason) {
		if (uuid == null) {
			return false;
		}
		if (Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		if (isOnline()) {
			kick(reason);
		}
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), reason, null, source);
		return true;
	}

	/**
	 * Attempt banning the desired player providing the banning source, a reason for removal
	 * and the date the ban will be lifted.
	 *
	 * @param source     The source of removal (The banner)
	 * @param reason     The reason for removal.
	 * @param expiration The date to lift the ban.
	 * @return false if the user is already banned or is not valid.
	 */
	public boolean ban(String source, String reason, Date expiration) {
		if (uuid == null) {
			return false;
		}
		if (Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		if (isOnline()) {
			kick(reason);
		}
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), reason, expiration, source);
		return true;
	}

	/**
	 * Unban the desired player.
	 *
	 * @return false if the user is not banned or is not valid.
	 */
	public boolean unban() {
		if (uuid == null) {
			return false;
		}
		if (!Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		Bukkit.getBanList(BanList.Type.NAME).pardon(Objects.requireNonNull(getOfflinePlayer().getName()));
		return true;
	}

	/**
	 * Kick the desired player.
	 *
	 * @return false if the user is not online or is not valid.
	 */
	public boolean kick() {
		if (uuid == null) {
			return false;
		}
		if (!Objects.requireNonNull(getOfflinePlayer()).isOnline()) {
			return false;
		}
		Objects.requireNonNull(getPlayer()).kickPlayer(StringUtils.translate("&c&oNo reason."));
		return true;
	}

	/**
	 * Kick the desired player for a specific reason.
	 *
	 * @param reason The reason for removal.
	 * @return false if the user isn't online or is not valid.
	 */
	public boolean kick(String reason) {
		if (uuid == null) {
			return false;
		}
		if (!Objects.requireNonNull(getOfflinePlayer()).isOnline()) {
			return false;
		}
		Objects.requireNonNull(getPlayer()).kickPlayer(reason);
		return true;
	}


}
