package com.sap.psr.vulas.test;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ArchivePrinter {
	
	private Path archive = null;
	private InputStream is = null;
		
	public ArchivePrinter(Path _a) throws FileNotFoundException {
		this(new FileInputStream(_a.toFile()));
		this.archive = _a;
	}
	
	public ArchivePrinter(InputStream _i) {
		this.is = _i;
	}
	
	/*
	 * Using httpclient-4.3.jar
	 * Expected: Not reachable, not traced.
	 */
/*	public void httpRequest1(String _url) {
		
		// The underlying HTTP connection is still held by the response object
		// to allow the response content to be streamed directly from the network socket.
		// In order to ensure correct deallocation of system resources
		// the user MUST call CloseableHttpResponse#close() from a finally clause.
		// Please note that if response content is not fully consumed the underlying
		// connection cannot be safely re-used and will be shut down and discarded
		// by the connection manager. 
		BasicHttpResponse response1 = null;
		try {
			// Set proxy
			RequestConfig config = RequestConfig.custom()
                    .setProxy(new HttpHost("proxy", 8080, "http"))
                    .build();
			
			// Do the request
			CloseableHttpClient httpclient = HttpClients.createDefault();
			
			HttpGet httpGet = new HttpGet(_url);
			httpGet.setConfig(config);
			
			response1 = httpclient.execute(httpGet);
		    System.out.println(response1.getStatusLine());
		    HttpEntity entity1 = response1.getEntity();
		    EntityUtils.consume(entity1);
		} catch(Exception e) {
			System.err.println("Exception: " + e.getMessage());
			e.printStackTrace();
		} finally {
		    try {
				if(response1!=null)
					response1.close();
			} catch (IOException e) {
				System.err.println("Exception: " + e.getMessage());
			}
		}

	}
*/
	/**
	 * Using httpClient-4.1.3.jar; adding a x509TrustManager to SSL context
	 * Expected: traced; but not reachable with wala X-CFA; reachable with wala RTA
	 */
	public void httpRequest2 (String _url) throws Exception {
    	//build a trust manager, which checks if given certificates are valid. Here accept all certificates, never throw an exception
    	X509TrustManager tm = new X509TrustManager() {
    		 
    		public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
    		}
    		 
    		public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
    		}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub
				
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub
				
			}

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
    	};


		SSLContext sslcontext = SSLContext.getInstance("SSL");
		sslcontext.init(null, new TrustManager[]{tm}, null);
		
		SSLSocketFactory sslsf = new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);    
		SchemeRegistry schemeRegistry = new SchemeRegistry();
	    schemeRegistry.register(new Scheme("http",new PlainSocketFactory(), 80));
	    schemeRegistry.register(new Scheme("https",sslsf, 443));

	    ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(schemeRegistry);
        HttpClient client = new DefaultHttpClient(manager);
        HttpGet httpGet = new HttpGet(_url);
        try {
			client.execute(httpGet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Using httpClient-4.3.jar; setting system default ssl context
	 * Expected: reachable with all configurations; but not traced
	 * @param _url
	 */
/*	public void httpRequest3(String _url) {
		SSLContext sslContext = SSLContexts.createSystemDefault();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);      
		Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
		        .register("https", sslsf)
		        .register("http", new PlainConnectionSocketFactory()) 
		        .build();
		HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);
		CloseableHttpClient httpclient = HttpClientBuilder.create()
				.setConnectionManager(cm)
				.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
				.build();  
		try {
			httpclient.execute(new HttpGet(_url));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	public void compressArchive() {
		try {
			FileOutputStream fos = new FileOutputStream(Paths.get(this.archive + ".bz2").toFile());
			BZip2CompressorOutputStream bzos = new BZip2CompressorOutputStream(fos);
			bzos.write(new String("Foo bar").getBytes());
			bzos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Test method with generic classes as parameters. The diamond <> should not appear in the method signature, i.e., the
	 * method's qualified name should be "com.sap.psr.vulas.test.ArchivePrinter.testGenerics1(Set,int,Map)".
	 */
	private void testGenericsAsParameters(Set<String> _set, int _int, Map<String,Object> _map) {}
	
	/**
	 * Non-static inner class (outer class will be added by compiler as first argument to constructor).
	 */
	private class InnerNonStaticClass {
		InnerNonStaticClass(String _string) {}
	}
	
	/**
	 * Static inner class.
	 */
	private static class InnerStaticClass {
		InnerStaticClass(String _string) {}
	}
	
	/**
	 * Test method with inner classes as parameters. The outer class should not appear in the method signature.
	 */
	private void testInnerClassesAsParameters(InnerNonStaticClass _foo, InnerStaticClass _bar) {}
	
	public void printEntries(PrintStream _out) throws FileNotFoundException {
		try {
			final BufferedInputStream bis = new BufferedInputStream(this.is);
			ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(bis);
			ArchiveEntry entry = null;
			while( (entry=input.getNextEntry())!=null ) {
				_out.println(entry.getName());
				
				// If Excel sheet, use Apache POI to open it (it'll always fail, since we do not extract the file, but that does not matter)
				if(entry.getName().endsWith("xls") || entry.getName().endsWith("xslx")) {
					try {
						this.openSpreadsheet(Paths.get(entry.getName()));
					} catch(Exception e) {
						System.err.println("Exception while opening [" + entry.getName() + "]: " + e.getMessage());
					}
				}
			}
			input.close();
			bis.close();
		} catch(FileNotFoundException e) {
			throw e;
		} catch(Exception e) {
			System.err.println("Exception while reading entries from [" + this.archive + "]: " + e.getMessage());
		}
	}
	
	/**
	 * It reads data from a an input file, then write and compress to a bzip2 file using BZip2CompressorOutputStream
	 * If a special input file with repeating inputs is provided, the vulnerability in BZip2CompressorOutputStream will result in endless writing and consuming lots of resources
	 * Please refer to https://web.nvd.nist.gov/view/vuln/detail?vulnId=CVE-2012-2098
	 * @param _in
	 * @param _out
	 * @throws Exception
	 */
	public static void compressExploitability(Path _in, Path _out) throws Exception {
		FileInputStream fin = new FileInputStream(_in.toString());
		BufferedInputStream in = new BufferedInputStream(fin);
		BZip2CompressorOutputStream out = new BZip2CompressorOutputStream(new FileOutputStream(_out.toString()));
		BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
		final byte[] buffer = new byte[1024*10];
		int n = 0;
		while (-1 != (n = bzIn.read(buffer))) {
		    out.write(buffer, 0, n);
		}
		out.close();
		bzIn.close();
	}
	
	/**
	 * It read an excel file from the Path parameter and output the number of lines in the excel file
	 * if it's a normal well-formatted excel file, it will correctly print the number of lines
	 * if it's a special excel file in which the sharedStrings.xml has been modified, it will go into an endless loop and result in OutOfMemory
	 * Modify the sharedStrings.xml to a repeating format like <!ENTITY lol1 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
	 * @param _p
	 * @throws Exception
	 */
	public static void openSpreadsheet(Path _p) throws Exception {
		XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(_p.toString()));
		Sheet sheet = wb.getSheetAt(0);   
		System.out.println(sheet.getPhysicalNumberOfRows());
	}
	
	public int countEntries() throws FileNotFoundException {
		int count = 0;
		try {
			final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(archive.toFile()));
			ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(bis);
			while( input.getNextEntry()!=null ) count++;
			input.close();
			bis.close();
		} catch(FileNotFoundException e) {
			throw e;
		} catch(Exception e) {
			System.err.println("Exception while reading entries from [" + this.archive + "]: " + e.getMessage());
		}
		return count;
	}

	/**
	 * Reads a given archive (of whatever format supported by commons compress) and prints all files to the console.
	 * @param args
	 */
	public static void main(String[] _args) {
		try {
			final ArchivePrinter p = new ArchivePrinter(Paths.get(_args[0]));
			p.printEntries(System.out);
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + _args[0]);
		}
	}
}
