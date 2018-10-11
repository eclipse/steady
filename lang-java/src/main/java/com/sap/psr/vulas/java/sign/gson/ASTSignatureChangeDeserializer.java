package com.sap.psr.vulas.java.sign.gson;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import com.sap.psr.vulas.java.sign.ASTUtil;
import com.sap.psr.vulas.sign.SignatureChange;

/**
 *  This class is used to deserialize an AST JSON representation of a signature change object into its corresponding
 *  java object
 *   Note : One needs to take a look at the source code of changedistiller to understand the elements that are employed for the Deserialization
 */

 /**

/**

JSON representation of ASTSignatureChange:

public class ASTSignatureChange extends DistillerUtil implements SignatureChange

public class ASTConstructBodySignature extends ASTSignature
public abstract class ASTSignature extends Node implements Signature


SourceCodeChange

	1. private ChangeType fChangeType = ChangeType.UNCLASSIFIED_CHANGE;

    //Structure entity in which the change operation happened, e.g., attribute, class, or method.
    2. private StructureEntityVersion fRootEntity;

    3. private SourceCodeEntity fChangedEntity;
		 -  fUniqueName : String  //same information as fValue above
		 -  fType : EntityType    //same information ast fLabel above
		 -  fModifiers : int	 // this is always zero (Not sure if i should have it in the json object)
		 -  fAssociatedEntities : List<SourceCodeEntity>
		 -  SourceRange fRange : SourceRange
				- end : int
				- start : int

    //Source code entity that becomes the parent entity when the change is applied.
    4. private SourceCodeEntity fParentEntity;




	public class ASTSignatureChange{

		private ChangeType changeType;
		private StructureEntityVersion StructureEntityVersion;
		private SourceCodeEntity changedEntity;
		private SourceCodeEntity r
	}

 *
 * @return JSON representation of the diff between two ASTs
 *
 *  A Source code change has this JSON object format
 *  {
 *  	"changeType" : " ",
 *  	"StructureEntityVersion" : {
 *  	              "EntityType" : "",
 *  				  "UniqueName" : "",
 *  				  "Modifiers" : "",  // might be discarded, int
 *  				  "Version " : ""
 *  		},
 *  	"changedEntity" : {
 *  			"UniqueName" : "",
 *  			"EntityType " : "",
 *  			"Modifiers" : "",  // might be discarded, int
 *  			"SourceRange" :
 *  					{
 *  						"start" : "",
 *  						 "end" : ""
 *  					}
 *        } ,
 *  	"parentEntity" : {
 *  			"UniqueName" : "",
 *  			"EntityType " : "",
 *  			"Modifiers" : "",  // might be discarded, int
 *  			"SourceRange" :
 *  					{
 *  						"start" : "",
 *  						 "end" : ""
 *  					}
 *
 *  		}
 *  }
**/
public class ASTSignatureChangeDeserializer  implements JsonDeserializer<SignatureChange>{

		private static final Log log = LogFactory.getLog(ASTSignatureChangeDeserializer.class);

	    //Values for JsonElement Names, used as a member name during deserialization
		//We need only to change these value if the name/value pair of a JsonElement changes in the json representation of the ASTSignatureChange
		//For instance, if we want to  compress  the json object -  not to store a large amount data in the DB.
		private static String SIGN_CHANGE = "vulasChange";
		private static String CHANGE_TYPE = "changeType";

		//StructureEntity, ROOT Entity
		private static String STRUCTURE_ENTITY = "StructureEntity";
		private static String STRUCTURE_ENTITY_UNIQUE_NAME = "UniqueName";
		private static String STRUCTURE_ENTITY_ENTITY_TYPE = "EntityType";
		private static String STRUCTURE_ENTITY_MODIFIERS = "Modifiers";
		private static String STRUCTURE_ENTITY_CHANGES = "changes";

		//ChagedEntity
		private static String SOURCE_CODE_ENTITY = "SourceCodeEntity";
		private static String SRC_ENTITY_UNIQUE_NAME = "UniqueName";
		private static String SRC_ENTITY_TYPE = "EntityType";
		private static String SRC_ENTITY_MODIFIERS = "Modifiers";
		private static String SRC_ENTITY_SOURCE_RANGE ="SourceCodeRange";
		private static String SRC_ENTITY_SOURCE_RANGE_END = "End";
		private static String SRC_ENTITY_SOURCE_RANGE_START = "Start";

