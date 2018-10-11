package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Library;

public interface LibraryRepositoryCustom {

	public Library customSave(Library _lib);
	
	public Library saveIncomplete(Library _lib);
}
