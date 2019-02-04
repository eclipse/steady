package com.sap.psr.vulas.goals;

import java.io.Serializable;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Space;
import com.sap.psr.vulas.shared.json.model.Tenant;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Represents the context of a goal execution. All its elements can be specified using
 * the tenant, space and application related configuration settings.
 *
 * @see {@link CoreConfiguration#APP_CTX_GROUP} as well as ARTIF and VERSION
 * @see {@link CoreConfiguration#TENANT_TOKEN}
 * @see {@link CoreConfiguration#SPACE_TOKEN}
 * 
 * The secret tokens of the tenant and space, if any, will be included as HTTP headers
 * into every request sent to the Vulas backend.
 * 
 * @see {@link Constants#HTTP_TENANT_HEADER}
 * @see {@link Constants#HTTP_SPACE_HEADER}
 * 
 *
 */
public class GoalContext implements Serializable {

	private Tenant tenant = null;

	private Space space = null;

	private Application application = null;
	
	private VulasConfiguration vulasConfiguration = null;

	public boolean hasTenant() { return this.tenant!=null; }
	public Tenant getTenant() { return this.tenant; }
	public void setTenant(Tenant _t) { this.tenant = _t; }

	public boolean hasSpace() { return this.space!=null; }
	public Space getSpace() { return this.space; }
	public void setSpace(Space _s) { this.space = _s; }

	public boolean hasApplication() { return this.application!=null; }
	public Application getApplication() { return this.application; }
	public void setApplication(Application _a) { this.application = _a; }
	
	public VulasConfiguration getVulasConfiguration() { return vulasConfiguration; }
	public void setVulasConfiguration(VulasConfiguration vulasConfiguration) { this.vulasConfiguration = vulasConfiguration; }
	
	@Override
	public String toString() {
		final StringBuffer b = new StringBuffer();
		b.append("[");
		
		if(this.tenant!=null)
			b.append("tenant=").append(this.tenant.toString());
		
		if(this.space !=null) {
			if(!b.toString().equals("["))
				b.append(", ");
			b.append("space=").append(this.space.toString());
		}
		
		if(this.application!=null) {
			if(!b.toString().equals("["))
				b.append(", ");
			b.append("app=").append(this.application.toString());
		}		
		b.append("]");
		return b.toString();
	}
}
