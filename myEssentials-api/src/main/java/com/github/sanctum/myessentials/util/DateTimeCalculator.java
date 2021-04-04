package com.github.sanctum.myessentials.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

public class DateTimeCalculator {

	/**
	 * Get a date object from the format (MM/DD/YYYY)
	 *
	 * @param time The time for this date.
	 * @return A date object or null if the format isn't recognized.
	 */
	public static Optional<Date> atSpecific(String time) {
		Date date = null;
		try {
			String example = "02/31/2021";
			DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
			date = format.parse(time);
		} catch (ParseException ignored) {
		}
		return Optional.ofNullable(date);
	}

	/**
	 * Get a date object from the format (DAY,HOUR,MINUTE)
	 *
	 * @param time The time for this date.
	 * @return A date object or null if the format isn't recognized.
	 */
	public static Optional<Date> timeAtSpecific(String time) {

		Date date = null;
		try {
			String example = "{CURRENT_MONTH},{CURRENT_YEAR},2,12,30";
			long month = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MONTH);
			long year = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR);
			DateFormat format = new SimpleDateFormat("M,yyyy,d,H,m", Locale.ENGLISH);
			String to = month + "," + year + "," + time;
			date = format.parse(to);
		} catch (ParseException ignored) {
		}
		return Optional.ofNullable(date);
	}

	/**
	 * Get a date object from the format (HOUR,MINUTE)
	 *
	 * @param time The time for this date.
	 * @return A date object or null if the format isn't recognized.
	 */
	public static Optional<Date> hoursAtSpecific(String time) {

		Date date = null;
		try {
			String example = "{CURRENT_MONTH},{CURRENT_YEAR},{DAY},12,30";
			long day = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_MONTH);
			long month = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MONTH);
			long year = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR);
			DateFormat format = new SimpleDateFormat("M,yyyy,d,H,m", Locale.ENGLISH);
			String to = month + "," + year + "," + day + "," + time;
			date = format.parse(to);
		} catch (ParseException ignored) {
		}
		return Optional.ofNullable(date);
	}

	/**
	 * Get a date object from the format (MINUTE)
	 *
	 * @param minutes The time for this date.
	 * @return A date object or null if the format isn't recognized.
	 */
	public static Optional<Date> minutesFromNow(String minutes) {

		Date date = null;
		try {
			String example = "{CURRENT_MONTH},{CURRENT_YEAR},{DAY},{HOUR},(MINUTE)+30";
			long day = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_MONTH);
			long hour = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.HOUR_OF_DAY);
			long minute = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MINUTE) + Long.parseLong(minutes);
			long month = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MONTH);
			long year = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR);
			DateFormat format = new SimpleDateFormat("M,yyyy,d,H,m", Locale.ENGLISH);
			String to = month + "," + year + "," + day + "," + +hour + "," + minute;
			date = format.parse(to);
		} catch (ParseException ignored) {
		}
		return Optional.ofNullable(date);
	}

	/**
	 * Parse a mini date format using context : " #d#h#m#s "
	 * <p>
	 * The date for example can be " 0d2hr5m9s "
	 * <p>
	 * And the resulting long value will be based precisely on 0 days 2 hours 5 minutes and 9 seconds.
	 *
	 * @param time The date format to parse.
	 * @return A dated long value based upon conversion results.
	 * @throws java.time.format.DateTimeParseException If the format provided doesn't match internal requirements.
	 */
	public static Long parse(String time) {
		return Duration.parse("P" + time.replace("D", "DT").replace("r", "")).getSeconds();
	}

	/**
	 * Parse a time stamp format using context : " #m, #hr, #s "
	 * <p>
	 * One or more time stamps can be requested excluding days,
	 * months and years.
	 *
	 * @param time The date format to parse.
	 * @return A dated long value based upon conversion results.
	 * @throws java.time.format.DateTimeParseException If the format provided doesn't match internal requirements.
	 */
	public static Long parseShort(String time) {
		return Duration.parse("PT" + time.replace("r", "")).getSeconds();
	}

	/**
	 * Parse a time stamp format using days only.
	 *
	 * @param time The amounts of days to convert.
	 * @return A dated long value based upon conversion results.
	 * @throws java.time.format.DateTimeParseException If the format provided doesn't match internal requirements.
	 */
	public static Long parseDays(String time) {
		return Duration.parse("P" + time.replace("r", "")).getSeconds();
	}

}