		//parentEntity
		private static String PARENT_SOURCE_CODE_ENTITY = "parentEntity";
		private static String PARENT_SRC_ENTITY_UNIQUE_NAME = "UniqueName";
		private static String PARENT_SRC_ENTITY_TYPE = "EntityType";
		private static String PARENT_SRC_ENTITY_MODIFIERS = "Modifiers";
		private static String PARENT_SRC_ENTITY_SOURCE_RANGE ="SourceRange";
		private static String PARENT_SRC_ENTITY_SOURCE_RANGE_END = "End";
		private static String PARENT_SRC_ENTITY_SOURCE_RANGE_START = "Start";


		//private Set<SourceCodeChange> srcCodeChanges;
		//private SourceCodeChange srcCodeChange;
		//private StructureEntityVersion rootEntity = null;
		//List of SourceCodeChange modifications
		//private List<SourceCodeChange> srcCodeChangeList= new ArrayList<SourceCodeChange>();

		//ASTSignatureChange
		//private ASTSignatureChange astSignChange = null;

		//Signature
		//private SignatureChange signChange = null;
		//private ChangeType changeType = null;

		@Override
		public SignatureChange deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
			      throws JsonParseException{
			
			// HP, 10.12.2015, the following were members, now moved into method
						SourceCodeChange srcCodeChange;
						StructureEntityVersion rootEntity = null;
						List<SourceCodeChange> srcCodeChangeList= new ArrayList<SourceCodeChange>();
						ChangeType changeType = null;

			 //The deserialization logic goes here
			final JsonObject jsonObject = json.getAsJsonObject();
			final JsonObject strEntityJsonObject = jsonObject.get(STRUCTURE_ENTITY).getAsJsonObject();

			String uniqueName = strEntityJsonObject.get(STRUCTURE_ENTITY_UNIQUE_NAME).getAsString();
			EntityType type = ASTUtil.getJavaEntityType(strEntityJsonObject.get(STRUCTURE_ENTITY_ENTITY_TYPE).getAsString());
			int modifiers = strEntityJsonObject.get(SRC_ENTITY_MODIFIERS).getAsInt();
			rootEntity = new StructureEntityVersion(type,uniqueName, modifiers);

			JsonArray changesJsonArray = strEntityJsonObject.get("changes").getAsJsonArray();
			SourceCodeChange [] srcCodeChanges = new SourceCodeChange[changesJsonArray.size()];

			//Read in the Value the array from the "changes" element
			for(int j=0; j < srcCodeChanges.length; j++){

				final JsonObject jsonChangesChild = changesJsonArray.get(j).getAsJsonObject();

			//Depending on the "OperationType" of the "changes" element, initialize the appropriate sourceCodeChange element (Insert, Update, Move, Delete)
			String operationType = jsonChangesChild.get("OperationType").getAsString();

			/*switch(operationType)
			{*/
				//case "Insert":
			if(operationType.equals("Insert")){

						    changeType =   ASTUtil.getChangeType(jsonChangesChild.get("changeType").getAsString());
							SourceCodeEntity insertedEntity = this.getSourceCodeEntityElement(jsonChangesChild.get("InsertedEntity").getAsJsonObject());
							SourceCodeEntity parentEntity = this.getSourceCodeEntityElement(jsonChangesChild.getAsJsonObject("ParentEntity").getAsJsonObject());

						    srcCodeChange = new Insert(
					             changeType,
					             rootEntity,  //StructureEntityVersion
					             insertedEntity,  //New Inserted Entity
					             parentEntity) ;  //ParentEntity for the New Inserted Node

								//Add it to the List<SourceCodeChanges>
								srcCodeChangeList.add(srcCodeChange);
								//break;

			}


				else if(operationType.equals("Move")){
			//	case "Move":
							changeType =   ASTUtil.getChangeType(jsonChangesChild.get("changeType").getAsString());
							SourceCodeEntity movedEntity = this.getSourceCodeEntityElement(jsonChangesChild.get("MovedEntity").getAsJsonObject());
							SourceCodeEntity newEntity = this.getSourceCodeEntityElement(jsonChangesChild.getAsJsonObject("NewEntity").getAsJsonObject());
							SourceCodeEntity oldParentEntity = this.getSourceCodeEntityElement(jsonChangesChild.get("OldParentEntity").getAsJsonObject());
							SourceCodeEntity newParentEntity = this.getSourceCodeEntityElement(jsonChangesChild.getAsJsonObject("NewParentEntity").getAsJsonObject());

							//srcCodeChange.setRootEntity(rootEntity);  //StructureEntityVersion
							srcCodeChange = new   Move(
						            changeType,
						            rootEntity,
						            movedEntity,
						            newEntity,
						            oldParentEntity,
						            newParentEntity);

							//Add it to the List<SourceCodeChanges>
							srcCodeChangeList.add(srcCodeChange);
					//		break;
					}


				//case "Update":
					else if(operationType.equals("Update")){
						changeType =  ASTUtil.getChangeType(jsonChangesChild.get("changeType").getAsString());

						SourceCodeEntity updatedEntity = this.getSourceCodeEntityElement(jsonChangesChild.get("UpdatedEntity").getAsJsonObject());
						SourceCodeEntity newEntityUpdate = this.getSourceCodeEntityElement(jsonChangesChild.getAsJsonObject("NewEntity").getAsJsonObject());
						SourceCodeEntity parentEntityUpdate = this.getSourceCodeEntityElement(jsonChangesChild.get("ParentEntity").getAsJsonObject());

						srcCodeChange = new  Update(
					            changeType,
					            rootEntity,
					            updatedEntity,  //Old Updated Entity
					            newEntityUpdate,  //New Updated Entity
					            parentEntityUpdate) ;  //Same Parent Entity

						//Add it to the Set<SourceCodeChanges>
						srcCodeChangeList.add(srcCodeChange);

					//		break;
					}


				//case "Delete":

					else if(operationType.equals("Delete")){
						changeType =   ASTUtil.getChangeType(jsonChangesChild.get("changeType").getAsString());
						SourceCodeEntity deletedEntity = this.getSourceCodeEntityElement(jsonChangesChild.get("DeletedEntity").getAsJsonObject());
						SourceCodeEntity parentEntityDelete = this.getSourceCodeEntityElement(jsonChangesChild.get("ParentEntity").getAsJsonObject());

						srcCodeChange = new Delete(
										   changeType,
										   rootEntity,   //StructureEntityVersion;
										   deletedEntity,    //Deleted Entity
										   parentEntityDelete) ;    //ParentEntity

						//Add it to the Set<SourceCodeChanges>
						srcCodeChangeList.add(srcCodeChange);

					//	break;
					}
					else{
							log.info("Unsupported Operation Type" + operationType);
					}

			/*			default:
			}*/

			}

			//Add the List<SourceCodeChange> to the StructureEntityVersion instance
			rootEntity.addAllSourceCodeChanges(srcCodeChangeList);

			//astSignChange = new ASTSignatureChange(srcCodeChangeList);
			ASTSignatureChange astSignChange = new ASTSignatureChange(rootEntity);
			return astSignChange;
		}


