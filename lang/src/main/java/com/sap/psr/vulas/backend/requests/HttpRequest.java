package com.sap.psr.vulas.backend.requests;

import java.io.IOException;
import java.io.Serializable;

import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.HttpResponse;

/**
 * Http request that can be send and saved to (loaded from) disk.
 *
 */
public interface HttpRequest extends Serializable {

	public HttpResponse send() throws BackendConnectionException;

	public void saveToDisk() throws IOException;

	public void savePayloadToDisk() throws IOException;
	
	public void loadFromDisk() throws IOException;
	
	public void loadPayloadFromDisk() throws IOException;

	public void deleteFromDisk() throws IOException;

	public void deletePayloadFromDisk() throws IOException;

	public String getFilename();
}
