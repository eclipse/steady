package com.sap.psr.vulas.backend.repo;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.Trace;
import com.sap.psr.vulas.backend.util.ResultSetFilter;
import com.sap.psr.vulas.shared.enums.GoalType;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** TracesRepository interface. */
@Repository
public interface TracesRepository extends CrudRepository<Trace, Long>, TracesRepositoryCustom {

  /** Constant <code>FILTER</code> */
  public static final ResultSetFilter<Trace> FILTER = new ResultSetFilter<Trace>();

  /**
   * findByApp.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @return a {@link java.util.List} object.
   */
  @Query("SELECT DISTINCT t FROM Trace t " + " JOIN FETCH t.constructId " + " WHERE t.app = :app")
  List<Trace> findByApp(@Param("app") Application app);

  /**
   * Deletes all traces collected in the context of the given {@link Application}. Called by goal
   * {@link GoalType#CLEAN}.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   */
  @Modifying
  @Transactional
  @Query("DELETE FROM Trace t WHERE t.app = :app")
  void deleteAllTracesForApp(@Param("app") Application app);

  /**
   * findTracesOfAppConstruct.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param constructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT t FROM Trace t WHERE t.app = :app AND t.lib IS NULL AND t.constructId = :constructId")
  List<Trace> findTracesOfAppConstruct(
      @Param("app") Application app, @Param("constructId") ConstructId constructId);

  /**
   * findTracesOfLibraryConstruct.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   * @param constructId a {@link com.sap.psr.vulas.backend.model.ConstructId} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT t FROM Trace t WHERE t.app = :app AND t.lib = :lib AND t.constructId = :constructId")
  List<Trace> findTracesOfLibraryConstruct(
      @Param("app") Application app,
      @Param("lib") Library lib,
      @Param("constructId") ConstructId constructId);

  /**
   * findTracesOfLibrary.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT DISTINCT t FROM Trace t "
          + " JOIN FETCH t.constructId "
          + " INNER JOIN t.lib"
          //   + " INNER JOIN t.lib l"
          //  + " LEFT OUTER JOIN FETCH l.libraryId "
          + " WHERE t.app = :app AND t.lib = :lib")
  //	@Query(value="SELECT * FROM APP_TRACE WHERE app =:app AND lib =:lib",nativeQuery=true)
  List<Trace> findTracesOfLibrary(@Param("app") Application app, @Param("lib") Library lib);

  /**
   * countTracesOfExecConstructLibrary.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param sha1 a {@link java.lang.String} object.
   * @return a {@link java.lang.Integer} object.
   */
  @Query(
      value =
          "SELECT COUNT(*) FROM "
              + " (SELECT DISTINCT app, construct_id ,lib FROM app_trace AS a"
              + "  WHERE app =:app AND lib =:lib "
              + "  ) as c  ",
      nativeQuery = true)
  Integer countTracesOfExecConstructLibrary(
      @Param("app") Application app, @Param("lib") String sha1);

  /**
   * countTracesOfLibrary.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   * @return a int.
   */
  @Query("SELECT count(t.id) FROM Trace AS t WHERE t.app = :app AND t.lib = :lib")
  int countTracesOfLibrary(@Param("app") Application app, @Param("lib") Library lib);

  //	@Query("SELECT"
  //			+ "   DISTINCT t FROM"
  //			+ "   Trace t"
  //			+ "	  JOIN FETCH t.constructId,"
  //			+ "   Bug b"
  //			+ "   JOIN b.constructChanges cc"
  //			+ " WHERE"
  //			+ "   cc.constructId = t.constructId"
  //			+ "   AND t.lib = :lib"
  //			+ "   AND t.app = :app"
  //			+ "   AND b = :bug")
  //	List<Trace> findVulnerableTracesOfLibraries(@Param("app") Application app, @Param("lib")
  // Library lib, @Param("bug") Bug bug);

  /**
   * findVulnerableTracesOfLibraries.
   *
   * @param app a {@link com.sap.psr.vulas.backend.model.Application} object.
   * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
   * @param bug_id a {@link java.lang.Long} object.
   * @return a {@link java.util.List} object.
   */
  @Query(
      "SELECT"
          + "   DISTINCT t FROM"
          + "   Trace t,"
          + "   Bug b"
          + "   JOIN b.constructChanges cc"
          + " WHERE"
          + "   cc.constructId = t.constructId"
          + "   AND t.lib = :lib"
          + "   AND t.app = :app"
          + "   AND b.id = :bug_id")
  List<Trace> findVulnerableTracesOfLibraries(
      @Param("app") Application app, @Param("lib") Library lib, @Param("bug_id") Long bug_id);
}
