package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import com.github.sanctum.myessentials.util.moderation.PlayerSearch;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerWrapper extends UniformedComponents<Player> {

	@Override
	public List<Player> list() {
		return new ArrayList<>(Bukkit.getOnlinePlayers());
	}

	public Optional<Player> get(String name) {
		return sort().stream().filter(p -> p.getName().equals(name)).findFirst();
	}

	@Override
	public List<Player> sort() {
		List<Player> list = new ArrayList<>(list());
		list.sort(Comparator.comparingDouble(Player::getTotalExperience));
		list.removeIf(p -> PlayerSearch.look(p).isVanished());
		return list;
	}

	@Override
	public List<Player> sort(Comparator<? super Player> comparable) {
		List<Player> list = new ArrayList<>(list());
		list.sort(comparable);
		list.removeIf(p -> PlayerSearch.look(p).isVanished());
		return list;
	}

	@Override
	public Collection<Player> collect() {
		return sort();
	}

	@Override
	public Player[] array() {
		return list().toArray(new Player[0]);
	}

	@Override
	public <R> Stream<R> map(Function<? super Player, ? extends R> mapper) {
		return list().stream().map(mapper);
	}

	@Override
	public Stream<Player> filter(Predicate<? super Player> predicate) {
		return list().stream().filter(predicate);
	}

	@Override
	public Player getFirst() {
		return list().get(0);
	}

	@Override
	public Player getLast() {
		return list().get(Math.max(list().size() - 1, 0));
	}

	@Override
	public Player get(int index) {
		return list().get(index);
	}

	public OfflinePlayerWrapper toOffline() {
		return PlayerSearch.getOfflinePlayers();
	}

}
