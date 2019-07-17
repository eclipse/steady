package com.sap.psr.vulas.python.pip;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.python.ProcessWrapperException;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;

/**
 * <p>PyWrapper class.</p>
 *
 */
public class PyWrapper {
	
	private final static Log log = LogFactory.getLog(PyWrapper.class);
	
	private Path pathToPython = null;
	
	private Path logDir = null;
	
	/**
	 * Assumes that the python executable is part of the PATH environment variable.
	 *
	 * @throws com.sap.psr.vulas.python.ProcessWrapperException if any.
	 */
	public PyWrapper() throws ProcessWrapperException { this(Paths.get("python"), null); }
	
	/**
	 * Creates a new wrapper for the python executable at the given path.
	 *
	 * @param _path_to_python a {@link java.nio.file.Path} object.
	 * @param _log_dir a {@link java.nio.file.Path} object.
	 * @throws com.sap.psr.vulas.python.ProcessWrapperException if any.
	 */
	public PyWrapper(Path _path_to_python, Path _log_dir) throws ProcessWrapperException {
		this.pathToPython = _path_to_python;
		if(_log_dir!=null)
			this.logDir = _log_dir;
		else {
			try {
				FileUtil.createTmpDir("vulas-pip-");
			} catch (IOException e) {
				throw new ProcessWrapperException("Cannot create tmp directory: " + e.getMessage());
			}
		}
	}

	/**
	 * <p>isAvailable.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isAvailable() {
		boolean exists = false;
		try {
			final Process p = new ProcessBuilder(this.pathToPython.toString()).start();
			final int exit_code = p.waitFor();
			exists = exit_code==0;
		} catch(IOException ioe) {
			log.error("Error calling [python]: " + ioe.getMessage(), ioe);
		}
		catch(InterruptedException ie) {
			log.error("Error calling [python]: " + ie.getMessage(), ie);
		}
		return exists;
	}
	
	/**
	 * Calls pip list and pip show <package> in order to create and return all {@link PipInstalledPackages} of the Python environment.
	 *
	 * @param _script a {@link java.nio.file.Path} object.
	 * @param _args a {@link java.util.List} object.
	 * @return a int.
	 */
	public int runScript(Path _script, List<String> _args) {
		int exit_code = -1;
		
		// Complete command line call
		final List<String> list = new ArrayList<String>();
		list.add(this.pathToPython.toString());
		list.add(_script.toString());
		list.addAll(_args);
		
		final String script_name = _script.getFileName().toString();
		
		try {
			// Perform call			
			final ProcessBuilder pb = new ProcessBuilder(list);
			
			// Create temp. directory for out and err streams
			final Path out = Paths.get(logDir.toString(), "python-" + script_name + "-out.txt");
			final Path err = Paths.get(logDir.toString(), "python-" + script_name + "-err.txt");
						
			// Redirect out and err
			pb.redirectOutput(out.toFile());
			pb.redirectError(err.toFile());
			
			// Start and wait
			final Process process = pb.start();
			exit_code = process.waitFor();
			
			// Success: Parse output and call pip show <package>
			if(exit_code!=0) {
				final String error_msg = FileUtil.readFile(err);
				log.error("Error calling [python " + StringUtil.join(list, " ") + "]: " + error_msg);
			}			
		} catch(IOException ioe) {
			log.error("Error calling [python " + StringUtil.join(list, " ") + "]: " + ioe.getMessage());
		}
		catch(InterruptedException ie) {
			log.error("Error calling [python " + StringUtil.join(list, " ") + "]: " + ie.getMessage());
		}
		return exit_code;
	}
}
