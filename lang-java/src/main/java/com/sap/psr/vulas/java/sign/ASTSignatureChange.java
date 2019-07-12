package com.sap.psr.vulas.java.sign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.psr.vulas.java.sign.gson.ASTSignatureChangeSerializer;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.sign.Signature;
import com.sap.psr.vulas.sign.SignatureChange;

import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

public class ASTSignatureChange extends DistillerUtil implements SignatureChange  {
	
	public static enum OperationType { Insert, Update, Move, Delete };

	private Signature mDefSignature = null;
	
	private Signature mFixSignature  = null;
	
	private Set<SourceCodeChange> listOfChanges = null;
	
	private String mMethodName;

	//TODO : (Something fishy here, needs an improvement - see the sourcecode of changedistiller)
	//A StructureEntityVersion has all the SourceCodeChange's
	private StructureEntityVersion structureEntity = null;
	
	public void addChange(SourceCodeChange scc) {
		this.listOfChanges.add(scc);
	}

	public void removeChange(SourceCodeChange scc){
		this.listOfChanges.remove(scc);
	}
	
	public static Set<SourceCodeChange> toSourceCodeChanges(Set<Object> _objects) {
		final Set<SourceCodeChange> changes = new HashSet<SourceCodeChange>();
		for(Object o: _objects) {
			changes.add((SourceCodeChange)o);
		}
		return changes;
	}

	/**
	 * Used to create instance of ASTSignatureChange during deserialization of
	 * JSON object,  when deserializing a ASTSignatureChange, we only have the list of
	 * SourceCodeChanges
	 * read from the DB.
	 * @param srcCodeChanges - List of SourceCodeChanges
	 *
	 */
	public ASTSignatureChange(Set<SourceCodeChange> srcCodeChanges){
		this(null,null);
		this.setListOfChanges(srcCodeChanges);
		List<SourceCodeChange> srcCodeChgs = new ArrayList<SourceCodeChange>(srcCodeChanges);
		this.structureEntity.setSourceCodeChanges(srcCodeChgs);
	}

	public ASTSignatureChange(StructureEntityVersion  strEntityVersion){
		//this(null,null);
		this.setStructureEntity(strEntityVersion);
		this.listOfChanges = new HashSet<SourceCodeChange>(this.getStructureEntity().getSourceCodeChanges());
	}

	public StructureEntityVersion getStructureEntity() {
		return structureEntity;
	}


	public void setStructureEntity(StructureEntityVersion structureEntity) {
		this.structureEntity = structureEntity;
	}


