package com.acme;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class ArchiveServlet extends HttpServlet {

	//private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();
	
	// Just to have a static initializer as part of the test app
	static String nonsense = null;
	static { nonsense = "Nonsense"; }

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		this.processRequest(request, response);
	}


	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		// Prepare file upload

		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// Configure a repository (to ensure a secure temp location is used)
		ServletContext servletContext = this.getServletConfig().getServletContext();
		File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
		factory.setRepository(repository);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = null;
		try {
			// Parse the request
			items = upload.parseRequest(request);
			FileItem item = null;
			ArchivePrinter printer = null;

			// Process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				item = iter.next();

				// Print the entries of an uploaded ZIP
				if (!item.isFormField()) {
					printer = new ArchivePrinter(item.getInputStream());
					printer.printEntries(new PrintStream(response.getOutputStream()));
				}
				else {
					//ArchiveServlet.log.info("Parameter [" + item.getFieldName() + "], value [" + item.getString() + "]");
					if(item.getFieldName().equals("note"))
						this.saveNote(item.getString());
				}
			}
		} catch (FileUploadException e) {
			//ArchiveServlet.log.error("Error uploading file: " + e.getMessage(), e);
		}
	}

	private void saveNote(String _note) throws IOException {
		// Save the notes in a tmp file
		final Path tmp = Files.createTempFile("note-", ".txt");
		final PrintWriter pw = new PrintWriter(new FileWriter(tmp.toFile()));
		pw.println(_note);
		pw.close();
		//ArchiveServlet.log.info("Note written to file [" + tmp.toAbsolutePath() + "]");
	}
}
