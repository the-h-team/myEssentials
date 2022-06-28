package com.github.sanctum.myessentials.model.warp;

import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WarpHolder extends Nameable {

	UUID getId();

	@Nullable Warp get(@NotNull String name);

	@NotNull LabyrinthCollection<Warp> getAll();

	void add(@NotNull Warp warp);

	void remove(@NotNull Warp warp);

}
