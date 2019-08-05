package com.sap.psr.vulas.java.sign.gson;

import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.sap.psr.vulas.java.sign.ASTConstructBodySignature;
import com.sap.psr.vulas.java.sign.ASTUtil;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>ASTConstructBodySignatureDeserializer class.</p>
 *
 */
public class ASTConstructBodySignatureDeserializer extends StdDeserializer<ASTConstructBodySignature> {
private static final Log log = LogFactory.getLog(ASTConstructBodySignatureDeserializer.class);
	/**
	 * <p>Constructor for ASTConstructBodySignatureDeserializer.</p>
	 */
	public ASTConstructBodySignatureDeserializer() {
		this(null);
	}

	/**
	 * <p>Constructor for ASTConstructBodySignatureDeserializer.</p>
	 *
	 * @param t a {@link java.lang.Class} object.
	 */
	public ASTConstructBodySignatureDeserializer(Class<ASTConstructBodySignature> t) {
		super(t);
	}

	private Node getAstNode(JsonNode _json_node){

		final String value = _json_node.findValue("Value").asText();
		final String type_str = _json_node.findValue("EntityType").asText();
		final EntityType type = ASTUtil.getJavaEntityType(type_str);

		// Instantiate the Node object here
		Node ast_node = new Node(type, value);

		// Retrieve the SourceCodeEntity
		final JsonNode src_code_entity = _json_node.findValue("SourceCodeEntity");
		final int modifiers = src_code_entity.findValue("Modifiers").asInt();

		// SourceRange JsonObject
		final JsonNode src_range_json = src_code_entity.findValue("SourceRange");
		final int src_start = src_range_json.findValue("Start").asInt();
		final int src_end = src_range_json.findValue("End").asInt();
		final SourceRange src_range = new SourceRange(src_start, src_end);

		final SourceCodeEntity srcCodeEntity =  new SourceCodeEntity(value, type, modifiers, src_range);
		ast_node.setEntity(srcCodeEntity);

		// Loop the children
		List<JsonNode> children = _json_node.findValues("C");
		
                for(JsonNode json_child: children) {
                    if ( json_child.isArray() ){
                        for ( int i=0; i<json_child.size(); i++ ) {
                            Node ast_child = getAstNode(json_child.get(i));
                            ast_node.add(ast_child);
                        }
                    } else {
                        Node ast_child = getAstNode(json_child);
                        ast_node.add(ast_child);
                    }                    
		}

		return ast_node;
	}

	/** {@inheritDoc} */
	@Override
	public ASTConstructBodySignature deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JsonProcessingException {
		final JsonNode json_root = jp.getCodec().readTree(jp);
		final List<JsonNode> ast_nodes = json_root.findValues("ast");
		final JsonNode ast_root  = ast_nodes.get(0);
		final String value = ast_root.findValue("Value").asText();
		final String type_str = ast_root.findValue("EntityType").asText();
		final EntityType type = ASTUtil.getJavaEntityType(type_str);

		// To be returned
		ASTConstructBodySignature astConstructSign = new ASTConstructBodySignature(type, value);

		//Create the Root Node
		astConstructSign.setRoot(astConstructSign);

		//TODO : Check if these setting are really necessary to compute the "diff" as an input to changedistiller
		//Settings for order and matching
		//astConstructSign.enableInOrder();
		//astConstructSign.enableMatched();

		// Loop the children
		List<JsonNode> children = ast_root.findValues("C");
                // from jackson documentation:
                // findValues(String fieldName)
                // Method for finding JSON Object fields with specified name, and returning found ones as a List.
		for(JsonNode json_child: children) {
                    if ( json_child.isArray() ){
                        for ( int i=0; i<json_child.size(); i++ ) {
                            Node ast_child = getAstNode(json_child.get(i));
                            astConstructSign.add(ast_child);
                        }
                    } else {
                        Node ast_child = getAstNode(json_child);
                        astConstructSign.add(ast_child);
                    }                    
		}

		// Retrieve the SourceCodeEntity
		final JsonNode src_code_entity = ast_root.findValue("SourceCodeEntity");
		final int modifiers = src_code_entity.findValue("Modifiers").asInt();

		// SourceRange JsonObject
		final JsonNode src_range_json = src_code_entity.findValue("SourceRange");
		final int src_start = src_range_json.findValue("Start").asInt();
		final int src_end = src_range_json.findValue("End").asInt();
		final SourceRange src_range = new SourceRange(src_start, src_end);

		final SourceCodeEntity srcCodeEntity =  new SourceCodeEntity(value, type, modifiers, src_range);
		astConstructSign.setEntity(srcCodeEntity);

		return astConstructSign;
	}
}
