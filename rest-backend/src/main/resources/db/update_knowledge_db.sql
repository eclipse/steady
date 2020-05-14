--
-- DESCRIPTION
--     PL/pgSQL code to update the open-source vulnerability assessment 
--     knowledge database base. The following script updates the following 
--     tables of the vulnas db: 
--          * bug
--          * bug_affected_library
--          * bug_construct_change
--          * bug_references
--          * construct_id
--          * library_id
--
-- PARAMETERS
--     The script has multiple input parameters.
--     The lines have been commented with -- @parameter
--     The following parameters have to be updated 
--          * The path to the exported db data
--          * Name of the owner of the tables
--          * Possibly the constraint names
--
-- CHANGE HISTORY
--     11 May 2020 - eren-ck (UKON)
--        * Initial version
--

-- Use UTF-8 encoding
SET CLIENT_ENCODING TO 'utf8';

--
-- Define functions
--

-- Replace ids of the bug table
CREATE FUNCTION update_bug_table_ids() RETURNS integer
AS
$body$
DECLARE
	c_ids CURSOR FOR
		SELECT bug.id, new_bug.id
			FROM bug, new_bug
			WHERE bug.bug_id = new_bug.bug_id;
	v_old_id bigint;
	v_new_id bigint;
	counter bigint := 0;
BEGIN
	OPEN c_ids;
	LOOP
	FETCH c_ids INTO v_old_id, v_new_id;
		EXIT WHEN NOT FOUND;
		IF NOT EXISTS(SELECT * FROM bug WHERE bug.id = v_new_id LIMIT 1) THEN
			IF (v_old_id != v_new_id) THEN
				--RAISE NOTICE '% -> %', v_old_id, v_new_id;
				UPDATE bug SET id = v_new_id WHERE id = v_old_id;
				counter := counter + 1;
			END IF;
		END IF;
	END LOOP;
	CLOSE c_ids;
	RETURN counter;
END;
$body$
LANGUAGE plpgsql;

CREATE FUNCTION loop_update_bug_table_ids() RETURNS integer
AS
$body$
DECLARE
  remaining integer;
BEGIN
  LOOP
 	 SELECT * INTO remaining FROM update_bug_table_ids();
     --RAISE NOTICE '%', remaining;
	 EXIT WHEN remaining <= 0;
  END LOOP;
  RETURN remaining;
END;
$body$
LANGUAGE plpgsql;

-- Replace ids of the library_id table

CREATE FUNCTION update_library_id_table() RETURNS integer
AS
$body$
DECLARE
	c_ids CURSOR FOR
		SELECT library_id.id, new_library_id.id
			FROM library_id, new_library_id
			WHERE library_id.artifact = new_library_id.artifact
				AND library_id.mvn_group = new_library_id.mvn_group
				AND library_id.version = new_library_id.version;
	v_old_id bigint;
	v_new_id bigint;
	counter bigint := 0;
BEGIN
	OPEN c_ids;
	LOOP
	FETCH c_ids INTO v_old_id, v_new_id;
		EXIT WHEN NOT FOUND;
		IF NOT EXISTS(SELECT * FROM library_id WHERE library_id.id = v_new_id LIMIT 1) THEN
			IF (v_old_id != v_new_id) THEN
				--RAISE NOTICE '% -> %', v_old_id, v_new_id;
				UPDATE library_id SET id = v_new_id WHERE id = v_old_id;
				counter := counter + 1;
			END IF;
		END IF;
	END LOOP;
	CLOSE c_ids;
	RETURN counter;
END;
$body$
LANGUAGE plpgsql;

CREATE FUNCTION loop_update_library_id_table() RETURNS integer
AS
$body$
DECLARE
  remaining integer;
BEGIN
  LOOP
 	 SELECT * INTO remaining FROM update_library_id_table();
     --RAISE NOTICE '%', remaining;
	 EXIT WHEN remaining <= 0;
  END LOOP;
  RETURN remaining;