	public ASTSignatureChange(Signature defSignature, Signature fixSignature){
		this.mDefSignature = defSignature;
		this.mFixSignature = fixSignature;
		//Name of the root of the AST for either the fixed or for the defective AST
		mMethodName = ((ASTSignature)mFixSignature).getValue();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((listOfChanges == null) ? 0 : listOfChanges.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASTSignatureChange other = (ASTSignatureChange) obj;
		if (listOfChanges == null) {
			if (other.listOfChanges != null)
				return false;
		} else {
			final boolean is_equal = ASTSignatureChange.isSameElements((Collection<SourceCodeChange>)this.listOfChanges, (Collection<SourceCodeChange>)other.listOfChanges);
			return is_equal;
		}
		return true;
	}

	public static boolean isSameElements(Collection<SourceCodeChange> _a, Collection<SourceCodeChange> _b) {
		if(_a.size()!=_b.size())
			return false;
		for(SourceCodeChange this_scc: _a) {
			boolean scc_found = false;
			for(SourceCodeChange other_scc: _b) {
				if(this_scc.equals(other_scc)) {
					scc_found = true;
					break;
				}
			}
			if(!scc_found)
				return false;
		}
		return true;
	}

	public Signature getmDefSignature() {
		return mDefSignature;
	}


	public void setmDefSignature(Signature mDefSignature) {
		this.mDefSignature = mDefSignature;
	}


	public Signature getmFixSignature() {
		return mFixSignature;
	}


	public void setmFixSignature(Signature mFixSignature) {
		this.mFixSignature = mFixSignature;
	}

	public Set<SourceCodeChange> getListOfChanges() {
		return listOfChanges;
	}

	public void setListOfChanges(Set<SourceCodeChange> listOfChanges) {
		this.listOfChanges = listOfChanges;
	}

	public String getmMethodName() {
		return mMethodName;
	}

	public void setmMethodName(String mMethodName) {
		this.mMethodName = mMethodName;
	}

	public String operationTypetoJSON(OperationType t){
		final StringBuilder b  = new StringBuilder();
		b.append("\"operationType\" : \"");
		switch(t) {
		case Insert: b.append("INSERT\""); break;
		case Update: b.append("UPDATE\""); break;
		case Move:   b.append("MOVE\""); break;
		case Delete: b.append("DELETE\""); break;
		default:     b.append("???\""); break;
		}
		return b.toString();
	}


	private String operationTypeToString(OperationType t){
		switch(t) {
		case Insert: return "I";
		case Update: return "U";
		case Move:   return "M";
		case Delete: return "D";
		default:     return "?";
		}
	}

	/**
	 * Returns the set of edit operations required to change the defective AST int the fixed one.
	 * Attention: This method will change the defective signature passed as argument to the constructor.
	 * @return edit operations to transform the defective AST into the fixed one
	 *//*
	@Override
	public Set<Object> getModifications() {

		if(this.listOfChanges==null) {

			structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, this.mMethodName, 0);
			Distiller mDistiller = mInjector.getInstance(DistillerFactory.class).create(structureEntity);

			if(this.mDefSignature != null && this.mFixSignature != null)
			{
				Node defSignatureNode = ((ASTSignature)this.mDefSignature).getRoot();
				Node fixSignatureNode = ((ASTSignature)this.mFixSignature).getRoot();

				//This call modifies the "Defective" Version, basically it converts it into the Fixed Version
				// (I don't know why , nothing is mentioned in the API documentation)
				// TODO : Read the the changedistiller paper  for a better understanding (http://www.merlin.uzh.ch/publication/show/2531)
				mDistiller.extractClassifiedSourceCodeChanges(defSignatureNode, fixSignatureNode);
				List<SourceCodeChange>change = structureEntity.getSourceCodeChanges();   //Extract the set of sourceCodeChanges from the StructureEntityVersion


				//Debug
				//System.out.println(change.toString());
				if(change != null)
					this.listOfChanges = new HashSet<SourceCodeChange>(change);

				/*	if(change != null)
				     		{
					            for(SourceCodeChange ch : change){
					            		listOfChanges.add(ch);
					            	}
				     		}
			}
		}

		//Good to know : http://stackoverflow.com/questions/905964/how-to-cast-generic-list-types-in-java?answertab=active#tab-top
		Set<Object> set = new HashSet<Object>();
		for (SourceCodeChange  e :  this.listOfChanges) {
			set.add((Object) e); // need to cast each object specifically
		}
		return set;
	}*/

	@Override
	public Set<Object> getModifications() {

		if(this.listOfChanges==null) {
			structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, this.mMethodName, 0);
			Distiller mDistiller = mInjector.getInstance(DistillerFactory.class).create(structureEntity);

			if(this.mDefSignature != null && this.mFixSignature != null)
			{
				Node defSignatureNode = ((ASTSignature)this.mDefSignature).getRoot();
				Node fixSignatureNode = ((ASTSignature)this.mFixSignature).getRoot();

				//This call modifies the "Defective" Version, basically it converts it into the Fixed Version
				// (I don't know why , nothing is mentioned in the API documentation)
				// TODO : Read the the changedistiller paper  for a better understanding (http://www.merlin.uzh.ch/publication/show/2531)
				mDistiller.extractClassifiedSourceCodeChanges(defSignatureNode, fixSignatureNode);
				List<SourceCodeChange>change = structureEntity.getSourceCodeChanges();   //Extract the set of sourceCodeChanges from the StructureEntityVersion

				//Debug
				if(change != null)
					this.listOfChanges = new HashSet<SourceCodeChange>(change);

				/*	if(change != null)
				     		{
					            for(SourceCodeChange ch : change){
					            		listOfChanges.add(ch);
					            	}
				     		}*/
			}
		}

		//Good to know : http://stackoverflow.com/questions/905964/how-to-cast-generic-list-types-in-java?answertab=active#tab-top
		final Set<Object> set = new HashSet<Object>();
		for (SourceCodeChange  e :  this.listOfChanges) {
			set.add( e); // need to cast each object specifically
		}
		return set;
	}

