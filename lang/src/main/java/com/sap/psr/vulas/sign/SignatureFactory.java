package com.sap.psr.vulas.sign;

import java.io.File;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.shared.json.model.ConstructId;

public interface SignatureFactory {
	
	/**
	 * Returns true if the factory can compute a signature for the given {@link ConstructId}, false otherwise.
	 */
	public boolean isSupportedConstructId(ConstructId _id);

	/**
	 * Creates a {@link Signature} of the body of the given {@link Construct}.
	 * @param _construct
	 * @return
	 */
	public Signature createSignature(Construct _construct);
	
	/**
	 * Creates a {@link Signature} of the given {@link ConstructId} on the basis of the given file.
	 */
	public Signature createSignature(ConstructId _cid, File _file);
	
	public SignatureChange computeChange(Construct _from, Construct _to);
}
