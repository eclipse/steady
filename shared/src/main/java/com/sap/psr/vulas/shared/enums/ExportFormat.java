package com.sap.psr.vulas.shared.enums;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.json.model.Application;

/**
 * Goal types for a given {@link Application} or workspace.
 */
public enum ExportFormat {

	CSV, JSON;

	private static Log log = LogFactory.getLog(ExportFormat.class);

	public static ExportFormat parseFormat(String _format, @NotNull ExportFormat _default) {
		if(_format==null || _format.equals("")) {
			log.warn("Invalid format [" + _format + "], returning the default [" + _default + "]");
			return _default;
		}
		for (ExportFormat t : ExportFormat.values())
			if (t.name().equalsIgnoreCase(_format))
				return t;
		log.warn("Invalid format [" + _format + "], returning the default [" + _default + "]");
		return _default;
	}

	/**
	 * Returns the Http content type for the given {@link ExportFormat}, either "text/csv;charset=UTF-8" or "application/json;charset=UTF-8".
	 * 
	 * @param _f
	 * @return
	 */
	public static String getHttpContentType(@NotNull ExportFormat _f) {
		switch(_f) {
			case CSV: return "text/csv;charset=UTF-8";
			case JSON: return "application/json;charset=UTF-8";
			default: return "text/plain;charset=UTF-8";
		}
	}
}