		/**
		 * Helper method for retrieving a SourceCodeEntity element
		 */
		private SourceCodeEntity getSourceCodeEntityElement (JsonObject srcCodeEntityJsonObject){

			//Retrieve the SourceCodeEntity
		    SourceCodeEntity srcCodeEntity = null;
		    String fValue = srcCodeEntityJsonObject.get("UniqueName").getAsString();				//Retrieve the Value
		 	String strLabel = srcCodeEntityJsonObject.get("EntityType").getAsString();
		 	EntityType fLabel = ASTUtil.getJavaEntityType(strLabel); 													     	//Retrieve the EntityType
		    int modifiers = srcCodeEntityJsonObject.get(SRC_ENTITY_MODIFIERS).getAsInt();

		    //SourceRange JsonObject
		    JsonObject srcCodeEntitySrcRangeJsonObject = srcCodeEntityJsonObject.get(SRC_ENTITY_SOURCE_RANGE).getAsJsonObject();
		    int srcRangeStart = srcCodeEntitySrcRangeJsonObject.get(SRC_ENTITY_SOURCE_RANGE_START).getAsInt();
		    int srcRangeEnd = srcCodeEntitySrcRangeJsonObject.get(SRC_ENTITY_SOURCE_RANGE_END).getAsInt();
		    SourceRange srcRange = new SourceRange(srcRangeStart,srcRangeEnd);
		    srcCodeEntity =  new SourceCodeEntity(fValue,fLabel,modifiers,srcRange);

			srcCodeEntity = new  SourceCodeEntity(fValue, fLabel,modifiers, srcRange);
			return srcCodeEntity;
		}
	}