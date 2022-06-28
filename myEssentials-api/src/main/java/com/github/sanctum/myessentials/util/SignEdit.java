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
package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public final class SignEdit {
    private final Block block;

    public enum Line {
        ONE(0), TWO(1), THREE(2), FOUR(3);
        public final int index;

        Line(int index) {
            this.index = index;
        }
    }

    public SignEdit(Block block) {
        if (!block.getType().name().contains("SIGN")) {
            throw new IllegalArgumentException("Block does not represent a sign!");
        }
        this.block = block;
    }

    private Optional<Sign> getSign() {
        if (!block.getType().name().contains("SIGN")) {
            return Optional.empty();
        }
        return Optional.of((Sign) block.getState());
    }

    public List<String> getLines() {
        return getSign().map(sign -> Arrays.asList(sign.getLines())).orElse(Collections.emptyList());
    }

    public Optional<String> getLine(Line line) {
        return getSign().map(sign -> sign.getLine(line.index)).map(string -> string.isEmpty() ? null : string);
    }

    public SignEdit colorAll() {
        final Optional<Sign> optionalSign = getSign();
        if (optionalSign.isPresent()) {
            Sign s = optionalSign.get();
            int index = 0;
            for (String line : s.getLines()) {
                s.setLine(index, StringUtils.use(line).translate());
                index++;
            }
            s.update();
        }
        return this;
    }

    public boolean setLine(Line line, String text) {
        final Optional<Sign> optionalSign = getSign();
        if (optionalSign.isPresent()) {
            final Sign sign = optionalSign.get();
            sign.setLine(line.index, StringUtils.use(text).translate());
            return sign.update();
        }
        return false;
    }
}
