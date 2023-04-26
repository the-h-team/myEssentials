package com.github.sanctum.myessentials.model.warp;

import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.panther.container.PantherCollection;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WarpHolder extends Nameable {

	UUID getId();

	@Nullable Warp get(@NotNull String name);

	@NotNull PantherCollection<Warp> getAll();

	void add(@NotNull Warp warp);

	void remove(@NotNull Warp warp);

}
