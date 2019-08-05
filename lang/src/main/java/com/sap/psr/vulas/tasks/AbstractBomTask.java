package com.sap.psr.vulas.tasks;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.Library;

/**
 * <p>Abstract AbstractBomTask class.</p>
 *
 */
public abstract class AbstractBomTask extends AbstractTask implements BomTask {

	private Application app = null;
	
	/**
	 * <p>setCompletedApplication.</p>
	 *
	 * @param _app a {@link com.sap.psr.vulas.shared.json.model.Application} object.
	 */
	protected void setCompletedApplication(Application _app) { this.app = _app; }
	
	/** {@inheritDoc} */
	@Override
	public Application getCompletedApplication() { return app; }
	
	/** {@inheritDoc} */
	@Override
	public String toString() { return this.getClass().getSimpleName(); }
}
