package com.sap.psr.vulas.cia.model.pypi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiInfo {
	
	String maintainer_email;
	
	String package_url;
	
	String author;
	
	String author_email;
	
	String download_url;
	
	String version;
	
	String release_url;
	
	List<String> classifiers;
	
	String name;
	
	String bugtrack_url;
	
	String license;
	
	String summary;
	
	String home_page;

	public String getMaintainer_email() {
		return maintainer_email;
	}

	public void setMaintainer_email(String maintainer_email) {
		this.maintainer_email = maintainer_email;
	}

	public String getPackage_url() {
		return package_url;
	}

	public void setPackage_url(String package_url) {
		this.package_url = package_url;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor_email() {
		return author_email;
	}

	public void setAuthor_email(String author_email) {
		this.author_email = author_email;
	}

	public String getDownload_url() {
		return download_url;
	}

	public void setDownload_url(String download_url) {
		this.download_url = download_url;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRelease_url() {
		return release_url;
	}

	public void setRelease_url(String release_url) {
		this.release_url = release_url;
	}

	public List<String> getClassifiers() {
		return classifiers;
	}

	public void setClassifiers(List<String> classifiers) {
		this.classifiers = classifiers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBugtrack_url() {
		return bugtrack_url;
	}

	public void setBugtrack_url(String bugtrack_url) {
		this.bugtrack_url = bugtrack_url;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getHome_page() {
		return home_page;
	}

	public void setHome_page(String home_page) {
		this.home_page = home_page;
	}
	
	
	
	

}
