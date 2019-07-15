package com.sap.psr.vulas.nodejs.sign;

import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.util.DigestUtil;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.sign.Signature;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class NodejsConstructDigest implements Signature {

    public static enum ComputedFromType { FILE, BODY }

    private static final int MAX_STRING_LENGTH = 100;

    private String computedFrom = null;

    private ComputedFromType computedFromType = null;

    private String digest = null;

    private DigestAlgorithm digestAlgorithm = null;

    public NodejsConstructDigest(Path _path, DigestAlgorithm _alg) throws IllegalArgumentException {
        if(!FileUtil.isAccessibleDirectory(_path))
            throw new IllegalArgumentException("Path argument [" + _path + "] is not a valid file");
        this.digest = FileUtil.getDigest(_path.toFile(), _alg);
        this.digestAlgorithm = _alg;
        this.computedFrom = _path.getFileName().toString();
        this.computedFromType = ComputedFromType.FILE;
    }

    public NodejsConstructDigest(String _string, DigestAlgorithm _alg) {
        if(_string==null)
            throw new IllegalArgumentException("String argument cannot be null");
        this.digest = DigestUtil.getDigestAsString(_string, StandardCharsets.UTF_8, _alg);
        this.digestAlgorithm = _alg;
        if(_string.length()>MAX_STRING_LENGTH)
            this.computedFrom = _string.substring(0, MAX_STRING_LENGTH-3) + "...";
        else
            this.computedFrom = _string;
        this.computedFromType = ComputedFromType.BODY;
    }

    public String getComputedFrom() {
        return computedFrom;
    }

    public void setComputedFrom(String computedFrom) {
        this.computedFrom = computedFrom;
    }

    public ComputedFromType getComputedFromType() {
        return computedFromType;
    }

    public void setComputedFromType(ComputedFromType computedFromType) {
        this.computedFromType = computedFromType;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public DigestAlgorithm getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(DigestAlgorithm digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    @Override
    public String toString() {
        return this.digest + " (" + this.digestAlgorithm + ")";
    }

    @Override
    public String toJson() {
        final JsonBuilder b = new JsonBuilder();
        b.startObject();
        b.appendObjectProperty("digest", this.digest);
        b.appendObjectProperty("digestAlgorithm", this.digestAlgorithm.toString());
        b.appendObjectProperty("computedFrom", this.computedFrom);
        b.appendObjectProperty("computedFromType", this.computedFromType.toString());
        b.endObject();
        return b.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((digest == null) ? 0 : digest.hashCode());
        result = prime * result + ((digestAlgorithm == null) ? 0 : digestAlgorithm.hashCode());
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
        NodejsConstructDigest other = (NodejsConstructDigest) obj;
        if (digest == null) {
            if (other.digest != null)
                return false;
        } else if (!digest.equals(other.digest))
            return false;
        if (digestAlgorithm != other.digestAlgorithm)
            return false;
        return true;
    }
}
