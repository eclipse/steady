package com.sap.psr.vulas.sign;

import static org.junit.Assert.assertTrue;

import com.sap.psr.vulas.java.sign.ASTConstructBodySignature;
import com.sap.psr.vulas.java.sign.JavaSignatureFactory;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import java.io.File;
import org.junit.Test;

public class SignatureFactoryTest {

  @Test
  public void createSignature() throws Exception {
    final SignatureFactory f = new JavaSignatureFactory();
    final ASTConstructBodySignature s =
        (ASTConstructBodySignature)
            f.createSignature(
                new ConstructId(
                    ProgrammingLanguage.JAVA,
                    ConstructType.METH,
                    "org.foo.Filter.doFilter(Boolean,String,String)"),
                new File("./src/test/resources/Filter.java"));
    assertTrue(s.toJson().length() > 0);
  }
}
