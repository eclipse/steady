package com.sap.psr.vulas.shared.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.json.model.ConstructId;

public class ConstructIdUtil {

	private static final Log log = LogFactory.getLog(ConstructIdUtil.class);
	
	public static Set<ConstructId> filterWithRegex(Set<ConstructId> _in, String[] _qname_filter) {
		final Set<ConstructId> result = new HashSet<ConstructId>();
		final Set<Pattern> filter = new HashSet<Pattern>();
		Matcher m = null;
		for(String f: _qname_filter) 
			filter.add(Pattern.compile(f));
		final int count_before = _in.size();
		for(ConstructId c: _in) {
			for(Pattern p: filter) {
				m = p.matcher(c.getQname());
				if(m.matches())
					result.add(c);
			}
		}
		final int count_after = result.size();
		log.info("[" + (count_before-count_after) + "/" + count_before + "] items filtered");
		return result;
	}
}
