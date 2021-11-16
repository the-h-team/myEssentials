/*
 *  Copyright 2021 Sanctum <https://github.com/the-h-team>
 *
 *  This file is part of myEssentials, a derivative work inspired by the
 *  Essentials <http://ess3.net/> and EssentialsX <https://essentialsx.net/>
 *  projects, both licensed under the GPLv3.
 *
 *  This software is currently in development and its licensing has not
 *  yet been chosen.
 */
package com.github.sanctum.myessentials.commands;

import com.github.sanctum.myessentials.model.GameModeCommandBase;
import com.github.sanctum.myessentials.model.InternalCommandData;
import org.bukkit.GameMode;

/**
 * Set yourself or other players to Adventure Mode.
 */
public final class GMACommand extends GameModeCommandBase {
	public GMACommand() {
		super(InternalCommandData.GMA_COMMAND, GameMode.ADVENTURE, "Adventure Mode");
	}
}