	@Override
	public String toJSON() {
		final Map<Class<?>, StdSerializer<?>> custom_serializers = new HashMap<Class<?>, StdSerializer<?>>();
		custom_serializers.put(ASTSignatureChange.class, new ASTSignatureChangeSerializer());
		return JacksonUtil.asJsonString(this, custom_serializers);

		/*final StringBuilder buffer = new StringBuilder();

		//The StructureEntityVersion has the Set of SourceCodeChanges
		buffer.append("{\"StructureEntity\" :{ ");

		buffer.append("\"UniqueName\" : ").append(JsonBuilder.escape(structureEntity.getUniqueName().toString())).append(",");
		buffer.append("\"EntityType\" : \"").append(structureEntity.getType()).append("\",");
		buffer.append("\"Modifiers\" : \"").append(structureEntity.getModifiers()).append("\",");

		buffer.append("\"changes\" :[");

		String operationType = null;
		int y = listOfChanges.size();
		int x = 0;
		for(SourceCodeChange change :listOfChanges) {

			// INSERT OPERATION
			if(change instanceof Insert){
				buffer.append("{");
				buffer.append("\"OperationType\" : \"").append(change.getClass().getSimpleName()).append("\","); //Operation Type
				buffer.append("\"changeType\" : \"").append(change.getChangeType()).append("\","); 						//Change Type

				//InsertedEntity
				buffer.append("\"InsertedEntity\" : {");
				buildSourceCodeEntityElement(change.getChangedEntity(), buffer);
				buffer.append("},"); //end of ChangedEntity

				//Parent Entity
				buffer.append("\"ParentEntity\" : {");
				buildSourceCodeEntityElement(change.getParentEntity(), buffer);
				buffer.append("}"); //end of ParentEntity
				buffer.append("}"); //end the  JSON object
			}

			//DELETE OPERATION
			else if(change instanceof Delete){

				buffer.append("{");
				buffer.append("\"OperationType\" : \"").append(change.getClass().getSimpleName()).append("\","); //CHANGE TYPE
				buffer.append("\"changeType\" : \"").append(change.getChangeType()).append("\","); //CHANGE TYPE

				//ChangedEntity
				buffer.append("\"DeletedEntity\" : {");
				buildSourceCodeEntityElement(change.getChangedEntity(), buffer);
				buffer.append("},"); //end of ChangedEntity

				//Parent Entity
				buffer.append("\"ParentEntity\" : {");
				buildSourceCodeEntityElement(change.getParentEntity(), buffer);
				buffer.append("}"); //end of ParentEntity
				buffer.append("}"); //end the  JSON object
			}

			//MOVE OPERATION
			else if(change instanceof Move){
				buffer.append("{");
				buffer.append("\"OperationType\" : \"").append(change.getClass().getSimpleName()).append("\","); //CHANGE TYPE
				buffer.append("\"changeType\" : \"").append(change.getChangeType()).append("\","); //CHANGE TYPE

				//Old Parent Entity
				buffer.append("\"OldParentEntity\" : {");
				buildSourceCodeEntityElement(change.getParentEntity(), buffer);
				buffer.append("},"); //end of ParentEntity

				//Moved Entity
				buffer.append("\"MovedEntity\" : {");
				buildSourceCodeEntityElement(change.getChangedEntity(), buffer);
				buffer.append("},"); //end of ChangedEntity

				//New Parent Entity
				buffer.append("\"NewParentEntity\" : {");
				buildSourceCodeEntityElement(((Move) change).getNewParentEntity(), buffer);
				buffer.append("},"); //end of ParentEntity

				//New Entity
				buffer.append("\"NewEntity\" : {");
				buildSourceCodeEntityElement(((Move) change).getNewEntity(), buffer);
				buffer.append("}"); //end of ParentEntity
				buffer.append("}"); //end the  JSON object
			}

			//UPDATE OPERATION
			else if(change instanceof Update){
				buffer.append("{");
				buffer.append("\"OperationType\" : \"").append(change.getClass().getSimpleName()).append("\","); //CHANGE TYPE
				buffer.append("\"changeType\" : \"").append(change.getChangeType()).append("\","); //CHANGE TYPE

				//NewEntity
				buffer.append("\"NewEntity\" : {");
				buildSourceCodeEntityElement(((Update) change).getNewEntity(), buffer);
				buffer.append("},"); //end of ChangedEntity

				//Updated Entity
				buffer.append("\"UpdatedEntity\" : {");
				buildSourceCodeEntityElement(((Update) change).getChangedEntity(), buffer);
				buffer.append("},"); //end of ChangedEntity

				//Parent Entity
				buffer.append("\"ParentEntity\" : {");
				buildSourceCodeEntityElement(change.getParentEntity(), buffer);
				buffer.append("}"); //end of ParentEntity
				buffer.append("}"); //end the  JSON object
			}


			//buffer.append("}");
			if(++x < y)
				buffer.append(",");
		}

		buffer.append("]}");  //end the sourceCodeChange element

		buffer.append("}");  //end the StructureEntityVersion element
		return buffer.toString();*/
	}

