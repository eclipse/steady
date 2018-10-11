package com.sap.psr.vulas.cg;

import com.ibm.wala.util.graph.*;
import com.sap.psr.vulas.*;
import com.ibm.wala.util.graph.impl.*;
import com.sap.psr.vulas.java.*;
import java.util.*;
import java.io.*;
import org.apache.commons.logging.*;

public class Callgraph
{
    private static final Log log;
    private Graph<ConstructId> graph;
    private Graph<String> graph_qname;
    public final List<String> nodeId;
    private static final int MAX_EDGES = 50;
    
    public Callgraph(final Graph<ConstructId> _g) {
        super();
        graph = null;
        graph_qname = (Graph<String>)SlowSparseNumberedGraph.make();
        nodeId = new ArrayList<String>();
        graph = _g;
        final Iterator<ConstructId> iter = (Iterator<ConstructId>)_g.iterator();
        ConstructId src_node = null;
        ConstructId tgt_node = null;
        String src_qname = null;
        String tgt_qname = null;
        while (iter.hasNext()) {
            src_node = iter.next();
            src_qname = src_node.getQName();
            if (!graph_qname.containsNode((Object)src_qname)) {
                graph_qname.addNode((Object)src_qname);
                nodeId.add(src_qname);
            }
            final Iterator<ConstructId> succNodes = (Iterator<ConstructId>)_g.getSuccNodes((Object)src_node);
            while (succNodes.hasNext()) {
                tgt_node = succNodes.next();
                tgt_qname = tgt_node.getQName();
                if (!graph_qname.containsNode((Object)tgt_qname)) {
                    graph_qname.addNode((Object)tgt_qname);
                    nodeId.add(tgt_qname);
                }
                if (!graph_qname.hasEdge((Object)src_qname, (Object)tgt_qname)) {
                    graph_qname.addEdge((Object)src_qname, (Object)tgt_qname);
                }
            }
        }
        log.info("Built Graph<ConstructId/qname> of " + nodeId.size() + " nodes");
    }
    
