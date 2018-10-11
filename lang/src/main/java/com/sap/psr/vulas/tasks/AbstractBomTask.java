package com.sap.psr.vulas.tasks;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.json.model.Library;

public abstract class AbstractBomTask extends AbstractTask implements BomTask {

	private Application app = null;
	
	protected void setCompletedApplication(Application _app) { this.app = _app; }
	
	@Override
	public Application getCompletedApplication() { return app; }
	
	@Override
	public String toString() { return this.getClass().getSimpleName(); }
}
