package com.sap.psr.vulas.cia.model.pypi;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiRelease {
	
	String upload_time;
	String python_version;
	String url;
	String md5_digest;
	String filename;
	
	public String getUpload_time() {
		return upload_time;
	}
	public void setUpload_time(String upload_time) {
		this.upload_time = upload_time;
	}
	public String getPython_version() {
		return python_version;
	}
	public void setPython_version(String python_version) {
		this.python_version = python_version;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMd5_digest() {
		return md5_digest;
	}
	public void setMd5_digest(String md5_digest) {
		this.md5_digest = md5_digest;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getPackagetype() {
		return packagetype;
	}
	public void setPackagetype(String packagetype) {
		this.packagetype = packagetype;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	String packagetype;
	String path;
	long size;
}
