package com.sap.psr.vulas.backend.requests;


import com.sap.psr.vulas.backend.HttpResponse;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Library;

public class PutLibraryCondition implements ResponseCondition {

	private Library lib;
	private int constructs_count;
	
	public PutLibraryCondition(Library _l){
		this.lib=_l;
	}
		
	@Override
	public boolean meetsCondition(HttpResponse _response) {
		if(_response==null || !_response.hasBody())
			return false;
		boolean meets = false;
		Library backend_lib = (Library) JacksonUtil.asObject(_response.getBody(), Library.class);
		
		//int existing_constructs = backend_lib.countConstructTypes().countTotal();
		constructs_count = (this.lib.getConstructs()==null ? 0 : this.lib.getConstructs().size());
		
		ContentCondition c = new ContentCondition("\\\"countTotal\\\"\\s*:\\s*([\\d]*)", ContentCondition.Mode.LT_DOUBLE, Integer.toString(constructs_count));
		if(c.meetsCondition(_response))
			meets = true;
		else if(backend_lib.getLibraryId()==null && this.lib.getLibraryId()!=null){
			meets = true;
		}else if((backend_lib.getBundledLibraryIds()==null || backend_lib.getBundledLibraryIds().size()==0) && (this.lib.getBundledLibraryIds()!=null && this.lib.getBundledLibraryIds().size()>0)){
			meets = true;
		}
			
		return meets;
	}

	public String toString() { return "[body LT_DOUBLE " + this.constructs_count + "] OR [existing_GAV==null, current_GAV!=null] OR [existing_bundledLibIds==null, current_bundledLibIds!=null]"; }
}
