package com.sap.psr.vulas.sign;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import java.io.File;

/** SignatureFactory interface. */
public interface SignatureFactory {

  /**
   * Returns true if the factory can compute a signature for the given {@link ConstructId}, false
   * otherwise.
   *
   * @param _id a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   * @return a boolean.
   */
  public boolean isSupportedConstructId(ConstructId _id);

  /**
   * Creates a {@link Signature} of the body of the given {@link Construct}.
   *
   * @param _construct a {@link com.sap.psr.vulas.Construct} object.
   * @return a {@link com.sap.psr.vulas.sign.Signature} object.
   */
  public Signature createSignature(Construct _construct);

  /**
   * Creates a {@link Signature} of the given {@link ConstructId} on the basis of the given file.
   *
   * @param _cid a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
   * @param _file a {@link java.io.File} object.
   * @return a {@link com.sap.psr.vulas.sign.Signature} object.
   */
  public Signature createSignature(ConstructId _cid, File _file);

  /**
   * computeChange.
   *
   * @param _from a {@link com.sap.psr.vulas.Construct} object.
   * @param _to a {@link com.sap.psr.vulas.Construct} object.
   * @return a {@link com.sap.psr.vulas.sign.SignatureChange} object.
   */
  public SignatureChange computeChange(Construct _from, Construct _to);
}