END;
$body$
LANGUAGE plpgsql;

-- Replace ids of the construct_id table

CREATE FUNCTION update_construct_id_table() RETURNS integer
AS
$body$
DECLARE
	c_ids CURSOR FOR
		SELECT construct_id.id AS old_id, new_construct_id.id AS new_id
			FROM construct_id, new_construct_id
			WHERE construct_id.lang = new_construct_id.lang
				AND construct_id.qname = new_construct_id.qname
				AND construct_id.type = new_construct_id.type;
	v_old_id bigint;
	v_new_id bigint;
	counter bigint := 0;
BEGIN
	OPEN c_ids;
	LOOP
	FETCH c_ids INTO v_old_id, v_new_id;
		EXIT WHEN NOT FOUND;
		IF NOT EXISTS(SELECT * FROM construct_id WHERE construct_id.id = v_new_id LIMIT 1) THEN
			IF (v_old_id != v_new_id) THEN
				--RAISE NOTICE '% -> %', v_old_id, v_new_id;
				UPDATE construct_id SET id = v_new_id WHERE id = v_old_id;
				counter := counter + 1;
			END IF;
		END IF;
	END LOOP;
	CLOSE c_ids;
	RETURN counter;
END;
$body$
LANGUAGE plpgsql;

CREATE FUNCTION loop_update_construct_id_table() RETURNS integer
AS
$body$
DECLARE
  remaining integer;
BEGIN
  LOOP
 	 SELECT * INTO remaining FROM update_construct_id_table();
     --RAISE NOTICE '%', remaining;
	 EXIT WHEN remaining <= 0;
  END LOOP;
  RETURN remaining;
END;
$body$
LANGUAGE plpgsql;

--
-- Update database
--

-- Enable cascading on update an delete of foreign keys

ALTER TABLE lib
DROP CONSTRAINT fk_y66eipsxk2mhkdopivh9f4rn, -- @parameter
ADD CONSTRAINT fk_y66eipsxk2mhkdopivh9f4rn -- @parameter
    FOREIGN KEY (library_id_id)
    REFERENCES library_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE lib_bundled_library_ids
DROP CONSTRAINT fkpfkpmqs18pra09mdrt5ln4qui, -- @parameter
ADD CONSTRAINT fkpfkpmqs18pra09mdrt5ln4qui -- @parameter
    FOREIGN KEY (bundled_library_ids_id)
    REFERENCES library_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE bug_affected_library
DROP CONSTRAINT fk_57ldgkgmrn98wtkrp8k7tnwe, -- @parameter
ADD CONSTRAINT fk_57ldgkgmrn98wtkrp8k7tnwe -- @parameter
    FOREIGN KEY (library_id)
    REFERENCES library_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE bug_affected_construct_change_same_bytecode_lids
DROP CONSTRAINT fkfirn9bju9powr15m6rd5883g3, -- @parameter
ADD CONSTRAINT fkfirn9bju9powr15m6rd5883g3 -- @parameter
    FOREIGN KEY (same_bytecode_lids_id)
    REFERENCES library_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE bug_references
