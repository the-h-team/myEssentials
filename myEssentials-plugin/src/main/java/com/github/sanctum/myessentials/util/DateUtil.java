package com.github.sanctum.myessentials.util;

import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.myessentials.Essentials;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

public class DateUtil {

	/**
	 * Get a date object from the format (MM/DD/YYYY)
	 *
	 * @param time The time for this date.
	 * @return A date object or null if the format isn't recognized.
	 */
	public static Optional<Date> fromNormal(String time) {
		Date date = null;
		try {
			String example = "02/31/2021";
			DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
			date = format.parse(time);
		} catch (ParseException e) {
			Message.loggedFor(Essentials.getInstance()).error("Unable to format date w/ format " + '"' + time + '"');
		}
		return Optional.ofNullable(date);
	}

	/**
	 * Get a date object from the format (DAY,HOUR,MINUTE)
	 *
	 * @param time The time for this date.
	 * @return A date object or null if the format isn't recognized.
	 */
	public static Optional<Date> fromTimed(String time) {

		Date date = null;
		try {
			String example = "{CURRENT_MONTH},{CURRENT_YEAR},2,12,30";
			long month = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MONTH);
			long year = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR);
			DateFormat format = new SimpleDateFormat("MM,yyyy,d,H,m", Locale.ENGLISH);
			String to = month + "," + year + "," + time;
			date = format.parse(to);
		} catch (ParseException e) {
			Message.loggedFor(Essentials.getInstance()).error("Unable to format date w/ format " + '"' + time + '"');
		}
		return Optional.ofNullable(date);
	}

}
