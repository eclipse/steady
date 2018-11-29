package com.sap.psr.vulas.shared.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

/**
 * Helper methods for string formatting.
 *
 */
public class StringUtil {

	public static final long KILOBYTE = 1024L;
	public static final long MEGABYTE = 1024L * 1024L;
	public static final long MILLI_IN_MIN = 60L * 1000L;

	public static final long NANOS_IN_MIN = 60L * 1000L * 1000L *1000L;

	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public static final String formatMinString(double _d) { return String.format("[%7.1f min]", _d); }
	public static final String formatMinString(long _n) { return formatMinString((double)_n/(double)MILLI_IN_MIN); }

	/**
	 * 
	 * @param _d
	 * @return
	 */
	public static final String formatMBString(double _d) { return String.format("%.2f MB", _d); }

	public static final String formatKBString(double _d) { return String.format("%.2f KB", _d); }

	/**
	 * Converts byte to Megabyte and returns a string representation in the form "XXXX.Y MB".
	 * @param _bytes the number of bytes
	 * @see #byteToMBString(long)
	 * @return
	 */
	public static final String byteToMBString(double _bytes) { return formatMBString(_bytes/(double)MEGABYTE); }

	public static final String byteToKBString(long _bytes)   { return formatKBString((double)_bytes/(double)KILOBYTE); }

	/**
	 * Converts byte to Megabyte and returns a string representation in the form "XXXX.Y MB".
	 * @param _d the number of bytes
	 * @see #byteToMBString(long)
	 * @return
	 */
	public static final String byteToMBString(long _bytes)   { return formatMBString((double)_bytes/(double)MEGABYTE); }

	public static final String formatDate(long _ms) { return FORMAT.format(new Date(_ms)); }

	/**
	 * Use {@link StringUtil#nanoToFlexDurationString(long)} instead.
	 * @param _nano
	 * @return
	 */
	@Deprecated
	public static final String nanoToMinString(long _nano) { return String.format("[%4.4f min]", (double)_nano/(double)NANOS_IN_MIN); }

	/**
	 * Returns a string representation of the given nano seconds. In case more than a minute passed, the representation
	 * [hhh:mm:ss] will be used, otherwise [ss.SSS ms].
	 * 
	 * @param _nano
	 * @return
	 */
	public static final String nanoToFlexDurationString(long _nano) {
		final long h  = TimeUnit.NANOSECONDS.toHours(_nano);
		final long m  = TimeUnit.NANOSECONDS.toMinutes(_nano) - TimeUnit.HOURS.toMinutes(h);
		final long s  = TimeUnit.NANOSECONDS.toSeconds(_nano) - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m);
		final long ms = TimeUnit.NANOSECONDS.toMillis(_nano) - TimeUnit.HOURS.toMillis(h) - TimeUnit.MINUTES.toMillis(m) - TimeUnit.SECONDS.toMillis(s);
		if(m>0)
			return String.format("[%03d:%02d:%02d]", h, m, s);
		else
			return String.format("[%02d.%03d ms]", s, ms);
	}

	public static final String msToMinString(long _ms) { return String.format("[%4.4f min]", (double)_ms/(double)MILLI_IN_MIN); }

	public static String join(Object[] _objs, String _sep) {
		final List<String> objs = new ArrayList<String>();
		for(Object o: _objs)
			objs.add(o.toString());
		return StringUtil.join(objs, _sep);
	}

	public static String join(String[] _strings, String _sep) {
		return StringUtil.join(Arrays.asList(_strings), _sep);
	}

	public static String join(List<String> _strings, String _sep) {
		final StringBuilder b = new StringBuilder();
		int i = 0;
		for(String p: _strings) {
			if(i++>0) b.append(_sep);
			b.append(p.toString());
		}
		return b.toString();
	}

	public static String join(Set<String> _strings, String _sep) {
		final StringBuilder b = new StringBuilder();
		int i = 0;
		for(String p: _strings) {
			if(i++>0) b.append(_sep);
			b.append(p.toString());
		}
		return b.toString();
	}

	public static String padLeft(String _value, int _length) { return String.format("%1$" + _length + "s", _value); }
	public static String padLeft(long _value, int _length) { return padLeft(Long.toString(_value), _length); }
	public static String padLeft(int _value, int _length) { return padLeft(Integer.toString(_value), _length); }
	public static String padLeft(boolean _value, int _length) { return padLeft(Boolean.toString(_value), _length); }
	public static String padLeft(double _value, int _length) { return padLeft(Double.toString(_value), _length); }

	/**
	 * Returns true if the given {@link String} is not null and its length is shorter or equal to the given number.
	 * Throws an {@link IllegalArgumentException} otherwise.
	 * @return
	 */
	public static boolean meetsLengthConstraint(String _string, int _length) throws IllegalArgumentException {
		if(_string==null)
			throw new IllegalArgumentException("String argument is null");
		else if(_string.length()>_length)
			throw new IllegalArgumentException("String argument exceeds the length of [" + _length + "] characters");
		else
			return true;
	}

	public static final String getRandonString(int _length) {
		final String rnd = Double.toString(Math.random()); 
		return rnd.substring(2, Math.min(_length+2, rnd.length()));
	}

	/**
	 * Removes square brackets (if any), splits the given String using comma (,) and returns an array of trimmed values. 
	 * @return
	 */
	public static String[] toArray(@NotNull String _string) {
		if(_string.startsWith("[") && _string.endsWith("]"))
			_string = _string.substring(1, _string.length()-1);
		final String[] values = _string.split(",");
		for(int i=0; i<values.length; i++)
			values[i] = values[i].trim();
		return values;
	}

	/**
	 * Returns true of the given arrayis null, empty or contains only one element that equals an empty {@link String}.
	 * @param _array
	 * @return
	 */
	public static boolean isEmptyOrContainsEmptyString(String[] _array) {
		return _array==null || _array.length==0 || (_array.length==1 && _array[0].equals(""));
	}
	
	/**
	 * Returns true if the given String matches to one of the provided {@link Pattern}s, false otherwise.
	 * 
	 * @param _string
	 * @param _patterns
	 * @return
	 */
	public static boolean matchesPattern(String _string, Pattern[] _patterns) {
		if(_string==null)
			return false;
		for(Pattern p: _patterns) {
			final Matcher m = p.matcher(_string);
			if(m.matches()) {
				return true;
			}
		}
		return false;
	}
}
