package com.sap.psr.vulas.backend.repo;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.util.ResultSetFilter;

/**
 * <p>LibraryIdRepository interface.</p>
 *
 */
@Repository
public interface LibraryIdRepository extends CrudRepository<LibraryId, Long> {

	/** Constant <code>FILTER</code> */
	public static final ResultSetFilter<LibraryId> FILTER = new ResultSetFilter<LibraryId>();

	
	/**
	 * <p>findBySecondaryKey.</p>
	 *
	 * @param mvnGroup a {@link java.lang.String} object.
	 * @param artifact a {@link java.lang.String} object.
	 * @param version a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT libid FROM LibraryId AS libid WHERE libid.mvnGroup = :mvnGroup AND libid.artifact = :artifact AND libid.version = :version")
	List<LibraryId> findBySecondaryKey(@Param("mvnGroup") String mvnGroup, @Param("artifact") String artifact, @Param("version") String version);
	
	/**
	 * <p>findLibIds.</p>
	 *
	 * @param mvnGroup a {@link java.lang.String} object.
	 * @param artifact a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT distinct libid FROM LibraryId AS libid JOIN FETCH libid.affLibraries WHERE libid.mvnGroup = :mvnGroup AND libid.artifact = :artifact")
	List<LibraryId> findLibIds(@Param("mvnGroup") String mvnGroup, @Param("artifact") String artifact);
	
	/**
	 * <p>findArtifactIdsOSSI.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT distinct libid.artifact FROM LibraryId AS libid where not libid.mvnGroup LIKE 'com.sap%' and not libid.artifact LIKE 'com.sap%'")
	List<String> findArtifactIdsOSSI();
	
	/**
	 * <p>findAllLibIdsOSSI.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@Query("SELECT distinct libid FROM LibraryId AS libid where not libid.mvnGroup LIKE 'com.sap%'")
	List<LibraryId> findAllLibIdsOSSI();
	
	@Query(value="select distinct d.id as dep_id, bl.bundled_library_ids_id as boundled_lid_id "
			+ "   from app_dependency d "
			+ "   inner join lib l1 on d.lib=l1.digest "
			+ "   inner join lib_bundled_library_ids bl on l1.id=bl.library_id "
			+ "   where d.app=:app and not l1.library_id_id = bl.bundled_library_ids_id  ", nativeQuery=true)
	List<Object[]> findBundledLibIdByApp(@Param("app") Application app);
}