    public static JavaPackageId getPackageId(final ConstructId _cid) {
        JavaPackageId pid = null;
        if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaConstructorId")) {
            final JavaConstructorId jcid = (JavaConstructorId)_cid;
            pid = jcid.getJavaClassContext().getJavaPackageContext();
        }
        else if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaMethodId")) {
            final JavaMethodId mid = (JavaMethodId)_cid;
            pid = mid.getJavaClassContext().getJavaPackageContext();
        }
        else if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaClassId")) {
            final JavaClassId classid = (JavaClassId)_cid;
            pid = classid.getJavaPackageContext();
        }
        else if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaPackageId")) {
            pid = (JavaPackageId)_cid;
        }
        else if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaClassInit")) {
            final JavaClassInit init = (JavaClassInit)_cid;
            pid = init.getJavaClassContext().getJavaPackageContext();
        }
        else {
            log.error("Cannot find the package id for construct " + _cid.getQName());
        }
        return pid;
    }
    
    public static JavaClassId getClassId(final ConstructId _cid) {
        JavaClassId classid = null;
        if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaConstructorId")) {
            final JavaConstructorId jcid = (JavaConstructorId)_cid;
            classid = jcid.getJavaClassContext();
        }
        else if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaClassInit")) {
            final JavaClassInit init = (JavaClassInit)_cid;
            classid = init.getJavaClassContext();
        }
        else if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaMethodId")) {
            final JavaMethodId mid = (JavaMethodId)_cid;
            classid = mid.getJavaClassContext();
        }
        else if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaClassId")) {
            classid = (JavaClassId)_cid;
        }
        else if (_cid.getClass().getName().equals("com.sap.psr.vulas.java.JavaPackageId")) {
            log.error("Cannot find the class id as " + _cid.getQName() + " is a package");
        }
        else {
            log.error("Cannot find the class id for construct " + _cid.getQName());
        }
        return classid;
    }
    
    public Map<String, Integer> getDist(final String _tgt) {
        final Iterator<String> nodes = (Iterator<String>)graph_qname.iterator();
        final Map<String, Integer> dist = new HashMap<String, Integer>();
        String node = null;
        while (nodes.hasNext()) {
            node = nodes.next();
            if (node.equals(_tgt)) {
                dist.put(node, 0);
            }
            else {
                dist.put(node, -1);
            }
        }
        return this.computeDist(_tgt, dist);
    }
    
    private Map<String, Integer> computeDist(final String _tgt, Map<String, Integer> _dist) {
        final Iterator<String> predNodes = (Iterator<String>)graph_qname.getPredNodes((Object)_tgt);
        String prednode = null;
        final int l = _dist.get(_tgt) + 1;
        while (predNodes.hasNext()) {
            prednode = predNodes.next();
            if (_dist.get(prednode) == -1 || _dist.get(prednode) > l) {
                _dist.put(prednode, l);
                _dist = this.computeDist(prednode, _dist);
            }
        }
        return _dist;
    }
    
    public Map<String, LinkedList<Integer>> getShortestPath(final String _tgt) {
        final Iterator<String> nodes = (Iterator<String>)graph_qname.iterator();
        final Map<String, LinkedList<Integer>> path = new HashMap<String, LinkedList<Integer>>();
        String node = null;
        while (nodes.hasNext()) {
            node = nodes.next();
            if (node.equals(_tgt)) {
                path.put(node, new LinkedList<Integer>());
            }
            else {
                path.put(node, null);
            }
        }
        return this.computeShortestPath(_tgt, path);
    }
    
    private Map<String, LinkedList<Integer>> computeShortestPath(final String _tgt, Map<String, LinkedList<Integer>> _path) {
        final Iterator<String> predNodes = (Iterator<String>)graph_qname.getPredNodes((Object)_tgt);
        String prednode = null;
        final LinkedList<Integer> newPath = new LinkedList<Integer>();
        for (final Integer i : _path.get(_tgt)) {
            newPath.add(i);
        }
        newPath.add(nodeId.indexOf(_tgt));
        final int l = newPath.size();
        while (predNodes.hasNext()) {
            prednode = predNodes.next();
            if (_path.get(prednode) == null || _path.get(prednode).size() > l) {
                _path.put(prednode, newPath);
                _path = this.computeShortestPath(prednode, _path);
            }
        }
        return _path;
    }
    
    public Map<String, HashSet<Short>> getAllEdges(final String _tgt) {
        final Iterator<String> nodes = (Iterator<String>)graph_qname.iterator();
        final Map<String, HashSet<Short>> edges = new HashMap<String, HashSet<Short>>();
        String node = null;
        while (nodes.hasNext()) {
            node = nodes.next();
            if (node.equals(_tgt)) {
                edges.put(node, new HashSet<Short>());
            }
            else {
                edges.put(node, null);
            }
        }
        return this.computeAllEdges(_tgt, edges);
    }
    
    private Map<String, HashSet<Short>> computeAllEdges(final String _tgt, Map<String, HashSet<Short>> _edges) {
        final Iterator<String> predNodes = (Iterator<String>)graph_qname.getPredNodes((Object)_tgt);
        String prednode = null;
        final short tgtid = (short)nodeId.indexOf(_tgt);
        while (predNodes.hasNext()) {
            prednode = predNodes.next();
            HashSet<Short> newedge = _edges.get(prednode);
            if (newedge == null) {
                newedge = new HashSet<Short>();
            }
            if (!newedge.contains(tgtid)) {
                newedge.add(tgtid);
                _edges.put(prednode, newedge);
                _edges = this.computeAllEdges(prednode, _edges);
            }
        }
        return _edges;
    }
    
    public HashSet<LinkedList<Short>> assembleAllPaths(final String _src, final String _tgt, final Map<String, HashSet<Short>> _alledges) {
        final LinkedList<Short> singlepath = new LinkedList<Short>();
        final HashSet<LinkedList<Short>> allpaths = new HashSet<LinkedList<Short>>();
        return this.assemblePath(_src, _tgt, _alledges, singlepath, allpaths);
    }
    
    private HashSet<LinkedList<Short>> assemblePath(final String _src, final String _tgt, final Map<String, HashSet<Short>> _alledges, final LinkedList<Short> _path, HashSet<LinkedList<Short>> _allpaths) {
        if (_src.equals(_tgt)) {
            final LinkedList<Short> newPath = new LinkedList<Short>();
            for (final short i : _path) {
                newPath.add(i);
            }
            _allpaths.add(newPath);
            _path.removeLast();
            return _allpaths;
        }
        final HashSet<Short> edges = _alledges.get(_src);
        if (edges != null) {
            for (final short i : edges) {
                if (!_path.contains(i) && _path.size() < 50) {
                    _path.add(i);
                    _allpaths = this.assemblePath(nodeId.get(i), _tgt, _alledges, _path, _allpaths);
                }
            }
        }
        if (_path != null && !_path.isEmpty()) {
            _path.removeLast();
        }
        return _allpaths;
    }
    
    public int oneInOneOut() {
        int n = 0;
        for (final String node : graph_qname) {
            if (graph_qname.getPredNodeCount((Object)node) == 1 && graph_qname.getSuccNodeCount((Object)node) == 1) {
                ++n;
            }
        }
        return n;
    }
    
    public void output_constructs() throws IOException {
        try {
            final File output = File.createTempFile("constructs", ".txt");
            output.createNewFile();
            final BufferedWriter bw = new BufferedWriter(new FileWriter(output));
            final Iterator<String> nodes = (Iterator<String>)graph_qname.iterator();
            while (nodes.hasNext()) {
                bw.write(nodes.next() + "\r\n");
            }
            bw.flush();
            bw.close();
        }
        catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    static {
        log = LogFactory.getLog(Callgraph.class);
    }
}
