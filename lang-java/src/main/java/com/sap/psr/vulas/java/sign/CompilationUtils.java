package com.sap.psr.vulas.java.sign;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;

import com.sap.psr.vulas.shared.util.FileUtil;

import ch.uzh.ifi.seal.changedistiller.ast.java.Comment;
import ch.uzh.ifi.seal.changedistiller.ast.java.CommentCollector;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilation;

/**
 * Helper class to create the ASTSignature instances using ChangeDistiller.
 *
 */
public final class CompilationUtils {
	
	private static final Log log = LogFactory.getLog(CompilationUtils.class);

	private CompilationUtils() {}

    public static JavaCompilation compileSource(String source) {

    	//Compiler Options
        CompilerOptions options = getDefaultCompilerOptions();

        //CommentRecorder
        Parser parser = createCommentRecorderParser(options);

        //Create Compilation Unit from Source
        ICompilationUnit cu = createCompilationunit(source, "");

        //Compilation Result
        CompilationResult compilationResult = createDefaultCompilationResult(cu, options);

        return new JavaCompilation(parser.parse(cu, compilationResult), parser.scanner);
    }


    private static CompilerOptions getDefaultCompilerOptions() {
        CompilerOptions options = new CompilerOptions();
        options.docCommentSupport = true;
        options.complianceLevel = ClassFileConstants.JDK1_7;
        options.sourceLevel = ClassFileConstants.JDK1_7;
        options.targetJDK = ClassFileConstants.JDK1_7;
        return options;
    }


    private static ICompilationUnit createCompilationunit(String _source_code, String filename) {
    	char[] source_code = _source_code.toCharArray();
        ICompilationUnit cu = new CompilationUnit(source_code, filename, null);
        return cu;
    }



    /**
     * Returns the generated {@link JavaCompilation} from the file identified by the given filename. This method assumes
     * that the filename is relative to <code>{@value #TEST_DATA_BASE_DIR}</code>.
     *
     * @param _filename
     *            of the file to compile
     * @return the compilation of the file
     */
    public static JavaCompilation compileFile(String _filename) {
    	JavaCompilation jc = null;
		try {
			final String src = FileUtil.readFile(_filename);
			final CompilerOptions options = getDefaultCompilerOptions();
	        final Parser parser = createCommentRecorderParser(options);
	        final ICompilationUnit cu = createCompilationunit(src, _filename);
	        final CompilationResult compilationResult = createDefaultCompilationResult(cu, options);
	        jc = new JavaCompilation(parser.parse(cu, compilationResult), parser.scanner);
		} catch (IOException e) {
			log.error(e);
		}
		return jc;
    }

    /**
     *  Create a CommentRecorderParser
     * @param options - compiler options
     * @return
     */
    private static Parser createCommentRecorderParser(CompilerOptions options) {
        Parser parser =
                new CommentRecorderParser(new ProblemReporter(
                        DefaultErrorHandlingPolicies.proceedWithAllProblems(),
                        options,
                        new DefaultProblemFactory()), false);
        return parser;
    }


    private static CompilationResult createDefaultCompilationResult(ICompilationUnit cu, CompilerOptions options) {
        CompilationResult compilationResult = new CompilationResult(cu, 0, 0, options.maxProblemsPerUnit);
        return compilationResult;
    }


    public static List<Comment> extractComments(JavaCompilation sCompilationUnit) {
        CommentCollector collector =
                new CommentCollector(sCompilationUnit.getCompilationUnit(), sCompilationUnit.getSource());
        collector.collect();
        return collector.getComments();
    }

    public static AbstractMethodDeclaration findMethod(CompilationUnitDeclaration cu, String methodName) {
        for (TypeDeclaration type : cu.types) {
            for (AbstractMethodDeclaration method : type.methods) {
                if (String.valueOf(method.selector).equals(methodName)) {
                    return method;
                }
            }
        }
        return null;
    }

    public static FieldDeclaration findField(CompilationUnitDeclaration cu, String fieldName) {
        for (TypeDeclaration type : cu.types) {
            for (FieldDeclaration field : type.fields) {
                if (String.valueOf(field.name).equals(fieldName)) {
                    return field;
                }
            }
        }
        return null;
    }

    public static TypeDeclaration findType(CompilationUnitDeclaration cu, String typeName) {
        for (TypeDeclaration type : cu.types) {
            for (TypeDeclaration memberType : type.memberTypes) {
                if (String.valueOf(memberType.name).equals(typeName)) {
                    return memberType;
                }
            }
        }
        return null;
    }

    public static File getFile(String filename) {
        return new File(filename);
    }


}
