/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.listeners;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.event.DefaultEvent;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.myessentials.api.MyEssentialsAPI;
import com.github.sanctum.myessentials.commands.PowertoolCommand;
import com.github.sanctum.myessentials.util.ConfiguredMessage;
import com.github.sanctum.myessentials.util.SignFunction;
import com.github.sanctum.myessentials.util.events.PendingTeleportEvent;
import com.github.sanctum.myessentials.util.events.PlayerFeedEvent;
import com.github.sanctum.myessentials.util.events.PlayerHealEvent;
import com.github.sanctum.myessentials.util.events.PlayerPendingFeedEvent;
import com.github.sanctum.myessentials.util.events.PlayerPendingHealEvent;
import com.github.sanctum.myessentials.util.moderation.KickReason;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import com.github.sanctum.myessentials.util.teleportation.TeleportRequest;
import com.github.sanctum.panther.event.Subscribe;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.BanEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

// TODO: cleanup class a little, were gonna have to make due with consolidation like "PlayerEvent" & "EntityEvent" & others
public class PlayerEventListener implements Listener {
	private final Plugin plugin = JavaPlugin.getProvidingPlugin(getClass());

	private static final Map<UUID, Location> prevLocations = new HashMap<>();

	/**
	 * Checks if a location is safe (solid ground with 2 breathable blocks)
	 *
	 * @param location Location to check
	 * @return True if location is safe
	 */
	boolean hasSurface(Location location) {
		Block feet = location.getBlock();
		Block head = feet.getRelative(BlockFace.UP);
		if (!feet.getType().isAir() && !feet.getLocation().add(0, 1, 0).getBlock().getType().isAir() && !head.getType().isAir()) {
			return false; // not transparent (will suffocate)
		}
		return feet.getRelative(BlockFace.DOWN).getType().isSolid(); // not solid
	}


	@EventHandler
	public void onMove(PlayerMoveEvent e) {

		if (e.getTo() != null) {
			if (e.getFrom().getX() != e.getTo().getX() && e.getFrom().getY() != e.getTo().getY() && e.getFrom().getZ() != e.getTo().getZ()) {
				TeleportRequest r = MyEssentialsAPI.getInstance().getTeleportRunner().getActiveRequests()
						.stream().filter(pr -> pr.getPlayerTeleporting().getUniqueId().equals(e.getPlayer().getUniqueId()))
						.findFirst()
						.orElse(null);

				if (r != null) {
					if (r.getStatus() == TeleportRequest.Status.ACCEPTED) {
						MyEssentialsAPI.getInstance().getTeleportRunner().cancelRequest(r);
						Mailer.empty(e.getPlayer()).chat(MyEssentialsAPI.getInstance().getPrefix() + " " + ConfiguredMessage.TP_CANCELLED.get()).deploy();
					}
				}

			}
		}
	}

	@EventHandler
	public void onSign(SignChangeEvent e) {
		// check player permission
		TaskScheduler.of(() -> {
			SignFunction.ofDefault(e.getBlock()).initialize(e.getPlayer());
			SignFunction.ofLibrary(e.getBlock()).initialize(e.getPlayer());
		}).scheduleLater(2L);
	}

