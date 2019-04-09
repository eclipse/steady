package com.sap.psr.vulas.shared.json.model.view;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * Json Views used to filter the entity fields to be serialized based on the API used.
 * 
 * Default: Used across all controllers for APIs that requires a minimal set of information for each entity (as it will exclude all fields with a different view annotated).
 * LibDetails: Used in LibraryController for API that includes details about the Library (including its counters extending the CountDetails view)
 * CountDetails: Used in Application and Library for counter fields
 */
public class Views {	
	public interface Default {}
	public interface LibDetails extends CountDetails{}
	public interface CountDetails {}
}
