package com.github.sanctum.myessentials.util.moderation;

import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulate an array of messages to be sent to a kicked user in order.
 */
public final class KickReason {

	private final Map<Integer, String> messages = new HashMap<>();
	private String primaryKickReason = "&rNo reason specified.";

	private KickReason() {
	}

	/**
	 * Add a message to the kick reason that will be displayed in the order
	 * you place it in.
	 *
	 * @param line The importance this message has is determined by the index
	 *             The lower the number the sooner the message appears in order.
	 * @param text The message to be received.
	 * @return The same kick reasoning object.
	 */
	public KickReason input(int line, String text) {
		messages.put(line, StringUtils.use(text).translate());
		return this;
	}

	/**
	 * Specify the primary reason the user is being kicked.
	 *
	 * @param reason The primary kick/ban reason.
	 * @return The same kick reasoning object.
	 */
	public KickReason reason(String reason) {
		this.primaryKickReason = reason;
		return this;
	}

	/**
	 * @return The reason specified primarily for kicking.
	 */
	public String getReason() {
		return this.primaryKickReason;
	}

	/**
	 * @return Get all the appropriate information displayed in order
	 * and colored translated.
	 */
	@Override
	public String toString() {
		List<Integer> list = new ArrayList<>(messages.keySet());
		Collections.sort(list);
		StringBuilder builder = new StringBuilder();
		for (Integer i : list) {
			builder.append(messages.get(i)).append("\n");
		}
		int stop = builder.length() - 1;
		return builder.substring(0, stop);
	}

	/**
	 * Encapsulate an array of messages to be sent to a kicked user in order.
	 *
	 * @return An encapsulate kick reason.
	 */
	public static KickReason next() {
		return new KickReason();
	}
}