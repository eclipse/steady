/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.shared.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

/**
 * Helper methods for string formatting.
 */
public class StringUtil {

    /** Constant <code>KILOBYTE=1024L</code> */
    public static final long KILOBYTE = 1024L;
    /** Constant <code>MEGABYTE=1024L * 1024L</code> */
    public static final long MEGABYTE = 1024L * 1024L;
    /** Constant <code>MILLI_IN_MIN=60L * 1000L</code> */
    public static final long MILLI_IN_MIN = 60L * 1000L;

    /** Constant <code>NANOS_IN_MIN=60L * 1000L * 1000L *1000L</code> */
    public static final long NANOS_IN_MIN = 60L * 1000L * 1000L * 1000L;

    private static final String FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(FORMAT_STRING);

    /**
     * <p>formatMinString.</p>
     *
     * @param _d a double.
     * @return a {@link java.lang.String} object.
     */
    public static final String formatMinString(double _d) {
        return String.format("[%7.1f min]", _d);
    }
    /**
     * <p>formatMinString.</p>
     *
     * @param _n a long.
     * @return a {@link java.lang.String} object.
     */
    public static final String formatMinString(long _n) {
        return formatMinString((double) _n / (double) MILLI_IN_MIN);
    }

    /**
     * <p>formatMBString.</p>
     *
     * @param _d a double.
     * @return a {@link java.lang.String} object.
     */
    public static final String formatMBString(double _d) {
        return String.format("%.2f MB", _d);
    }

    /**
     * <p>formatKBString.</p>
     *
     * @param _d a double.
     * @return a {@link java.lang.String} object.
     */
    public static final String formatKBString(double _d) {
        return String.format("%.2f KB", _d);
    }

    /**
     * Converts byte to Megabyte and returns a string representation in the form "XXXX.Y MB".
     *
     * @param _bytes the number of bytes
     * @see #byteToMBString(long)
     * @return a {@link java.lang.String} object.
     */
    public static final String byteToMBString(double _bytes) {
        return formatMBString(_bytes / (double) MEGABYTE);
    }

    /**
     * <p>byteToKBString.</p>
     *
     * @param _bytes a long.
     * @return a {@link java.lang.String} object.
     */
    public static final String byteToKBString(long _bytes) {
        return formatKBString((double) _bytes / (double) KILOBYTE);
    }

    /**
     * Converts byte to Megabyte and returns a string representation in the form "XXXX.Y MB".
     *
     * @param _bytes the number of bytes
     * @return a {@link java.lang.String} object.
     */
    public static final String byteToMBString(long _bytes) {
        return formatMBString((double) _bytes / (double) MEGABYTE);
    }

    /**
     * <p>formatDate.</p>
     *
     * @param _ms a long.
     * @return a {@link java.lang.String} object.
     */
    public static final synchronized String formatDate(long _ms) {
        return FORMAT.format(new Date(_ms));
    }

    /**
     * Use {@link StringUtil#nanoToFlexDurationString(long)} instead.
     *
     * @param _nano a long.
     * @return a {@link java.lang.String} object.
     */
    @Deprecated
    public static final String nanoToMinString(long _nano) {
        return String.format("[%4.4f min]", (double) _nano / (double) NANOS_IN_MIN);
    }

    /**
     * Returns a string representation of the given nano seconds. In case more than a minute passed, the representation
     * [hhh:mm:ss] will be used, otherwise [ss.SSS ms].
     *
     * @param _nano a long.
     * @return a {@link java.lang.String} object.
     */
    public static final String nanoToFlexDurationString(long _nano) {
        final long h = TimeUnit.NANOSECONDS.toHours(_nano);
        final long m = TimeUnit.NANOSECONDS.toMinutes(_nano) - TimeUnit.HOURS.toMinutes(h);
        final long s =
                TimeUnit.NANOSECONDS.toSeconds(_nano)
                        - TimeUnit.HOURS.toSeconds(h)
                        - TimeUnit.MINUTES.toSeconds(m);
        final long ms =
                TimeUnit.NANOSECONDS.toMillis(_nano)
                        - TimeUnit.HOURS.toMillis(h)
                        - TimeUnit.MINUTES.toMillis(m)
                        - TimeUnit.SECONDS.toMillis(s);
        if (m > 0) return String.format("[%03d:%02d:%02d]", h, m, s);
        else return String.format("[%02d.%03d ms]", s, ms);
    }

    /**
     * <p>msToMinString.</p>
     *
     * @param _ms a long.
     * @return a {@link java.lang.String} object.
     */
    public static final String msToMinString(long _ms) {
        return String.format("[%4.4f min]", (double) _ms / (double) MILLI_IN_MIN);
    }