DROP CONSTRAINT fkb05vl8sr4x7u4dsvrfupb79nh,
ADD CONSTRAINT fkb05vl8sr4x7u4dsvrfupb79nh
    FOREIGN KEY (bug_id)
    REFERENCES bug (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE app_constructs
DROP CONSTRAINT fk_h47hsv2r1pdicng39emhhxmtg, -- @parameter
ADD CONSTRAINT fk_h47hsv2r1pdicng39emhhxmtg -- @parameter
    FOREIGN KEY (constructs_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE app_dependency_reachable_construct_ids
DROP CONSTRAINT fk_2jd4jp00y9e6agxmjobraqadw, -- @parameter
ADD CONSTRAINT fk_2jd4jp00y9e6agxmjobraqadw -- @parameter
    FOREIGN KEY (reachable_construct_ids_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE app_dependency_touch_points
DROP CONSTRAINT fk_bun1ayax18wu6fk614hsocmvb, -- @parameter
ADD CONSTRAINT fk_bun1ayax18wu6fk614hsocmvb -- @parameter
    FOREIGN KEY (from_construct_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE app_dependency_touch_points
DROP CONSTRAINT fk_jdm5c0k5hb2royytvjmkgn1m7, -- @parameter
ADD CONSTRAINT fk_jdm5c0k5hb2royytvjmkgn1m7 -- @parameter
    FOREIGN KEY (to_construct_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE lib_constructs
DROP CONSTRAINT fk_pfk33vengljm5qa5bkfsp56vn, -- @parameter
ADD CONSTRAINT fk_pfk33vengljm5qa5bkfsp56vn -- @parameter
    FOREIGN KEY (constructs_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE app_path_path
DROP CONSTRAINT fk_qxer1tis3iutifb5gn7k2av30, -- @parameter
ADD CONSTRAINT fk_qxer1tis3iutifb5gn7k2av30 -- @parameter
    FOREIGN KEY (construct_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE app_path
DROP CONSTRAINT fk_7h30fx3yc59q56knty7t33wlj, -- @parameter
ADD CONSTRAINT fk_7h30fx3yc59q56knty7t33wlj -- @parameter
    FOREIGN KEY (end_construct_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE app_path
DROP CONSTRAINT fk_r1okg9en9y4194ndmjstpw5nv, -- @parameter
ADD CONSTRAINT fk_r1okg9en9y4194ndmjstpw5nv -- @parameter
    FOREIGN KEY (start_construct_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE app_trace
DROP CONSTRAINT fk_7kj7j0b1qq0j9x0fymkawomtc,
ADD CONSTRAINT fk_7kj7j0b1qq0j9x0fymkawomtc
    FOREIGN KEY (construct_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE bug_construct_change
DROP CONSTRAINT fk_6bpg570tuwe3febcd8gflu2bl, -- @parameter
ADD CONSTRAINT fk_6bpg570tuwe3febcd8gflu2bl -- @parameter
    FOREIGN KEY (construct_id)
    REFERENCES construct_id (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

ALTER TABLE bug_affected_construct_change
DROP CONSTRAINT fk_49ig9c71ttnmsowf1v6yx36v0, -- @parameter
    ADD CONSTRAINT fk_49ig9c71ttnmsowf1v6yx36v0 -- @parameter
    FOREIGN KEY (bug_construct_change)
    REFERENCES bug_construct_change (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;
	
ALTER TABLE bug_affected_construct_change
DROP CONSTRAINT fk_7dap5l5oskn621gbnfh5fbhc9, -- @parameter
    ADD CONSTRAINT fk_7dap5l5oskn621gbnfh5fbhc9 -- @parameter
    FOREIGN KEY (affected_lib)
    REFERENCES bug_affected_library (id)
    ON UPDATE CASCADE
    ON DELETE NO ACTION;

-- Create temporary tables for the data

CREATE TABLE new_bug (like bug including all);
CREATE TABLE new_bug_affected_library (like bug_affected_library including all);
CREATE TABLE new_bug_construct_change (like bug_construct_change including all);
CREATE TABLE new_bug_references (like bug_references including all);
CREATE TABLE new_construct_id (like construct_id including all);
CREATE TABLE new_library_id (like library_id including all);

-- Copy data from files into temporary tables
-- Currently relative path used - absolute also possible

\copy new_bug FROM 'bug.sql'; -- @parameter
--COPY new_bug FROM 'bug.sql';

\copy new_bug_affected_library FROM 'bug_affected_library.sql'; -- @parameter
--COPY new_bug_affected_library FROM 'bug_affected_library.sql'

\copy new_bug_construct_change FROM 'bug_construct_change.sql'; -- @parameter
--COPY new_bug_construct_change FROM 'bug_construct_change.sql'

\copy new_bug_references FROM 'bug_references.sql'; -- @parameter
--COPY new_bug_references FROM 'bug_references.sql'

\copy new_construct_id FROM 'construct_id.sql'; -- @parameter
--COPY new_construct_id FROM 'construct_id.sql'

\copy new_library_id FROM 'library_id.sql'; -- @parameter
--COPY new_library_id FROM 'library_id.sql'

-- Should return 0
SELECT loop_update_bug_table_ids();

-- Insert new bugs
INSERT INTO bug SELECT new_bug.* FROM new_bug, (SELECT new_bug.id FROM new_bug EXCEPT ALL SELECT bug.id FROM bug) AS tmp_bug
	WHERE new_bug.id = tmp_bug.id;

-- Should return 0
SELECT loop_update_library_id_table();

-- Insert new library_ids
INSERT INTO library_id SELECT new_library_id.* FROM new_library_id, (SELECT new_library_id.id FROM new_library_id EXCEPT ALL SELECT library_id.id FROM library_id) AS tmp_library_id
	WHERE new_library_id.id = tmp_library_id.id;

-- Should return 0
SELECT loop_update_construct_id_table();

-- Insert new construct_ids
INSERT INTO construct_id SELECT new_construct_id.* FROM new_construct_id, (SELECT new_construct_id.id FROM new_construct_id EXCEPT ALL SELECT construct_id.id FROM construct_id) AS tmp_construct_id
	WHERE new_construct_id.id = tmp_construct_id.id;

-- bug_affected_library is empty, insert all as is
INSERT INTO bug_affected_library SELECT * FROM new_bug_affected_library;

-- Create temporary bug_affected_construct_change with serial id

CREATE TABLE public.tmp_bug_construct_change
(
    id SERIAL NOT NULL,
    body_change text,
    buggy_body text,
    commit character varying(255),
    committed_at timestamp without time zone,
    construct_change_type character varying(3) NOT NULL,
    fixed_body text,
    repo character varying(255),
    repo_path character varying(255),
    bug character varying(32) NOT NULL,
    construct_id bigint NOT NULL,
    CONSTRAINT tmp_bug_construct_change_pkey PRIMARY KEY (id),
    CONSTRAINT tmp_bug_construct_change_bug_repo_commit_repo_path_construc_key UNIQUE (bug, repo, commit, repo_path, construct_id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.tmp_bug_construct_change
    OWNER to postgres; -- @parameter

-- Insert old data in temporary table
INSERT INTO tmp_bug_construct_change (body_change, buggy_body, commit, committed_at, construct_change_type, fixed_body, repo, repo_path, bug, construct_id)
	SELECT body_change, buggy_body, commit, committed_at, construct_change_type, fixed_body, repo, repo_path, bug, construct_id
	FROM bug_construct_change;

-- Insert new data in temporary table
INSERT INTO tmp_bug_construct_change (body_change, buggy_body, commit, committed_at, construct_change_type, fixed_body, repo, repo_path, bug, construct_id)
	SELECT body_change, buggy_body, commit, committed_at, construct_change_type, fixed_body, repo, repo_path, bug, construct_id
	FROM new_bug_construct_change;

-- Clear bug_construct_change table
DELETE FROM bug_construct_change;

-- Copy new data from temporary table
INSERT INTO bug_construct_change SELECT * FROM tmp_bug_construct_change;

-- Drop all tables
DROP TABLE new_bug;
DROP TABLE new_bug_affected_library;
DROP TABLE new_bug_construct_change;
DROP TABLE new_bug_references;
DROP TABLE new_construct_id;
DROP TABLE tmp_bug_construct_change;

-- Drop all functions
DROP FUNCTION update_bug_table_ids;
DROP FUNCTION loop_update_bug_table_ids;
DROP FUNCTION update_library_id_table;
DROP FUNCTION loop_update_library_id_table;
DROP FUNCTION update_construct_id_table;
DROP FUNCTION loop_update_construct_id_table;