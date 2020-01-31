DO $$ 
<<first_block>>
DECLARE
  libid_total integer := null;
  libid integer := null;
  libid_count integer := 0;
  
  prop_count integer := 0;
  prop_del_total bigint := 0;
  prop_size_before bigint := 0;
  prop_size_current bigint := 0;

  cons_count integer := 0;
  cons_del_total bigint := 0;
  cons_size_before bigint := 0;
  cons_size_current bigint := 0;
  
  StartTime timestamptz;
  EndTime timestamptz;
  Delta double precision;
  
  o integer := 0;
  n integer := 5000;
  
  rec RECORD;
BEGIN

  -- Get sizes before
  SELECT pg_total_relation_size(relid) FROM pg_catalog.pg_statio_user_tables where relname = 'lib_constructs' into cons_size_before;
  SELECT pg_total_relation_size(relid) FROM pg_catalog.pg_statio_user_tables where relname = 'lib_properties' into prop_size_before;
  RAISE NOTICE 'Table sizes (before)';
  RAISE NOTICE '  lib_constructs [%]', pg_size_pretty(cons_size_before); 
  RAISE NOTICE '  lib_properties [%]', pg_size_pretty(prop_size_before); 

  -- Count all un-used libs
  select count(*) from lib l where l.digest NOT IN (select distinct lib from app_dependency) into libid_total;
  RAISE NOTICE '[%] libs are un-used', libid_total; 

  -- Loop over all un-used libs
  LOOP
    StartTime := clock_timestamp();
  
    -- Select and loop over n un-used lib ids (un-used = not referenced by app dependency)
    for rec in select distinct(l.id) from lib l where l.digest NOT IN (select distinct lib from app_dependency) limit n offset o
	loop
	  libid = rec.id;
	  libid_count = libid_count + 1;
      BEGIN  
        -- Prop rels not needed
        --select count(*) from lib_properties lp where lp.library_id = libid into prop_count;
        delete from lib_properties lp where lp.library_id = libid;
	    GET DIAGNOSTICS prop_count = ROW_COUNT;
		prop_del_total = prop_del_total + prop_count;
		
        -- Construct rels not needed
        --select count(*) from lib_constructs lc where lc.library_id = libid into cons_count;
	    delete from lib_constructs lc where lc.library_id = libid;
	    GET DIAGNOSTICS cons_count = ROW_COUNT;
		cons_del_total = cons_del_total + cons_count;
		
	--RAISE NOTICE '[%] Processed un-used lib [id=%]: Deleted [%] construct rels and [%] property rels', libid_count, libid, cons_count, prop_count; 
		
	    COMMIT;
	  END;	  
	END LOOP;
	
	-- Time
	EndTime := clock_timestamp();
	Delta := 1000 * ( extract(epoch from EndTime) - extract(epoch from StartTime) );
    RAISE NOTICE '[%] millisecs to process next [%] libraries: Processed [%] libs, deleted [%] construct rels and [%] property rels', Delta, n, libid_count, cons_del_total, prop_del_total;
		
	-- Get sizes current (note: useless: sizes will only change when VACUUM FULL is called)
    SELECT pg_total_relation_size(relid) FROM pg_catalog.pg_statio_user_tables where relname = 'lib_constructs' into cons_size_current;
    SELECT pg_total_relation_size(relid) FROM pg_catalog.pg_statio_user_tables where relname = 'lib_properties' into prop_size_current;
    RAISE NOTICE 'Table sizes (before, current, saving)';
    RAISE NOTICE '  lib_constructs [%], [%], [%]', pg_size_pretty(cons_size_before), pg_size_pretty(cons_size_current), pg_size_pretty(cons_size_before - cons_size_current); 
    RAISE NOTICE '  lib_properties [%], [%], [%]', pg_size_pretty(prop_size_before), pg_size_pretty(prop_size_current), pg_size_pretty(prop_size_before - prop_size_current); 
	
	o = o + n;
  END LOOP;
END first_block $$;

--SELECT pg_database.datname, pg_size_pretty(pg_database_size(pg_database.datname)) AS size FROM pg_database;
-- ==> vulas 274GB
-- SELECT relname as "Table",
--   pg_size_pretty(pg_total_relation_size(relid)) As "Size",
--   pg_size_pretty(pg_total_relation_size(relid) - pg_relation_size(relid)) as "External Size"
--   FROM pg_catalog.pg_statio_user_tables ORDER BY pg_total_relation_size(relid) DESC; 
 
