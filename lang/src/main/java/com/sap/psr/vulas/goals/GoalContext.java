package com.sap.psr.vulas.goals;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Space;
import com.sap.psr.vulas.shared.json.model.Tenant;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import java.io.Serializable;

/**
 * Represents the context of a goal execution. All its elements can be specified using the tenant,
 * space and application related configuration settings.
 *
 * @see CoreConfiguration#APP_CTX_GROUP
 * @see CoreConfiguration#TENANT_TOKEN
 * @see CoreConfiguration#SPACE_TOKEN
 *     <p>The secret tokens of the tenant and space, if any, will be included as HTTP headers into
 *     every request sent to the Vulas backend.
 * @see Constants#HTTP_TENANT_HEADER
 * @see Constants#HTTP_SPACE_HEADER
 */
public class GoalContext implements Serializable {

  private static final long serialVersionUID = 5117042314456447399L;

  private Tenant tenant = null;

  private Space space = null;

  private Application application = null;

  private transient VulasConfiguration vulasConfiguration = null;

  /**
   * hasTenant.
   *
   * @return a boolean.
   */
  public boolean hasTenant() {
    return this.tenant != null;
  }
  /**
   * Getter for the field <code>tenant</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Tenant} object.
   */
  public Tenant getTenant() {
    return this.tenant;
  }
  /**
   * Setter for the field <code>tenant</code>.
   *
   * @param _t a {@link com.sap.psr.vulas.shared.json.model.Tenant} object.
   */
  public void setTenant(Tenant _t) {
    this.tenant = _t;
  }

  /**
   * hasSpace.
   *
   * @return a boolean.
   */
  public boolean hasSpace() {
    return this.space != null;
  }
  /**
   * Getter for the field <code>space</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Space} object.
   */
  public Space getSpace() {
    return this.space;
  }
  /**
   * Setter for the field <code>space</code>.
   *
   * @param _s a {@link com.sap.psr.vulas.shared.json.model.Space} object.
   */
  public void setSpace(Space _s) {
    this.space = _s;
  }

  /**
   * hasApplication.
   *
   * @return a boolean.
   */
  public boolean hasApplication() {
    return this.application != null;
  }
  /**
   * Getter for the field <code>application</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   */
  public Application getApplication() {
    return this.application;
  }
  /**
   * Setter for the field <code>application</code>.
   *
   * @param _a a {@link com.sap.psr.vulas.shared.json.model.Application} object.
   */
  public void setApplication(Application _a) {
    this.application = _a;
  }

  /**
   * Getter for the field <code>vulasConfiguration</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.util.VulasConfiguration} object.
   */
  public VulasConfiguration getVulasConfiguration() {
    return vulasConfiguration;
  }
  /**
   * Setter for the field <code>vulasConfiguration</code>.
   *
   * @param vulasConfiguration a {@link com.sap.psr.vulas.shared.util.VulasConfiguration} object.
   */
  public void setVulasConfiguration(VulasConfiguration vulasConfiguration) {
    this.vulasConfiguration = vulasConfiguration;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final StringBuffer b = new StringBuffer();
    b.append("[");

    if (this.tenant != null) b.append("tenant=").append(this.tenant.toString());

    if (this.space != null) {
      if (!b.toString().equals("[")) b.append(", ");
      b.append("space=").append(this.space.toString());
    }

    if (this.application != null) {
      if (!b.toString().equals("[")) b.append(", ");
      b.append("app=").append(this.application.toString());
    }
    b.append("]");
    return b.toString();
  }
}
