package com.sap.psr.vulas.backend.repo;


import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sap.psr.vulas.backend.model.AffectedConstructChange;
import com.sap.psr.vulas.backend.model.AffectedLibrary;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;

@Repository
public interface AffectedLibraryRepository extends CrudRepository<AffectedLibrary, Long>, AffectedLibraryRepositoryCustom {
	
	public static final ResultSetFilter<AffectedLibrary> FILTER = new ResultSetFilter<AffectedLibrary>();

	/**
	 * Find all entries for a given {@link Bug}, {@link LibraryId} and {@link AffectedVersionSource}.
	 * @param bug
	 * @param libraryId
	 * @return
	 */
	@Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact  AND afflib.libraryId.version = :version AND afflib.source = :source")
	List<AffectedLibrary> findByBugAndLibraryIdAndSource(@Param("bug") Bug bug, @Param("group") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("source") AffectedVersionSource source);
	
	/**
	 * Find all entries for a given {@link Bug} and {@link AffectedVersionSource}.
	 * @param bug
	 * @param libraryId
	 * @return
	 */
	@Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.source = :source")
	List<AffectedLibrary> findByBugAndSource(@Param("bug") Bug bug, @Param("source") AffectedVersionSource source);
	
	@Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.source = :source AND afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact AND afflib.libraryId.version = :version")
	List<AffectedLibrary> findByBugAndLibIdAndSource(@Param("bug") Bug bug, @Param("source") AffectedVersionSource source, @Param("group") String group, @Param("artifact") String artifact,@Param("version") String version);
	
	/**
	 * Find all entries for a given {@link Bug} and {@link AffectedVersionSource}.
	 * @param bug
	 * @param libraryId
	 * @return
	 */
	@Modifying
	@Transactional
	@Query("DELETE FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.source = :source")
	void deleteByBugAndSource(@Param("bug") Bug bug, @Param("source") AffectedVersionSource source);
	
	/**
	 * Find all entries for a given {@link Bug} and {@link LibraryId}.
	 * @param bug
	 * @param libraryId
	 * @return
	 */
	//@Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact  AND afflib.libraryId.version = :version")
	//List<AffectedLibrary> findByBugAndLibraryId(@Param("bug") Bug bug, @Param("group") String group, @Param("artifact") String artifact, @Param("version") String version);

	/**
	 * Find all entries for a given {@link Bug}.
	 * @param bug
	 * @return
	 */
	@Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug")
	List<AffectedLibrary> findByBug(@Param("bug") Bug bug);
	
	
	@Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact AND afflib.libraryId.version = :version")
	List<AffectedLibrary> findByBugAndLibId(@Param("bug") Bug bug, @Param("group") String group, @Param("artifact") String artifact,@Param("version") String version);
	
	@Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.bugId = :bug AND afflib.source = :source AND afflib.lib = :digest")
	List<AffectedLibrary> findByBugAndLibAndSource(@Param("bug") Bug bug, @Param("digest") Library digest, @Param("source") AffectedVersionSource source);

	@Modifying
	@Transactional
	@Query("DELETE FROM AffectedConstructChange as affcc WHERE affcc.affectedLib = :aff_lib")
	void deleteCCByAffLib(@Param("aff_lib") AffectedLibrary aff_lib);
	
	
	@Query("SELECT affcc FROM AffectedConstructChange AS affcc JOIN affcc.affectedLib afflib WHERE afflib.bugId = :bug AND afflib.lib = :digest")
	List<AffectedConstructChange> findByBugAndLib(@Param("bug") Bug bug, @Param("digest") Library digest);


	/**
	 * Finds all bugs for a given {@link LibraryId}.
	 * @param libraryId
	 * @return
	 */
	@Query("SELECT distinct afflib.bugId FROM AffectedLibrary AS afflib WHERE afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact AND afflib.libraryId.version = :version")
	List<Bug> findBugByLibraryId(@Param("group") String group, @Param("artifact") String artifact, @Param("version") String version);
//	to consider also '?' we can use the following query on our custom view
//	@Query("SELECT distinct bug FROM v_libraryid_bugs AS afflib WHERE afflib.mvnGroup = :group AND afflib.artifact = :artifact  AND afflib.version = :version AND afflib.affected=:affected", nativeQuery=true)
//	List<String> findBugByLibraryId(@Param("group") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("affected")Boolean affected);

