package org.dsi.sanitrack;

import java.util.Date;
import java.util.Calendar;

/**
 * Converts date to string in specified format
 */
public class DateFormat {

	/**
	 * Converts date to string in specified format
	 * 
	 * @param format
	 *            format for date converting (consist of 'yyyy', 'mm', 'dd'
	 *            parts with dividers between them)
	 * @param date
	 *            date to be converted
	 * @return string containing converted date
	 */
	public static String toString(String format, Date date) {
		StringBuffer buf = new StringBuffer(format);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		// Change "YYYY" to year
		String year = String.valueOf(cal.get(Calendar.YEAR));
		replace(buf, "YYYY", year);

		// Change "DD" to day of month
		String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {
			day = "0" + day;
		}
		replace(buf, "DD", day);

		// Change "MM" to month
		String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		if (month.length() == 1) {
			month = "0" + month;
		}
		replace(buf, "MM", month);

		// Change "hh" to hour
		String hour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		if (hour.length() == 1) {
			hour = "0" + hour;
		}
		replace(buf, "hh", hour);

		// Change "mm" to minutes
		String minute = String.valueOf(cal.get(Calendar.MINUTE));
		if (minute.length() == 1) {
			minute = "0" + minute;
		}
		replace(buf, "mm", minute);
		
		return buf.toString();
	}

	/**
	 * Replaces first found substring in string buffer to dest string
	 * 
	 * @param buf -
	 *            string buffer
	 * @param subStr -
	 *            substring for replacing
	 * @param dest -
	 *            string to insert to string buffer
	 * @return position of first symbol of replaced substring in buffer or -1
	 */
	private static int replace(StringBuffer buf, String src, String dest) {
		int pos = find(buf.toString(), src, 0);
		if (pos == -1) {
			return -1;
		}

		buf.delete(pos, pos + src.length());
		buf.insert(pos, dest);

		return -1;
	}

	/**
	 * Returns position of substring in a string
	 * 
	 * @param str -
	 *            string to search in
	 * @param subStr -
	 *            substring to find
	 * @param offset -
	 *            first position to search
	 * @return position of first occurence of substring in string or -1 if
	 *         nothing was found
	 */
	private static int find(String str, String subStr, int offset) {
		if (offset < 0 || offset >= str.length()
				|| str.length() < subStr.length()) {
			return -1;
		}

		int maxPos = str.length() - subStr.length();
		for (int pos = offset; pos <= maxPos; pos++) {
			if (str.startsWith(subStr, pos) == true) {
				return pos;
			}
		}

		return -1;
	}
}
