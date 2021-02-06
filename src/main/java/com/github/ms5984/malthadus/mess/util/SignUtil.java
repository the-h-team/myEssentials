package com.github.ms5984.malthadus.mess.util;

import org.bukkit.block.Block;
import org.bukkit.block.Sign; // Sign BlockState class

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SignUtil {
    private final Block block;

    public enum SignLine {
        FIRST_LINE(0), SECOND_LINE(1), THIRD_LINE(2), FOURTH_LINE(3);
        public final int index;
        SignLine(int index) {
            this.index = index;
        }
    }

    public SignUtil(Block block) {
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

    public Optional<String> getLine(SignLine line) {
        return getSign().map(sign -> sign.getLine(line.index)).map(string -> string.isEmpty() ? null : string);
    }

    public boolean setLine(SignLine line, String text) {
        final Optional<Sign> optionalSign = getSign();
        if (optionalSign.isPresent()) {
            final Sign sign = optionalSign.get();
            sign.setLine(line.index, text);
            return sign.update();
        }
        return false;
    }
}
