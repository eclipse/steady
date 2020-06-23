package com.sap.psr.vulas.bytecode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sap.psr.vulas.backend.BackendConnectionException;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.goals.GoalContext;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.java.sign.ASTConstructBodySignature;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import com.sap.psr.vulas.java.sign.gson.ASTSignatureChangeDeserializer;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.json.model.AffectedLibrary;
import com.sap.psr.vulas.shared.json.model.Bug;
import com.sap.psr.vulas.shared.json.model.ConstructChange;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.sign.SignatureFactory;

public class BytecodeComparator  {
	
	private static final Log log = LogFactory.getLog(BytecodeComparator.class);
	
	GoalContext context;
	Map<Class<?>,StdDeserializer<?>> custom_deserializers = new HashMap<Class<?>,StdDeserializer<?>>();
	

	
	public BytecodeComparator(GoalContext _g) {
		this();
		context = _g;
	}
	
	public BytecodeComparator() {
		custom_deserializers.put(ASTSignatureChange.class, new ASTSignatureChangeDeserializer());
	}
	
	public void compareLibForBug(Library _l,String _b, Path _p) throws BackendConnectionException, IOException {
		String digest = new String(_l.getDigest());
		String bugId = _b;
		boolean vuln = false;
		boolean fixed = false;
		Boolean affected = null;
		boolean toUpload = false;
		
		Set<LibraryId> list = new HashSet<LibraryId>();

		Bug b1 = BackendConnector.getInstance().getBug(context, bugId);
		// retrieve existing affectedVersions
		List<AffectedLibrary> alist = new ArrayList<AffectedLibrary>();
		for (AffectedVersionSource s : AffectedVersionSource.values()) {
			if (!s.equals(AffectedVersionSource.TO_REVIEW)) {
				AffectedLibrary[] als = BackendConnector.getInstance().getBugAffectedLibraries(context, bugId,s.toString(), true);
				alist.addAll(Arrays.asList(als));
				log.debug("Existing [" + als.length + "] affected libraries in backend for source ["+ s.toString() + "]");
			}
		}
		
		// check if current pair bug/digest was already assessed
		for(AffectedLibrary a: alist){
			if(a.getLib()!=null && a.getLib().getDigest().equals(digest)){
				return;
			}
		}

		b1.setAffectedVersions(alist);
		
		JarFile archive  =  null;
		try {
			archive = new JarFile(_p.toFile());
			for (ConstructChange cc : b1.getConstructChanges()) {
				// only compare for type MOD of METH,CONS
				if (cc.getConstructChangeType().equals(ConstructChangeType.MOD)
						&& (cc.getConstructId().getType().equals(ConstructType.CONS)
								|| cc.getConstructId().getType().equals(ConstructType.METH))) {
					
	
					// retrieve the construct from JAR
					JavaId jid = JavaId.getJavaId(cc.getConstructId().getType().toString(),cc.getConstructId().getQname());
					JavaId def_ctx = JavaId.getCompilationUnit(jid);
	
					final String entry_name = def_ctx.getQualifiedName().replace('.', '/') + ".class";
					final Enumeration<JarEntry> en = archive.entries();
					JarEntry entry = null;
					while (en.hasMoreElements()) {
						entry = en.nextElement();
						if (entry.getName().equals(entry_name)) {
							break;
						}
					}
					Path classfile = null;
					if (entry != null) {
						classfile = Files.createTempFile(def_ctx.getQualifiedName(), ".class");
						log.debug("classfile at " + classfile.toAbsolutePath());
						final InputStream ais = archive.getInputStream(entry);
						final FileOutputStream fos = new FileOutputStream(classfile.toFile());
						IOUtils.copy(ais, fos);
						fos.flush();
						fos.close();
						ais.close();
					} else {
						log.warn("Artifact does not contain entry [" + entry_name + "] for class ["
								+ def_ctx.getQualifiedName() + "]");
					}
	
					String ast_current = null;
					ASTConstructBodySignature sign = null;
					if (classfile != null) {
						SignatureFactory sf = CoreConfiguration.getSignatureFactory(JavaId.toSharedType(jid));
						sign = (ASTConstructBodySignature) sf.createSignature(JavaId.toSharedType(jid),classfile.toFile());
						if (sign != null)
							ast_current = sign.toJson();
						// Delete
						try {
							Files.delete(classfile);
						} catch (Exception e) {
							log.error("Cannot delete file [" + classfile + "]: " + e.getMessage());
						}
					}
	
					if (ast_current != null) {
	
						ConstructBytecodeASTManager astMgr = new ConstructBytecodeASTManager(
								cc.getConstructId().getQname(), cc.getRepoPath(), cc.getConstructId().getType());
	
						for (AffectedLibrary a : b1.getAffectedVersions()) {
							if (a.getAffected() && a.getLibraryId() != null)
								astMgr.addVulnLid(a.getLibraryId());
							else if (!a.getAffected() && a.getLibraryId() != null)
								astMgr.addFixedLid(a.getLibraryId());
						}
	
						// retrieve and compare source whose libid was assessed as vuln
						for (LibraryId v : astMgr.getVulnLids()) {
							log.debug(v.toString());
							// retrieve bytecode of the known to be vulnerable library
							String ast_lid = astMgr.getVulnAst(v);
	
							if (ast_lid != null) {
								// check if the ast's diff is empty
	
								String body = "[" + ast_lid + "," + ast_current + "]";
								String editV = BackendConnector.getInstance().getAstDiff(context, body);
								ASTSignatureChange scV = (ASTSignatureChange) JacksonUtil.asObject(editV, custom_deserializers, ASTSignatureChange.class);
								/* */
	
								// SP check if scV get mofidications is null?
								log.debug("size to vulnerable lib " + v.toString() + " is ["
										+ scV.getModifications().size() + "]");
								if (scV.getModifications().size() == 0) {
	
									// check that there isn't also a construct = to vuln
	
									log.info("LID equal to vuln based on AST bytecode comparison with  [" + v.toString() + "]");
									vuln = true;
									list.add(v);
									break;
								}
							}
	
						}
	
						// retrieve and compare source whose libid was assessed as fixed
						for (LibraryId f : astMgr.getFixedLids()) {
							log.debug(f.toString());
							// retrieve bytecode of the known to be vulnerable library
							String ast_lid = astMgr.getFixedAst(f);
	
							if (ast_lid != null) {
								// check if the ast's diff is empty
	
								String body = "[" + ast_lid + "," + ast_current + "]";
								String editV = BackendConnector.getInstance().getAstDiff(context, body);
								ASTSignatureChange scV = (ASTSignatureChange) JacksonUtil.asObject(editV, custom_deserializers, ASTSignatureChange.class);
								/* */
	
								log.debug("size to fixed lib " + f.toString() + " is [" + scV.getModifications().size()
										+ "]");
								if (scV.getModifications().size() == 0) {
	
									log.info("LID  equal to fix based on AST bytecode comparison with  [" + f.toString()
											+ "]");
									// cpa2.addLibsSameBytecode(l);
									fixed = true;
									list.add(f);
									break;
								}
							}
						}
					}
				}
				// cia does not serve code for type class
	//		if(cc.getConstructChangeType().equals(ConstructChangeType.MOD) && cc.getConstructId().getType().equals(ConstructType.CLAS)) {
	//			// retrieve the bytecode of the currently analyzed library
	//			String cls_current = BackendConnector.getInstance().getSourcesForQnameInLib(context, toAssess.getMvnGroup()+"/"+toAssess.getArtifact()
	//					+"/"+toAssess.getVersion()+"/"+cc.getConstructId().getType().toString()+"/"+cc.getConstructId().getQname());
	//
	//			if(cls_current!=null){
	//				for(AffectedLibrary[] array: existingxSource.values()){
	//					for(AffectedLibrary a : array){
	//						if(a.getLibraryId()!=null) {
	//							String cls_known = BackendConnector.getInstance().getSourcesForQnameInLib(context, a.getLibraryId().getMvnGroup()+"/"+a.getLibraryId().getArtifact()
	//							+"/"+a.getLibraryId().getVersion()+"/"+cc.getConstructId().getType().toString()+"/"+cc.getConstructId().getQname());
	//						
	//							if(cls_known!=null && cls_current.equals(cls_known)) {
	//								list.add(a.getLibraryId());
	//								if(a.getAffected()) 
	//									vuln=true;
	//								else if(!a.getAffected()) 
	//									fixed=true;
	//							}
	//						}
	//					}
	//				}
	//			}
	//		}
			}
			archive.close();
		} catch (ZipException ze) {
			log.error("Error in opening zip file.");
		}
		if (vuln && !fixed) {
			affected = true;
			toUpload = true;
		} else if (!vuln && fixed) {
			affected = false;
			toUpload = true;
		}
	
		if (toUpload) {
			log.info("Creating Json for source CHECK_CODE for digest [" + digest + "] and bug [" + b1.getBugId()+ "]");
			
			AffectedLibrary al = new AffectedLibrary();
			al.setBugId(b1);
			al.setLib(_l);
			al.setAffected(affected);
			al.setExplanation("Same bytecode found in library(ies) ["+list.toString()+"] ");// by DigestAnalyzer on " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
			al.setSource(AffectedVersionSource.CHECK_CODE);
			
			final JsonBuilder json = new JsonBuilder().startArray();
			json.appendJsonToArray(JacksonUtil.asJsonString(al));
			json.endArray();
			
			BackendConnector.getInstance().uploadBugAffectedLibraries(context, b1.getBugId(), json.getJson(),AffectedVersionSource.CHECK_CODE);
		}
	}
	

}