	/**
	 * Helper method for building a "SourceCodeEntity" element
	 *
	 * @param change - SourcCodeChangeElement
	 * @param buffer
	 */
	/*private void buildSourceCodeEntityElement(SourceCodeEntity srcCodeEntity, StringBuilder buffer){
		buffer.append("\"UniqueName\" : ").append(JsonBuilder.escape(srcCodeEntity.getUniqueName().toString())).append(",");
		buffer.append("\"EntityType\" : \"").append(srcCodeEntity.getType()).append("\",");
		buffer.append("\"Modifiers\" : \"").append(srcCodeEntity.getModifiers()).append("\",");
		buffer.append("\"SourceCodeRange\" : {");
		buffer.append("\"Start\" : \"").append(srcCodeEntity.getSourceRange().getStart()).append("\",");
		buffer.append("\"End\" : \"").append(srcCodeEntity.getSourceRange().getEnd()).append("\"");
		buffer.append("}");
	}*/

	/**
	 * Returns a formatted 'SourceCodeChange' elements
	 */
	public String toString() {
		int count = 0;
		final StringBuffer buffer = new StringBuffer();

		if(listOfChanges != null) {
			buffer.append("Test - Everything SourceCodeChange Offers :\n");

			for(SourceCodeChange change :listOfChanges) {
				buffer.append("\n CHANGE"  + ++count + "\n");
				buffer.append("\n ChangedEntity = " + change.getChangedEntity());
				buffer.append("\n ChangedEntity Source Range= " + change.getChangedEntity().getSourceRange());
				buffer.append("\n UniqueName = " + change.getChangedEntity().getUniqueName());
				buffer.append("\n Entity Label = " + change.getChangedEntity().getLabel());
				buffer.append("\n Entity Type = " + change.getChangedEntity().getType().toString());
				buffer.append("\n Change Type Label = " +change.getLabel());
				buffer.append("\n Parent  =  " + change.getParentEntity());
				buffer.append("\n Parent Source Range = " + change.getParentEntity().getSourceRange());
				buffer.append("\n Simple Name = " +change.getClass().getSimpleName());
				buffer.append("\n " +change.toString() +"\n");
				//buffer.append("\n SignificanceLevel(CRUCIAL, HIGH, MEDIMU, LOW) :" + change.getSignificanceLevel().toString());
				//buffer.append("\n ChangedEntity, StartPosition : " + change.getChangedEntity().getStartPosition());
			}
		}
		return buffer.toString();
	}
	
	@Override
	public boolean isEmpty() {
		return this.getListOfChanges().size()==0;
	}
}
