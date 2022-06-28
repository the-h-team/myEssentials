package com.github.sanctum.myessentials.model.kit;

import com.github.sanctum.labyrinth.library.IllegalTimeFormatException;
import com.github.sanctum.labyrinth.library.ParsedTimeFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public final class KitTimeStamp {

	public static ParsedTimeFormat of(@NotNull String context) throws IllegalTimeFormatException {
		Pattern pattern = Pattern.compile("(\\d+)(d|hr|m|s)");
		Matcher matcher = pattern.matcher(context);
		String days = null;
		String hours = null;
		String minutes = null;
		String seconds = null;
		while (matcher.find()) {
			switch (matcher.group(2)) {
				case "d":
					days = matcher.group(1);
					break;
				case "hr":
					hours = matcher.group(1);
					break;
				case "m":
					minutes = matcher.group(1);
					break;
				case "s":
					seconds = matcher.group(1);
					break;
			}
		}
		if (days == null && hours == null && minutes == null && seconds == null)
			throw new IllegalTimeFormatException("Time format cannot be empty!");
		long d = 0, hr = 0, m = 0, s = 0;
		if (days != null) d = Long.parseLong(days);
		if (hours != null) hr = Long.parseLong(hours);
		if (minutes != null) m = Long.parseLong(minutes);
		if (seconds != null) s = Long.parseLong(seconds);
		return ParsedTimeFormat.of(d, hr, m, s);
	}

}
