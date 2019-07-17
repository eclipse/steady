package com.sap.psr.vulas.python;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;

/**
 * Wraps the two Python File Analyzers into one.
 */
public class PythonFileAnalyzer implements FileAnalyzer {

	private final static Log log = LogFactory.getLog(PythonFileAnalyzer.class);

	private FileAnalyzer analyzer = null;

	private File file = null;

	private Map<ConstructId, Construct> constructs = null;

	/** {@inheritDoc} */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "py" };
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public void analyze(final File _file) throws FileAnalysisException {
		if(!FileUtil.isAccessibleFile(_file.toPath()))
			throw new IllegalArgumentException("[" + _file + "] does not exist or is not readable");
		this.file = _file;
	}

	/**
	 * Returns true if the top-most element of the stack is of type {@link ConstructType#MODU}, false otherwise.
	 */
	static boolean isTopOfType(Stack<PythonId> _context, PythonId.Type _type) {
		if(_context==null)
			throw new IllegalArgumentException("Stack argument is null");
		if(_context.isEmpty())
			return false;
		final PythonId id = (PythonId)_context.peek();
		return id.getType().equals(_type);
	}

	/**
	 * Returns true if the top-most element of the stack is of any of the given {@link PythonId#Type}s, false otherwise.
	 */
	static boolean isTopOfType(Stack<PythonId> _context, PythonId.Type[] _types) {
		if(_context==null)
			throw new IllegalArgumentException("Stack argument is null");
		if(_context.isEmpty())
			return false;
		final PythonId id = (PythonId)_context.peek();
		for(PythonId.Type t: _types)
			if(id.getType().equals(t))
				return true;
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
		if(this.constructs==null) {
			analyzer = PythonFileAnalyzer.createAnalyzer(this.file);
			analyzer.analyze(this.file);
			this.constructs = analyzer.getConstructs();
		}		
		return analyzer.getConstructs();
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsConstruct(ConstructId _id) throws FileAnalysisException {
		return this.getConstructs().containsKey(_id);
	}

	/** {@inheritDoc} */
	@Override
	public Construct getConstruct(ConstructId _id) throws FileAnalysisException {
		return this.getConstructs().get(_id);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The nested {@link Python3FileAnalyzer} is completely hidden.
	 */
	@Override
	public boolean hasChilds() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The nested {@link Python3FileAnalyzer} is completely hidden.
	 */
	@Override
	public Set<FileAnalyzer> getChilds(boolean _recursive) {
		return null;
	}

	/**
	 * Creates a {@link PythonId} of type {@link ConstructType#MODU} for the given py file.
	 *
	 * @param _file a {@link java.io.File} object.
	 * @return a {@link com.sap.psr.vulas.python.PythonId} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public static PythonId getModule(File _file) throws IllegalArgumentException {
		if(!FileUtil.hasFileExtension(_file.toPath(), new String[] { "py" })) {
			throw new IllegalArgumentException("Expected file with file extension [py], got [" + _file.toString() + "]");
		}

		final Path p = _file.toPath().toAbsolutePath();

		// Add file name w/o extension to qname components
		final String module_name = FileUtil.getFileName(p.toString(), false);

		// Search upwards until there's no __init__.py anymore, and add directory names to the qname components
		final List<String> package_name = new ArrayList<String>();
		Path search_path = p.getParent();
		while(DirUtil.containsFile(search_path.toFile(), "__init__.py") && search_path.getNameCount() > 1) {
			package_name.add(0, search_path.getFileName().toString());
			search_path = search_path.getParent();
		}

		// Create the package (if any), the module and return the latter
		PythonId pack = null;
		if(!package_name.isEmpty())
			pack = new PythonId(null, PythonId.Type.PACKAGE, StringUtil.join(package_name, "."));
		return new PythonId(pack, PythonId.Type.MODULE, module_name);
	}
	
	/**
	 * Checks for syntax that is specific to Python2.
	 * 
	 * TODO: To be completed according to https://docs.python.org/release/3.0.1/whatsnew/3.0.html.
	 *
	 * As of today, the method searches for the following:
	 * - print statement becomes a function: print "bla" --> print("bla")
	 * - raw_input does not exist anymore: raw_input() --> input("bla: ")
	 */
	final static Pattern[] PY2_PATTERNS = new Pattern[] { Pattern.compile("^\\s*print\\s+\".*$"), Pattern.compile("^.*raw_input\\(.*$") };

	final static Pattern[] PY35_ASYNC_PATTERNS = new Pattern[] { Pattern.compile("^.*async\\s*def.*$") };
	
	final static Pattern[] COMMENT_PATTERNS = new Pattern[] { Pattern.compile("^\\s*#.*$") };
	
	/**
	 * <p>createAnalyzer.</p>
	 *
	 * @param _file a {@link java.io.File} object.
	 * @return a {@link com.sap.psr.vulas.FileAnalyzer} object.
	 */
	public static FileAnalyzer createAnalyzer(final File _file) { 
		try(final InputStream is = new FileInputStream(_file)) {
			return PythonFileAnalyzer.createAnalyzer(is);
		} catch(IOException e) {
			log.error(e.getClass().getSimpleName() + " when creating analyzer for file [" + _file.toPath().toAbsolutePath() + "]: " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Reads the input stream line by line in order to decide which {@link FileAnalyzer} to take.
	 * Defaults to {@link PythonAnalyzer335}.
	 *
	 * @param _is a {@link java.io.InputStream} object.
	 * @return a {@link com.sap.psr.vulas.FileAnalyzer} object.
	 * @throws java.io.IOException if any.
	 */
	public static FileAnalyzer createAnalyzer(InputStream _is) throws IOException {
		FileAnalyzer fa = null;
		final BufferedReader isr = new BufferedReader(new InputStreamReader(_is));
		String line = null;
		int line_count = 0;
		while( (line=isr.readLine())!=null ) {
			line_count++;
			// No comment
			if(!StringUtil.matchesPattern(line, COMMENT_PATTERNS)) {
				// Py2
				if(StringUtil.matchesPattern(line, PY2_PATTERNS)) {
					log.info("Found one of the Python 2 patterns [" + StringUtil.join(PY2_PATTERNS, ", ") + "] in line [" + line_count + "]: " + line.trim());
					fa = new Python335FileAnalyzer();
					break;
				}
				// Py 35+
				else if(StringUtil.matchesPattern(line, PY35_ASYNC_PATTERNS)) {
					log.info("Found one of the Python 3.5 patterns [" + StringUtil.join(PY35_ASYNC_PATTERNS, ", ") + "] in line [" + line_count + "]: " + line.trim());
					fa = new Python3FileAnalyzer();
					break;
				}
			}
		}
		
		// Default to 335
		if(fa==null)
			fa = new Python335FileAnalyzer();
		
		return fa;
	}
}
