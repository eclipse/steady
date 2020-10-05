--
-- This file is part of Eclipse Steady.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
-- SPDX-License-Identifier: Apache-2.0
-- SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
--

-- This script determines all libraries not referenced by any dependency and
-- unknown to public package repositories.
--
-- For each such library, the corresponding entries in the relationship tables
-- lib_properties and lib_constructs are deleted. This deletion can significantly
-- reduce the database size.
--
-- However, to actually recover the disk space, one has to run a VACUUM FULL.
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
  n integer := 5000; -- Batches to be read and deleted
  
  rec RECORD;
BEGIN

  -- Get sizes before
  SELECT pg_total_relation_size(relid) FROM pg_catalog.pg_statio_user_tables where relname = 'lib_constructs' into cons_size_before;
  SELECT pg_total_relation_size(relid) FROM pg_catalog.pg_statio_user_tables where relname = 'lib_properties' into prop_size_before;
  RAISE NOTICE 'Table sizes (before)';
  RAISE NOTICE '  lib_constructs [%]', pg_size_pretty(cons_size_before); 
  RAISE NOTICE '  lib_properties [%]', pg_size_pretty(prop_size_before); 

  -- Count all un-used libs
  select count(*) from lib l where l.digest NOT IN (select distinct lib from app_dependency) and wellknown_digest = FALSE into libid_total;
  RAISE NOTICE '[%] libs are un-used', libid_total; 

  -- Loop over all un-used libs
  LOOP
    StartTime := clock_timestamp();
  
    -- Select and loop over n un-used lib ids (un-used = not referenced by app dependency)
    for rec in select distinct(l.id) from lib l where l.digest NOT IN (select distinct lib from app_dependency) and wellknown_digest = FALSE limit n offset o
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
	
	-- Exit loop when all un-used libs are processed
	EXIT WHEN libid_count = libid_total;
	
	o = o + n;
  END LOOP;
END first_block $$;
