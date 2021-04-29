package com.github.sanctum.myessentials.util.moderation;

import com.github.sanctum.labyrinth.gui.shared.SharedMenu;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.SkullItem;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.model.CooldownFinder;
import com.github.sanctum.myessentials.util.OfflinePlayerWrapper;
import com.github.sanctum.myessentials.util.ProvidedMessage;
import com.github.sanctum.myessentials.util.events.PlayerPendingHealEvent;
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object that encapsulates a source object and locates a bukkit player
 */
public final class PlayerSearch implements CooldownFinder {

	private CommandSender sender;

	private UUID uuid;

	protected PlayerSearch(OfflinePlayer target) {
		this.uuid = target.getUniqueId();
	}

	protected PlayerSearch(UUID uuid) {
		this.uuid = uuid;
	}

	protected PlayerSearch(CommandSender sender) {
		this.sender = sender;
	}

	protected PlayerSearch(String name) {
		OfflinePlayer search = new OfflinePlayerWrapper().get(name).orElse(null);
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
		return new PlayerSearch((OfflinePlayer) target);
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
	 * For console use primarily, wrap a command-sender to send formatted messaging to.
	 *
	 * @param sender The non human sender to wrap.
	 * @return A console messaging utility for protected circumstances.
	 */
	public static PlayerSearch look(CommandSender sender) {
		return new PlayerSearch(sender);
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

	public boolean isVanished() {
		return false;
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
		if (timer("MyBan-id-" + uuid.toString()) == null) {
			return null;
		}
		return factory(Objects.requireNonNull(timer("MyBan-id-" + uuid.toString())));
	}

	/**
	 * Get the player's ban timer, if they have one you can check information like the initial time and remaining.
	 *
	 * @param format The format display for the timer, if the timer is empty this could result in an NPE.
	 *               Format the timer using tags : {DAYS} {HOURS} {MINUTES} {SECONDS}
	 * @return The player's ban timer if one is present otherwise empty.
	 * @throws NullPointerException If the timer has no proper empty check and access is attempted.
	 */
	public @NotNull Optional<Cooldown> getBanTimer(String format) {
		if (uuid == null) {
			return Optional.empty();
		}
		if (timer("MyBan-id-" + uuid.toString()) == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(timer("MyBan-id-" + uuid.toString()).format(format));
	}

	/**
	 * Get the desired player's personal vault.
	 *
	 * @return A shared menu instance.
	 */
	public @NotNull Optional<SharedMenu> getVault() {
		if (uuid == null) {
			return Optional.empty();
		}
		return Optional.of(SharedMenu.get("MyVault-" + uuid.toString()));
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
	 * Heal the target.
	 *
	 * @param amount health points between 0 and 20
	 * @throws IllegalArgumentException if amount is over 20
	 * @throws IllegalStateException if {@link #getPlayer()} is null
	 */
	public void heal(double amount) throws IllegalArgumentException {
		heal(null, amount);
	}

	/**
	 * Heal the target.
	 *
	 * @param healer a healer; use null for console
	 * @param amount health points between 0 and 20
	 * @throws IllegalArgumentException if amount is over 20
	 * @throws IllegalStateException if {@link #getPlayer()} is null
	 */
	public void heal(@Nullable CommandSender healer, double amount) throws IllegalArgumentException {
		final Player target = getPlayer();
		if (target == null) throw new IllegalStateException("Target not present!");
		Bukkit.getPluginManager().callEvent(new PlayerPendingHealEvent(healer, target, amount));
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
	 * @param silent whether or not to kick player silently
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
	 * @param silent whether or not to kick player silently
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
	 * @param silent whether or not to kick player silently
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

	/**
	 * Send a pre-formatted plugin message to a given player or command-sender.
	 *
	 * @param message The message to send.
	 */
	public void sendMessage(ProvidedMessage message) {
		if (sender != null) {
			JavaPlugin.getProvidingPlugin(getClass()).getLogger().info(message.toString());
			return;
		}
		new Message(getPlayer(), MyEssentialsAPI.getInstance().getPrefix()).send(message.toString());
	}

	/**
	 * Send a pre-formatted plugin message to a given player or command-sender.
	 *
	 * @param text The message to send.
	 */
	public void sendMessage(String text) {
		if (sender != null) {
			JavaPlugin.getProvidingPlugin(getClass()).getLogger().info(text);
			return;
		}
		new Message(getPlayer(), MyEssentialsAPI.getInstance().getPrefix()).send(text);
	}


}
