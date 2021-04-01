package com.github.sanctum.myessentials.util.moderation;

import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.SkullItem;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object that encapsulates a source object and locates a bukkit player
 */
public final class PlayerSearch {

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
		if (uuid == null) {
			return null;
		}
		return Bukkit.getOfflinePlayer(uuid);
	}

	/**
	 * Get the player's {@link Bukkit} public ban entry.
	 *
	 * @return The player's ban entry if one is present otherwise empty.
	 */
	public @NotNull Optional<BanEntry> getBanEntry() {
		if (uuid == null) {
			return Optional.empty();
		}
		if (!Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return Optional.empty();
		}
		return Optional.ofNullable(Bukkit.getBanList(BanList.Type.NAME).getBanEntry(Objects.requireNonNull(getOfflinePlayer().getName())));
	}

	/**
	 * Get the player's ban timer, if they have one you can check information like the initial time and remaining.
	 *
	 * @return The player's ban timer if one is present otherwise null.
	 */
	public @Nullable Cooldown getBanTimer() {
		if (uuid == null) {
			return null;
		}
		return Cooldown.getById("MyBan-id-" + uuid.toString());
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
		kick();
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
		kick(reason);
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), reason, null, source);
		return true;
	}

	/**
	 * Attempt banning the desired player providing the banning source and reason for removal.
	 *
	 * @param source The source of removal. (Name of banner)
	 * @param reason The reason they're getting banned.
	 * @return false if the user is already banned or is not valid.
	 */
	public boolean ban(String source, Consumer<KickReason> reason) {
		final KickReason r = KickReason.next();
		reason.accept(r);
		if (uuid == null) {
			return false;
		}
		if (Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		kick(r);
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), r.getReason(), null, source);
		return true;
	}

	/**
	 * Attempt banning the desired player providing the banning source and reason for removal.
	 *
	 * @param source The source of removal. (Name of banner)
	 * @param reason The reason they're getting banned.
	 * @param silent Whether or not to announce the player getting banned.
	 * @return false if the user is already banned or is not valid.
	 */
	public boolean ban(String source, Consumer<KickReason> reason, boolean silent) {
		final KickReason r = KickReason.next();
		reason.accept(r);
		if (uuid == null) {
			return false;
		}
		if (Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		kick(r);
		if (!silent) {
			Bukkit.broadcastMessage(StringUtils.translate(MyEssentialsAPI.getInstance().getPrefix() + " &c&oPlayer &4" + getOfflinePlayer().getName() + " &c&owas banned for &r" + '"' + ChatColor.stripColor(r.getReason()) + "&r" + '"'));
		}
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), r.getReason(), null, source);
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
		kick(reason);
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), reason, expiration, source);
		return true;
	}

	/**
	 * Attempt banning the desired player providing the banning source, a reason for removal
	 * and length of time for the ban.
	 *
	 * @param source The source of removal (The banner)
	 * @param reason The reason for removal.
	 * @param time   The length of the ban.
	 * @return false if the user is already banned or is not valid.
	 */
	public boolean ban(String source, String reason, long time) {
		if (uuid == null) {
			return false;
		}
		if (Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		kick(reason);
		BanCooldown cooldown = new BanCooldown(uuid, time);
		cooldown.save();
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
	public boolean ban(String source, Consumer<KickReason> reason, Date expiration) {
		final KickReason r = KickReason.next();
		reason.accept(r);
		if (uuid == null) {
			return false;
		}
		if (Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		kick(r);
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), r.getReason(), expiration, source);
		return true;
	}

	/**
	 * Attempt banning the desired player providing the banning source, a reason for removal
	 * and the length of time for the ban.
	 *
	 * @param source The source of removal (The banner)
	 * @param reason The reason for removal.
	 * @param time   The length of the ban.
	 * @return false if the user is already banned or is not valid.
	 */
	public boolean ban(String source, Consumer<KickReason> reason, long time) {
		if (uuid == null) {
			return false;
		}
		if (Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		BanCooldown cooldown = new BanCooldown(uuid, time);
		cooldown.save();
		KickReason r = KickReason.next();
		reason.accept(r);
		kick(r);
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), r.getReason(), null, source);
		return true;
	}

	/**
	 * Attempt banning the desired player providing the banning source, a reason for removal
	 * and the length of time for the ban.
	 *
	 * @param source The source of removal (The banner)
	 * @param reason The reason for removal.
	 * @param silent Whether or not to announce the player getting banned.
	 * @param time   The length of the ban.
	 * @return false if the user is already banned or is not valid.
	 */
	public boolean ban(String source, Consumer<KickReason> reason, long time, boolean silent) {
		if (uuid == null) {
			return false;
		}
		if (Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		BanCooldown cooldown = new BanCooldown(uuid, time);
		cooldown.save();
		KickReason r = KickReason.next();
		reason.accept(r);
		kick(r);
		if (!silent) {
			Bukkit.broadcastMessage(StringUtils.translate(MyEssentialsAPI.getInstance().getPrefix() + " &c&oPlayer &4" + getOfflinePlayer().getName() + " &c&owas banned for &r" + '"' + ChatColor.stripColor(r.getReason()) + "&r" + '"'));
		}
		Bukkit.getBanList(BanList.Type.NAME).addBan(Objects.requireNonNull(getOfflinePlayer().getName()), r.getReason(), null, source);
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
		if (getBanTimer() != null) {
			Cooldown.remove(getBanTimer());
		}
		Bukkit.getBanList(BanList.Type.NAME).pardon(Objects.requireNonNull(getOfflinePlayer().getName()));
		return true;
	}

	/**
	 * Unban the desired player.
	 *
	 * @param silent Whether or not to announce the player's unban.
	 * @return false if the user is not banned or is not valid.
	 */
	public boolean unban(boolean silent) {
		if (uuid == null) {
			return false;
		}
		if (!Bukkit.getBanList(BanList.Type.NAME).getBanEntries().stream().map(BanEntry::getTarget).collect(Collectors.toList()).contains(getOfflinePlayer().getName())) {
			return false;
		}
		if (getBanTimer() != null) {
			Cooldown.remove(getBanTimer());
		}
		if (!silent) {
			Bukkit.broadcastMessage(StringUtils.translate(MyEssentialsAPI.getInstance().getPrefix() + " &6&oPlayer &e" + getOfflinePlayer().getName() + " &6&owas unbanned."));
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
		Objects.requireNonNull(getPlayer()).kickPlayer(StringUtils.translate("&c&oNo reason specified."));
		return true;
	}

	/**
	 * Kick the desired player.
	 *
	 * @return false if the user is not online or is not valid.
	 */
	public boolean kick(boolean silent) {
		if (uuid == null) {
			return false;
		}
		if (!Objects.requireNonNull(getOfflinePlayer()).isOnline()) {
			return false;
		}
		if (!silent) {
			Bukkit.broadcastMessage(StringUtils.translate(MyEssentialsAPI.getInstance().getPrefix() + " &c&oPlayer &4" + getPlayer().getName() + " &c&owas kicked."));
		}
		Objects.requireNonNull(getPlayer()).kickPlayer(StringUtils.translate("&c&oNo reason specified."));
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

	/**
	 * Kick the desired player for a specific reason.
	 *
	 * @param reason The reason for removal.
	 * @return false if the user isn't online or is not valid.
	 */
	public boolean kick(KickReason reason) {
		if (uuid == null) {
			return false;
		}
		if (!Objects.requireNonNull(getOfflinePlayer()).isOnline()) {
			return false;
		}
		Objects.requireNonNull(getPlayer()).kickPlayer(reason.toString());
		return true;
	}

	/**
	 * Kick the desired player for a specific reason.
	 *
	 * @param reason The reason for removal.
	 * @return false if the user isn't online or is not valid.
	 */
	public boolean kick(KickReason reason, boolean silent) {
		if (uuid == null) {
			return false;
		}
		if (!Objects.requireNonNull(getOfflinePlayer()).isOnline()) {
			return false;
		}
		if (!silent) {
			Bukkit.broadcastMessage(StringUtils.translate(MyEssentialsAPI.getInstance().getPrefix() + " &c&oPlayer &4" + getPlayer().getName() + " &c&owas kicked for &r" + '"' + reason.getReason() + "&r" + '"'));
		}
		Objects.requireNonNull(getPlayer()).kickPlayer(reason.toString());
		return true;
	}

	/**
	 * Kick the desired player for a specific reason.
	 *
	 * @param reason The reason for removal.
	 * @return false if the user isn't online or is not valid.
	 */
	public boolean kick(String reason, boolean silent) {
		if (uuid == null) {
			return false;
		}
		if (!Objects.requireNonNull(getOfflinePlayer()).isOnline()) {
			return false;
		}
		if (!silent) {
			Bukkit.broadcastMessage(StringUtils.translate(MyEssentialsAPI.getInstance().getPrefix() + " &c&oPlayer &4" + getPlayer().getName() + " &c&owas kicked for &r" + '"' + reason + "&r" + '"'));
		}
		Objects.requireNonNull(getPlayer()).kickPlayer(reason);
		return true;
	}


}
