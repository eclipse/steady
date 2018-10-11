package com.sap.psr.vulas.python;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;

public class ProcessWrapper implements Runnable {
	
	private static Log log = LogFactory.getLog(ProcessWrapper.class);
	
	private static final Pattern ALLOWED = Pattern.compile("[\\.\\-\\w=]+");
	
	private String id = null;
	
	private Path exe = null;
	
	private String[] args = null;
	
	private Path outPath = null;
	
	private Path outFile = null;
	
	private Path errFile;
	
	private int exitCode = -1;
	
	public ProcessWrapper() { 
		this.id = StringUtil.getRandonString(10);
	}
	
	public ProcessWrapper(String _id) {
		this.id = _id;
	}
	
	public String getId() { return this.id; }
	
	public ProcessWrapper setCommand(Path _executable, String... _args) throws ProcessWrapperException {
		//if(_executable==null || FileUtil.isAccessibleFile(_executable))
		//	throw new ProcessWrapperException("Illegal executable [" + _executable + "]");
		
		for(int i=0; i<_args.length; i++) {
			final Matcher m = ALLOWED.matcher(_args[i]);
			if(!m.matches() && !FileUtil.isAccessibleFile(_args[i]) && !FileUtil.isAccessibleDirectory(_args[i]))
				throw new ProcessWrapperException("Illegal characters in argument [" + i + "], allowed are: a-zA-Z_0-9-.=");
		}
		
		this.exe = _executable;
		this.args = _args;
		return this;
	}
	
	public ProcessWrapper setPath(Path _p) {
		this.outPath = _p;
		return this;
	}

	@Override
	public void run() {		
		String name = null;
		if(FileUtil.isAccessibleFile(this.exe))
			name = this.exe.getFileName().toString();
		else if(this.exe.toString().indexOf(System.getProperty("file.separator"))!=-1)
			name = this.exe.toString().substring(this.exe.toString().lastIndexOf(System.getProperty("file.separator"))+1);		
		else
			name = this.exe.toString();	
		final String rnd = StringUtil.getRandonString(6);
		final String out_name = name + "-" + this.getId() + "-" + rnd + "-out.txt";
		final String err_name = name + "-" + this.getId() + "-" + rnd + "-err.txt";
		
		// Create temp. directory for out and err streams
		this.outFile = Paths.get(this.outPath.toString(), out_name);
		this.errFile = Paths.get(this.outPath.toString(), err_name);
		
		try {
			final ArrayList<String> cmd = new ArrayList<String>();
			cmd.add(this.exe.toString());
			cmd.addAll(Arrays.asList(this.args));
			final ProcessBuilder pb = new ProcessBuilder(cmd);
			
			// Redirect out and err
			pb.redirectOutput(this.outFile.toFile());
			pb.redirectError(this.errFile.toFile());

			// Start and wait
			final Process process = pb.start();
			this.exitCode = process.waitFor();

			if(this.exitCode!=0) {
				final String error_msg = FileUtil.readFile(this.errFile);
				log.error("Error running [" + this.getCommand() + "]: " + error_msg);
			}

		}
		catch(IOException ioe) {
			log.error("Error running [" + this.getCommand() + "]: " + ioe.getMessage());
		}
		catch(InterruptedException ie) {
			log.error("Error running [" + this.getCommand() + "]: " + ie.getMessage());
		}
	}

	public Path getOutFile() {
		return outFile;
	}

	public Path getErrFile() {
		return errFile;
	}

	public int getExitCode() {
		return exitCode;
	}
	
	public boolean terminatedWithSuccess() {
		return this.exitCode==0;
	}
	
	public String getCommand() {
		final ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(this.exe.toString());
		cmd.addAll(Arrays.asList(this.args));
		return StringUtil.join(cmd, " ");
	}
}
