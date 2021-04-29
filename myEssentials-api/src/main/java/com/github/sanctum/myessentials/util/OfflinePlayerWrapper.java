package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class OfflinePlayerWrapper extends UniformedComponents<OfflinePlayer> {
	@Override
	public List<OfflinePlayer> list() {
		return new ArrayList<>(Arrays.asList(Bukkit.getOfflinePlayers()));
	}

	public Optional<OfflinePlayer> get(String name) {
		return sort().stream().filter(p -> p.getName().equals(name)).findFirst();
	}

	@Override
	public List<OfflinePlayer> sort() {
		List<OfflinePlayer> list = new ArrayList<>(list());
		list.sort(Comparator.comparingDouble(OfflinePlayer::getLastPlayed));
		return list;
	}

	@Override
	public List<OfflinePlayer> sort(Comparator<? super OfflinePlayer> comparable) {
		List<OfflinePlayer> list = new ArrayList<>(list());
		list.sort(comparable);
		return list;
	}

	@Override
	public Collection<OfflinePlayer> collect() {
		return sort();
	}

	@Override
	public OfflinePlayer[] array() {
		return list().toArray(new OfflinePlayer[0]);
	}

	@Override
	public <R> Stream<R> map(Function<? super OfflinePlayer, ? extends R> mapper) {
		return list().stream().map(mapper);
	}

	@Override
	public Stream<OfflinePlayer> filter(Predicate<? super OfflinePlayer> predicate) {
		return list().stream().filter(predicate);
	}

	@Override
	public OfflinePlayer getFirst() {
		return list().get(0);
	}

	@Override
	public OfflinePlayer getLast() {
		return list().get(Math.max(list().size() - 1, 0));
	}

	@Override
	public OfflinePlayer get(int index) {
		return list().get(index);
	}
}