	@Subscribe
	public void onInteract(DefaultEvent.Interact e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getBlock().map(b -> b.getState() instanceof Sign).orElse(false) && !e.getPlayer().getInventory().getItemInMainHand().getType().name().contains("DYE")) {
				SignFunction.ofDefault(e.getBlock().get()).run(e.getPlayer());
				SignFunction.ofLibrary(e.getBlock().get()).run(e.getPlayer());
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if (StringUtils.use(e.getMessage()).containsIgnoreCase("${jndi:ldap:"))
			e.setCancelled(true); // logj command prevention.
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTeleport(PendingTeleportEvent e) {
		if (!e.getDestination().getDestinationPlayer().isPresent()) return;
		e.setDelay(0);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();

		PlayerSearch search = PlayerSearch.look(p);
		Cooldown timer = search.getBanTimer();
		if (timer != null) {
			if (!timer.isComplete()) {
				e.disallow(PlayerLoginEvent.Result.KICK_BANNED, KickReason.next()
						.input(1, MyEssentialsAPI.getInstance().getPrefix())
						.input(2, " ")
						.input(3, ConfiguredMessage.LOGIN_TEMP_BANNED.toString())
						.input(4, "")
						.input(5, ConfiguredMessage.LOGIN_BANNED_REASON.replace(search.getBanEntry().map(BanEntry::getReason).orElse("null")))
						.input(6, "")
						.input(7, ConfiguredMessage.LOGIN_BAN_EXPIRES.replace(MessageFormat.format("&r(D&r)&e{0} &r(H&r)&e{1} &r(M&r)&e{2} &r(S&r)&e{3}", Math.abs(timer.getDays()), Math.abs(timer.getHours()), Math.abs(timer.getMinutes()), Math.abs(timer.getSeconds()))))
						.toString());
			} else {
				PlayerSearch.look(p).unban();
				LabyrinthProvider.getInstance().remove(timer);
				e.allow();
			}
		}

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onTeleport(PlayerTeleportEvent e) {
		if (!e.getFrom().equals(e.getTo())) {
			prevLocations.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
		}
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (StringUtils.use(e.getMessage()).containsIgnoreCase("/plugins")) {
			e.getPlayer().performCommand("pl");
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (e.getItem() != null) {
				ItemStack i = e.getItem();
				if (i.hasItemMeta()) {
					NamespacedKey match = new NamespacedKey(PowertoolCommand.KEY.getNamespace(), PowertoolCommand.KEY.getKey());
					if (i.getItemMeta().getPersistentDataContainer().has(match, PersistentDataType.STRING)) {
						e.setCancelled(true);
						String command = i.getItemMeta().getPersistentDataContainer().get(match, PersistentDataType.STRING);
						Bukkit.dispatchCommand(e.getPlayer(), command);

					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPendingHealEvent(PlayerPendingHealEvent e) {
		Bukkit.getPluginManager().callEvent(new PlayerHealEvent(e));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFinalHealEvent(PlayerHealEvent e) {
		final Player target = e.getTarget();
		final CommandSender healer = e.getHealer();
		double s = target.getHealth() + e.getAmount();
		e.getTarget().setHealth(s < 20 ? s : 20);
		if (healer != null) {
			if (healer instanceof Player) {
				Player heal = (Player) healer;
				Mailer.empty(target).chat(ConfiguredMessage.PLAYER_HEALED_YOU.replace(plugin.getName(), heal.getName())).deploy();
			} else {
				Mailer.empty(target).chat(ConfiguredMessage.CONSOLE_HEALED_YOU.replace(plugin.getName())).deploy();
			}
		} else {
			Mailer.empty(target).chat(ConfiguredMessage.HEALED.replace(plugin.getName())).deploy();
		}
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPendingFeedEvent(PlayerPendingFeedEvent e) {
		Bukkit.getPluginManager().callEvent(new PlayerFeedEvent(e));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onFinalHealEvent(PlayerFeedEvent e) {
		final Player target = e.getTarget();
		final CommandSender healer = e.getHealer();

		int food_level = target.getFoodLevel() + e.getAmountReal();

		food_level = Math.min(food_level, 20);
		e.getTarget().setFoodLevel(food_level);

		// Only set saturation if food is full
		if (food_level == 20) {
			e.getTarget().setSaturation(20);
		}

		if (healer != null) {
			if (healer instanceof Player) {
				Player heal = (Player) healer;
				Mailer.empty(target).chat(ConfiguredMessage.PLAYER_FED_YOU.replace(plugin.getName(), heal.getName())).deploy();
			} else {
				Mailer.empty(target).chat(ConfiguredMessage.CONSOLE_FED_YOU.replace(plugin.getName())).deploy();
			}
		} else {
			Mailer.empty(target).chat(ConfiguredMessage.FED.replace(plugin.getName())).deploy();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onWorldChange(PlayerChangedWorldEvent e) {
		prevLocations.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDeath(PlayerDeathEvent e) {
		prevLocations.put(e.getEntity().getUniqueId(), e.getEntity().getLocation());
	}

	public static Map<UUID, Location> getPrevLocations() {
		return Collections.unmodifiableMap(prevLocations);
	}


}
