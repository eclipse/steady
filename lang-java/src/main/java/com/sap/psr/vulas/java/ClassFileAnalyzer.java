package com.sap.psr.vulas.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.monitor.ClassPoolUpdater;
import com.sap.psr.vulas.monitor.ClassVisitor;
import com.sap.psr.vulas.shared.util.FileUtil;

import javassist.ClassPool;
import javassist.CtClass;

/**
 * Analyzes class files.
 */
public class ClassFileAnalyzer implements FileAnalyzer {

	private static final Log log = LogFactory.getLog(ClassFileAnalyzer.class);

	/** The file to be analyzed. */
	private File file = null;

	/** All Java constructs found in the given class file. */
	private Map<ConstructId, Construct> constructs = null;
	
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "class" };
	}
	
	@Override
	public boolean canAnalyze(File _file) {
		final String ext = FileUtil.getFileExtension(_file);
		if(ext == null || ext.equals(""))
			return false;
		for(String supported_ext: this.getSupportedFileExtensions()) {
			if(supported_ext.equalsIgnoreCase(ext))
				return true;
		}
		return false;
	}

	@Override
	public void analyze(final File _file) throws FileAnalysisException {
		this.setFile(_file);
	}
	
	public void setFile(File _file) throws IllegalArgumentException {
		final String ext = FileUtil.getFileExtension(_file);
		if(!ext.equals("class"))
			throw new IllegalArgumentException("Expected a class file but got [" + _file + "]");
		if(!FileUtil.isAccessibleFile(_file.toPath()))
			throw new IllegalArgumentException("Cannot open file [" + _file + "]");
		this.file = _file;
	}

	@Override
	public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
		if(this.constructs==null) {
			try {
				this.constructs = new TreeMap<ConstructId, Construct>();

				// Create a Javassist representation of the class file
				final CtClass ctclass;
				try (final FileInputStream fis = new FileInputStream(this.file)) {
					ctclass = ClassPool.getDefault().makeClass(fis);
				}

				// Update default class pool so that other classes are also found (e.g., the declaring class of nested classes when constructing the ClassVisitor)
				//TODO HP, 4.4.16: This does not yet seem to work
				ClassPoolUpdater.getInstance().updateClasspath(ctclass, this.file);

				// Only add constructs of ctclass is either a class or enum (no interfaces)
				if(ctclass.isInterface()) {
					ClassFileAnalyzer.log.debug("Interface [" + ctclass.getName() + "] skipped");
				}
				else {
					// Use a class visitor to get all constructs from the class file
					final ClassVisitor cv = new ClassVisitor(ctclass);
					final Set<ConstructId> temp_constructs = cv.getConstructs();

					// Add all constructs with a "" body
					// TODO: Change Construct so that string (for source files) and binary bodies (file compiled classes) can be covered
					for(ConstructId c: temp_constructs)
						this.constructs.put(c, new Construct(c, ""));
				}
			} catch (FileNotFoundException e) {
				throw new FileAnalysisException(e.getMessage(), e);
			} catch (IOException e) {
				throw new FileAnalysisException("IO exception while analysing class file [" + this.file.getName() + "]: " + e.getMessage(), e);
			} catch (RuntimeException e) {
				throw new FileAnalysisException("Runtime exception while analysing class file [" + this.file.getName() + "]: " + e.getMessage(), e);
			}
		}
		return this.constructs;
	}

	@Override
	public boolean containsConstruct(ConstructId _id) throws FileAnalysisException { return this.getConstructs().containsKey(_id); }

	@Override
	public Construct getConstruct(ConstructId _id) throws FileAnalysisException { return this.getConstructs().get(_id); }
	
	@Override
	public boolean hasChilds() { return false; }
	
	@Override
	public Set<FileAnalyzer> getChilds(boolean _recursive) { return null; }
}