	/**
	 * Same as {@link AffectedLibraryRepository#findBugByLibraryId(String, String, String)}, but offering an additional filter for selected bug ID(s).
	 * @param libraryId
	 * @return
	 */
	@Query("SELECT distinct afflib.bugId FROM AffectedLibrary AS afflib WHERE afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact AND afflib.libraryId.version = :version AND afflib.bugId.bugId IN :bugIds ")
	List<Bug> findBugByLibraryId(@Param("group") String group, @Param("artifact") String artifact, @Param("version") String version, @Param("bugIds") String[] bugIds);	
	
	@Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact  AND afflib.libraryId.version = :version")
	List<AffectedLibrary> findByLibraryId(@Param("group") String group, @Param("artifact") String artifact, @Param("version") String version);
	
	@Query("SELECT afflib FROM AffectedLibrary AS afflib WHERE afflib.libraryId.mvnGroup = :group AND afflib.libraryId.artifact = :artifact ")
	List<AffectedLibrary> findByLibraryIdGA(@Param("group") String group, @Param("artifact") String artifact);
	
	@Query(value= "	select a.affected from "
			+ "	(select distinct bug_id,lib,affected from bug_affected_library where source='MANUAL' and library_id is null "
			+ " UNION "
			+ " select distinct al1.bug_id,al1.lib,al1.affected from bug_affected_library as al1"
			+ " where al1.library_id is null and (al1.source='AST_EQUALITY' OR al1.source='MINOR_EQUALITY'OR al1.source='MAJOR_EQUALITY' OR al1.source='GREATER_RELEASE' OR al1.source='INTERSECTION' OR al1.source='PROPAGATE_MANUAL') "
			+ " and not exists (select 1 from bug_affected_library as al2 where al2.source='MANUAL' and al1.bug_id=al2.bug_id and al1.lib=al2.lib)) as a "
			+ " where a.bug_id=:bug_id and a.lib=:lib",nativeQuery=true)
	Boolean isBugLibAffected(@Param("bug_id") String bug_id, @Param("lib") String lib);
	
//	@Query(value= "	select a.affected from "
//			+ "	(select distinct bug_id,lib,library_id,affected from bug_affected_library where source='MANUAL'  "
//			+ " UNION "
//			+ " select distinct bug_id,lib,library_id,affected from bug_affected_library "
//			+ " where (source='AST_EQUALITY' OR source='MINOR_EQUALITY'OR source='MAJOR_EQUALITY' OR source='GREATER_RELEASE' OR source='INTERSECTION') "
//			+ " and not (lib is not null and exists (select 1 from bug_affected_library where source='MANUAL' and bug_id=bug_id and lib=lib)) "
//			+ " and not (library_id is not null and exists (select 1 from bug_affected_library where source='MANUAL' and bug_id=bug_id and library_id=library_id))) as a "
//			+ " where a.bug_id=:bug_id and a.library_id=:library_id",nativeQuery=true)
	@Query(value= "	select a.affected from "
			+ "	(select distinct bug_id,library_id,affected from bug_affected_library where source='MANUAL'  and lib is null"
			+ " UNION "
			+ " select distinct al1.bug_id,al1.library_id,al1.affected from bug_affected_library as al1"
			+ " where al1.lib is null and (al1.source='AST_EQUALITY' OR al1.source='MINOR_EQUALITY'OR al1.source='MAJOR_EQUALITY' OR al1.source='GREATER_RELEASE' OR al1.source='INTERSECTION' OR al1.source='PROPAGATE_MANUAL') "
			+ " and not exists (select 1 from bug_affected_library as al2 where al2.source='MANUAL' and al1.bug_id=al2.bug_id and al1.library_id=al2.library_id)) as a "
			+ " where a.bug_id=:bug_id and a.library_id=:library_id",nativeQuery=true)
	Boolean isBugLibIdAffected(@Param("bug_id") String bug_id, @Param("library_id") LibraryId library_id);
}