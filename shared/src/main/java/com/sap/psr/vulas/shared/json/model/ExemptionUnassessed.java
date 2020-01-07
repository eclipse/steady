package com.sap.psr.vulas.shared.json.model;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExemptionUnassessed implements IExemption {

	private static final Log log = LogFactory.getLog(ExemptionUnassessed.class);

	/**
	 * Configuration setting <code>REP_EXCL_UNASS="vulas.report.exceptionExcludeUnassessed"</code>.
	 */
	public final static String CFG = "vulas.report.exceptionExcludeUnassessed";

	/**
	 * Determines whether unassessed vulnerable dependencies throw a build exception or not.
	 */
	public enum Value { IGN_UNASS_ALL, IGN_UNASS_KNOWN };

	private Value value = null;

	public ExemptionUnassessed(Value _value) { this.value = _value; }

	@Override
	public boolean isExempted(VulnerableDependency _vd) {
		if(this.value.equals(Value.IGN_UNASS_ALL))
			return !_vd.isAffectedVersionConfirmed();
		else if(this.value.equals(Value.IGN_UNASS_KNOWN))
			return !_vd.isAffectedVersionConfirmed() && _vd.getDep().getLib().isWellknownDigest();
		else
			return false;
	}

	@Override
	public String getReason() {
		if(this.value.equals(Value.IGN_UNASS_ALL))
			return "All unassessed findings are exempted (according to configuration setting [" + CFG + "])";
		else if(this.value.equals(Value.IGN_UNASS_KNOWN))
			return "Unassessed findings in libraries known to artifact repositories such as Maven Central are exempted (according to configuration setting [" + CFG + "])";
		else
			return "Illegal State, check configuration setting [" + CFG + "]";
	}

	/**
	 * Reads the {@link Configuration} setting {@link ExemptionUnassessed#CFG} (if any) in order to create one {@link Exemption}.
	 * 
	 * @param _cfg
	 * @return
	 */
	public static IExemption readFromConfiguration(Configuration _cfg) {
		IExemption e = null;
		final String setting = _cfg.getString(CFG, null);
		if(setting!=null) {
			if(setting.equalsIgnoreCase(Value.IGN_UNASS_ALL.toString())) {
				e = new ExemptionUnassessed(Value.IGN_UNASS_ALL);
				ExemptionUnassessed.log.warn("All unassessed vulnerabilities will be ignored");
			}
			else if(setting.equalsIgnoreCase(Value.IGN_UNASS_KNOWN.toString())) {
				e = new ExemptionUnassessed(Value.IGN_UNASS_KNOWN);
				ExemptionUnassessed.log.warn("All unassessed vulnerabilities in archives with known digests will be ignored");
			}
		}
		return e;
	}

	@Override
	public String toString() {
		return "ExemptionUnassessed [value=" + value + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExemptionUnassessed other = (ExemptionUnassessed) obj;
		if (value != other.value)
			return false;
		return true;
	}
}
