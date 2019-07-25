package com.sap.psr.vulas.nodejs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.util.FileUtil;

public class NodejsPackageAnalyzer implements FileAnalyzer {

    private static final Log log = LogFactory.getLog(NodejsPackageAnalyzer.class);

    private File archive = null;

    private Map<ConstructId, Construct> constructs = null;
    private File pack_path = null;

    @Override
    public String[] getSupportedFileExtensions() {
        return new String[] {"js"};
    }

    @Override
    public boolean canAnalyze(File _file) {
        final Path pack_json = Paths.get(_file.toString(), "package.json");
        return FileUtil.isAccessibleFile(pack_json);
    }

    @Override
    public void analyze(File _file) throws FileAnalysisException {
        if(!FileUtil.isAccessibleDirectory(_file))
            throw new IllegalArgumentException("[" + _file + "] does not exist or is not readable");
        this.pack_path = _file;
    }

    @Override
    public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
        if(this.constructs == null) {
            this.constructs = new TreeMap<>();
            final Queue<File> dir_queue = new LinkedList<File>();
            dir_queue.add(this.pack_path);
            final NodejsId pack_id = new NodejsId(null, NodejsId.Type.PACKAGE, this.pack_path.getName());
            final Construct pack_con = new Construct(pack_id, "");
            this.constructs.put(pack_id, pack_con);

            // BFS in package directory
            while (!dir_queue.isEmpty()) {
                final File current_dir = dir_queue.remove();
                for (File file : current_dir.listFiles()) {
                    // Enqueue new directory
                    if (file.isDirectory()) {
                        // Skip node_modules directory
                        if (!file.getName().equalsIgnoreCase("node_modules")) {
                            dir_queue.add(file);
                        }
                    }
                    // Analyze .js file
                    else {
                        final String ext = FileUtil.getFileExtension(file);
                        for(String supported_ext: this.getSupportedFileExtensions()) {
                            if(supported_ext.equalsIgnoreCase(ext)) {
                                final FileAnalyzer analyzer = FileAnalyzerFactory.buildFileAnalyzer(file);
                                this.constructs.putAll(analyzer.getConstructs());
                            }
                        }
                    }
                }
            }
        }
        return this.constructs;
    }

    @Override
    public boolean containsConstruct(ConstructId _id) throws FileAnalysisException {
        return this.getConstructs().containsKey(_id);
    }

    @Override
    public Construct getConstruct(ConstructId _id) throws FileAnalysisException {
        return this.getConstructs().get(_id);
    }

    @Override
    public boolean hasChilds() {
        return false;
    }

    @Override
    public Set<FileAnalyzer> getChilds(boolean _recursive) {
        return null;
    }

    public List<com.sap.psr.vulas.shared.json.model.ConstructId> getSharedConstructs() throws FileAnalysisException {
        List<com.sap.psr.vulas.shared.json.model.ConstructId> l= new ArrayList<com.sap.psr.vulas.shared.json.model.ConstructId>();
        for(ConstructId c: this.getConstructs().keySet()) {
            l.add(new com.sap.psr.vulas.shared.json.model.ConstructId(ProgrammingLanguage.JS, c.getSharedType(),c.getQualifiedName()));
        }
        return l;
    }
}