    /**
     * <p>join.</p>
     *
     * @param _objs an array of {@link java.lang.Object} objects.
     * @param _sep a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(Object[] _objs, String _sep) {
        final List<String> objs = new ArrayList<String>();
        for (Object o : _objs) objs.add(o.toString());
        return StringUtil.join(objs, _sep);
    }

    /**
     * <p>join.</p>
     *
     * @param _strings an array of {@link java.lang.String} objects.
     * @param _sep a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(String[] _strings, String _sep) {
        return StringUtil.join(Arrays.asList(_strings), _sep);
    }

    /**
     * Joins a {@link List} of {@link String}s.
     *
     * @param _strings a {@link java.util.List} object.
     * @param _sep a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    /*public static String join(List<String> _strings, String _sep) {
    	final StringBuilder b = new StringBuilder();
    	int i = 0;
    	for(String p: _strings) {
    		if(i++>0) b.append(_sep);
    		b.append(p.toString());
    	}
    	return b.toString();
    }*/

    /**
     * Joins a {@link Set} of {@link String}s.
     *
     * @param _strings a {@link java.util.Set} object.
     * @param _sep a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    /*public static String join(Set<String> _strings, String _sep) {
    	final StringBuilder b = new StringBuilder();
    	int i = 0;
    	for(String p: _strings) {
    		if(i++>0) b.append(_sep);
    		b.append(p.toString());
    	}
    	return b.toString();
    }*/

    /**
     * Joins a {@link Collection} of {@link Object}s into a {@link String} by calling {@link Object#toString()}.
     *
     * @param _objs a collection of {@link java.lang.Object} objects.
     * @param _sep a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String join(Collection<?> _objs, String _sep) {
        final StringBuilder b = new StringBuilder();
        int i = 0;
        for (Object o : _objs) {
            if (i++ > 0) b.append(_sep);
            b.append(o.toString());
        }
        return b.toString();
    }

    /**
     * <p>padLeft.</p>
     *
     * @param _value a {@link java.lang.String} object.
     * @param _length a int.
     * @return a {@link java.lang.String} object.
     */
    public static String padLeft(String _value, int _length) {
        return String.format("%1$" + _length + "s", _value);
    }
    /**
     * <p>padLeft.</p>
     *
     * @param _value a long.
     * @param _length a int.
     * @return a {@link java.lang.String} object.
     */
    public static String padLeft(long _value, int _length) {
        return padLeft(Long.toString(_value), _length);
    }
    /**
     * <p>padLeft.</p>
     *
     * @param _value a int.
     * @param _length a int.
     * @return a {@link java.lang.String} object.
     */
    public static String padLeft(int _value, int _length) {
        return padLeft(Integer.toString(_value), _length);
    }
    /**
     * <p>padLeft.</p>
     *
     * @param _value a boolean.
     * @param _length a int.
     * @return a {@link java.lang.String} object.
     */
    public static String padLeft(boolean _value, int _length) {
        return padLeft(Boolean.toString(_value), _length);
    }
    /**
     * <p>padLeft.</p>
     *
     * @param _value a double.
     * @param _length a int.
     * @return a {@link java.lang.String} object.
     */
    public static String padLeft(double _value, int _length) {
        return padLeft(Double.toString(_value), _length);
    }

    /**
     * Returns true if the given {@link String} is not null and its length is shorter or equal to the given number.
     * Throws an {@link IllegalArgumentException} otherwise.
     *
     * @param _string a {@link java.lang.String} object.
     * @param _length a int.
     * @return a boolean.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public static boolean meetsLengthConstraint(String _string, int _length)
            throws IllegalArgumentException {
        if (_string == null) throw new IllegalArgumentException("String argument is null");
        else if (_string.length() > _length)
            throw new IllegalArgumentException(
                    "String argument exceeds the length of [" + _length + "] characters");
        else return true;
    }

    /**
     * <p>getRandonString.</p>
     *
     * @param _length a int.
     * @return a {@link java.lang.String} object.
     */
    public static final String getRandonString(int _length) {
        final String rnd = Double.toString(Math.random());
        return rnd.substring(2, Math.min(_length + 2, rnd.length()));
    }

    /**
     * Removes square brackets (if any), splits the given String using comma (,) and returns an array of trimmed values.
     *
     * @param _string a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] toArray(@NotNull String _string) {
        if (_string.startsWith("[") && _string.endsWith("]"))
            _string = _string.substring(1, _string.length() - 1);
        final String[] values = _string.split(",");
        for (int i = 0; i < values.length; i++) values[i] = values[i].trim();
        return values;
    }

    /**
     * Returns true of the given arrayis null, empty or contains only one element that equals an empty {@link String}.
     *
     * @param _array an array of {@link java.lang.String} objects.
     * @return a boolean.
     */
    public static boolean isEmptyOrContainsEmptyString(String[] _array) {
        return _array == null || _array.length == 0 || (_array.length == 1 && _array[0].equals(""));
    }

    /**
     * Returns true if the given String matches to one of the provided {@link Pattern}s, false otherwise.
     *
     * @param _string a {@link java.lang.String} object.
     * @param _patterns an array of {@link java.util.regex.Pattern} objects.
     * @return a boolean.
     */
    public static boolean matchesPattern(String _string, Pattern[] _patterns) {
        if (_string == null) return false;
        for (Pattern p : _patterns) {
            final Matcher m = p.matcher(_string);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }
}
