package com.sap.psr.vulas.cia.util;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.sap.psr.vulas.cia.model.mavenCentral.MavenVersionsSearch;
import com.sap.psr.vulas.cia.model.mavenCentral.ResponseDoc;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class ArtifactDownloader {

	

	private static Logger log = LoggerFactory.getLogger(ArtifactDownloader.class);


	static class DefaultRequestCallback implements RequestCallback {
		@Override
		public void doWithRequest(ClientHttpRequest request) throws IOException {}
	}

	static class FileResponseExtractor implements ResponseExtractor<Path> {

		private Artifact artifact = null;
		private Path file = null;

		FileResponseExtractor(Artifact _doc, Path _file) {
			this.artifact = _doc;
			this.file = _file;
		}

		@Override
		public Path extractData(ClientHttpResponse response) throws IOException {
			// In case of 200, write the bytes to disk
			if(response.getStatusCode().equals(HttpStatus.OK)) {
				try(final FileOutputStream fos = new FileOutputStream(this.file.toFile())){
					byte[] bytes = new byte[1024];
					int buffer_length = 0;
					try(final BufferedInputStream is = new BufferedInputStream(response.getBody())){ 
						while( (buffer_length=is.read(bytes, 0, 1024)) > 0 ) {
							fos.write(bytes, 0, buffer_length);
							fos.flush();
						}
						fos.flush();
						fos.close();
					}
				}
				return this.file;
			}
			else {
				log.error("Error " + response.getRawStatusCode() + " when retrieving " + this.artifact.getLibId() + ": " + response.getStatusText());
				return null;
			}
		}		
	}
}
