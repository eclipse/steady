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
--     23 Sep 2020 - eren-ck (UKON)
--        * Requested changes
--        * New entries are assigned a new id for the hibernate_sequence sequence
--        * Existing entries are updated if the ids suggest that it is the same item
--

-- Use UTF-8 encoding
SET CLIENT_ENCODING TO 'utf8';

--
-- Copy data from files into temporary tables
--

CREATE TABLE new_bug (like bug including all);
CREATE TABLE new_bug_affected_library (like bug_affected_library including all);
CREATE TABLE new_bug_construct_change (like bug_construct_change including all);
CREATE TABLE new_bug_references (like bug_references including all);
CREATE TABLE new_construct_id (like construct_id including all);
CREATE TABLE new_library_id (like library_id including all);

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

--
-- Update bug_references table (references bug via bug_id)
--

-- Update existing bug_references without changing ids

CREATE FUNCTION update_existing_bug_references() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for bugs that are already in the database
	c_ids CURSOR FOR
		SELECT bug.id, new_bug.id FROM bug, new_bug
			WHERE bug.bug_id = new_bug.bug_id;
	r_new_bug_references new_bug_references%rowtype;
	v_old_id bigint;
	v_new_id bigint;
	v_counter bigint := 0;
BEGIN
	OPEN c_ids;
	LOOP
	FETCH c_ids INTO v_old_id, v_new_id;
		EXIT WHEN NOT FOUND;
		-- Check if an actual entry existing
		IF EXISTS (SELECT bug_id FROM new_bug_references WHERE bug_id = v_new_id) THEN
			SELECT * INTO r_new_bug_references FROM new_bug_references WHERE new_bug_references.bug_id = v_new_id;
			-- Insert existing entry in-case of new reference, keep original id
			INSERT INTO bug_references VALUES (v_old_id, r_new_bug_references.reference) ON CONFLICT DO NOTHING;
			-- Delete the entry from the source table
			DELETE FROM new_bug_references WHERE bug_id = v_new_id;
			v_counter := v_counter + 1;
		--ELSE
		--	RAISE NOTICE 'bug_id % not found in new_bug_references.', v_new_id;
		END IF;
	END LOOP;
	CLOSE c_ids;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

SELECT update_existing_bug_references();

DROP FUNCTION update_existing_bug_references;

--
-- Update bug table
--

-- Update existing bugs without changing ids

CREATE FUNCTION update_existing_bugs() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for bugs that are already in the database
	c_ids CURSOR FOR
		SELECT bug.id, new_bug.id
			FROM bug, new_bug
			WHERE bug.bug_id = new_bug.bug_id;
	r_new_bug new_bug%rowtype;
	v_old_id bigint;
	v_new_id bigint;
	v_counter bigint := 0;
BEGIN
	OPEN c_ids;
	LOOP
	FETCH c_ids INTO v_old_id, v_new_id;
		EXIT WHEN NOT FOUND;
		SELECT * INTO r_new_bug FROM new_bug WHERE new_bug.id = v_new_id;
		--RAISE NOTICE 'Updating bug entry %.', r_new_bug.bug_id;
		-- Update existing entry, keep original id and bug_id
		UPDATE bug SET 
			created_at = r_new_bug.created_at,
			created_by = r_new_bug.created_by,
			description = r_new_bug.description,
			modified_at = r_new_bug.modified_at,
			modified_by = r_new_bug.modified_by,
			bug_id_alt = r_new_bug.bug_id_alt,
			cvss_score = r_new_bug.cvss_score,
			cvss_vector = r_new_bug.cvss_vector,
			cvss_version = r_new_bug.cvss_version,
			maturity = r_new_bug.maturity,
			origin = r_new_bug.origin,
			description_alt = r_new_bug.description_alt
				WHERE id = v_old_id;
		-- Delete the entry from the source table
		DELETE FROM new_bug WHERE id = v_new_id;
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_ids;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

SELECT update_existing_bugs();

DROP FUNCTION update_existing_bugs;

-- Add new bugs and bug_references to the database and assign a new id

CREATE FUNCTION add_new_bugs() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for bugs that are new to the database
	c_new_bugs CURSOR FOR SELECT * FROM new_bug;
	r_new_bug new_bug%rowtype;
	r_new_bug_references new_bug_references%rowtype;
	v_new_id bigint;
	v_counter bigint := 0;
BEGIN
	OPEN c_new_bugs;
	LOOP
	FETCH c_new_bugs INTO r_new_bug;
		EXIT WHEN NOT FOUND;
		--RAISE NOTICE 'Adding bug %.', r_new_bug.bug_id;
		-- Insert new bug, generate id from sequence
		v_new_id = nextval('hibernate_sequence');
		INSERT INTO bug VALUES (
			v_new_id,
			r_new_bug.bug_id,
			r_new_bug.created_at,
			r_new_bug.created_by,
			r_new_bug.description,
			r_new_bug.modified_at,
			r_new_bug.modified_by,
			r_new_bug.bug_id_alt,
			r_new_bug.cvss_score,
			r_new_bug.cvss_vector,
			r_new_bug.cvss_version,
			r_new_bug.maturity,
			r_new_bug.origin,
			r_new_bug.description_alt);
		-- Insert new bug_references entry if one exists but use new bug_id
		IF EXISTS (SELECT bug_id FROM new_bug_references WHERE new_bug_references.bug_id = r_new_bug.id) THEN
			SELECT * INTO r_new_bug_references FROM new_bug_references WHERE new_bug_references.bug_id = r_new_bug.id;
			INSERT INTO bug_references VALUES (v_new_id, r_new_bug_references.reference);
		--ELSE
		--	RAISE NOTICE 'bug_id % not found in new_bug_references.', r_new_bug.id;
		END IF;
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_new_bugs;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

SELECT add_new_bugs();

DROP FUNCTION add_new_bugs;

-- Drop the temporary new_bug and new_bug_references table
DROP TABLE new_bug_references;
DROP TABLE new_bug;

--
-- Update library_id table
--

-- Update existing library_ids without changing ids

CREATE FUNCTION delete_existing_library_ids() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for library_ids that are already in the database
	c_ids CURSOR FOR
		SELECT new_library_id.id
			FROM library_id, new_library_id
			-- A library_id entry is defined by all non id columns
			WHERE library_id.artifact = new_library_id.artifact
				AND library_id.mvn_group = new_library_id.mvn_group
				AND library_id.version = new_library_id.version;
	v_new_id bigint;
	v_counter bigint := 0;
BEGIN
	OPEN c_ids;
	LOOP
	FETCH c_ids INTO v_new_id;
		EXIT WHEN NOT FOUND;
		-- Delete the entry from the source table
		DELETE FROM new_library_id WHERE id = v_new_id;
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_ids;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

SELECT delete_existing_library_ids();

DROP FUNCTION delete_existing_library_ids;

-- Add new library_ids to the database and assign a new id

CREATE FUNCTION add_new_library_ids() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for library_ids that are new to the database
	c_new_library_ids CURSOR FOR SELECT * FROM new_library_id;
	r_new_library_id new_library_id%rowtype;
	v_counter bigint := 0;
BEGIN
	OPEN c_new_library_ids;
	LOOP
	FETCH c_new_library_ids INTO r_new_library_id;
		EXIT WHEN NOT FOUND;
		-- Insert new library_id, generate id from sequence
		INSERT INTO library_id VALUES (
			nextval('hibernate_sequence'),
			r_new_library_id.artifact,
			r_new_library_id.mvn_group,
			r_new_library_id.version
		);
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_new_library_ids;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

SELECT add_new_library_ids();

DROP FUNCTION add_new_library_ids;

-- Drop the temporary new_library_id table
DROP TABLE new_library_id;

--
-- Update bug_affected_library table (references library_id)
--

-- Delete existing bug_affected_libraries from source table

CREATE FUNCTION delete_existing_bug_affected_libraries() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for bug_affected_libraries that are already in the database
	c_ids CURSOR FOR
		SELECT new_bug_affected_library.id
			FROM bug_affected_library, new_bug_affected_library
			WHERE bug_affected_library.bug_id = new_bug_affected_library.bug_id
				AND bug_affected_library.library_id = new_bug_affected_library.library_id
				AND bug_affected_library.source = new_bug_affected_library.source;
	r_new_bug_construct_change new_bug_construct_change%rowtype;
	v_new_id bigint := 0;
	v_counter bigint := 0;
BEGIN
	OPEN c_ids;
	LOOP
	FETCH c_ids INTO v_new_id;
		EXIT WHEN NOT FOUND;
		DELETE FROM new_bug_affected_library WHERE id = v_new_id;
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_ids;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

SELECT delete_existing_bug_affected_libraries();

DROP FUNCTION delete_existing_bug_affected_libraries;

-- Add new bug_affected_libraries to the database and assign a new id

CREATE FUNCTION add_bug_affected_libraries() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for bug_affected_libraries that are new to the database
	c_new_bug_affected_libraries CURSOR FOR SELECT * FROM new_bug_affected_library;
	r_new_bug_affected_library new_bug_affected_library%rowtype;
	v_library_id bigint;
	v_counter bigint := 0;
BEGIN
	OPEN c_new_bug_affected_libraries;
	LOOP
	FETCH c_new_bug_affected_libraries INTO r_new_bug_affected_library;
		EXIT WHEN NOT FOUND;
		-- Retrieve id of the referenced library_id
		SELECT library_id.id INTO v_library_id FROM library_id, new_library_id
			WHERE library_id.artifact = new_library_id.artifact
				AND library_id.mvn_group = new_library_id.mvn_group
				AND library_id.version = library_id.version
				AND new_library_id.id = r_new_bug_affected_library.library_id;
		-- Insert new ug_affected_library, generate id from sequence
		INSERT INTO bug_affected_library VALUES (
			nextval('hibernate_sequence'),
			r_new_bug_affected_library.affected,
			r_new_bug_affected_library.created_at,
			r_new_bug_affected_library.created_by,
			r_new_bug_affected_library.explanation,
			r_new_bug_affected_library.first_fixed,
			r_new_bug_affected_library.from_intersection,
			r_new_bug_affected_library.last_vulnerable,
			r_new_bug_affected_library.source,
			r_new_bug_affected_library.to_intersection,
			r_new_bug_affected_library.bug_id,
			r_new_bug_affected_library.lib,
			v_library_id,
			r_new_bug_affected_library.adfixed,
			r_new_bug_affected_library.adpath_fixed,
			r_new_bug_affected_library.overall_confidence,
			r_new_bug_affected_library.path_confidence,
			r_new_bug_affected_library.sources_available,
			r_new_bug_affected_library.modified_at 
		) ON CONFLICT DO NOTHING;
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_new_bug_affected_libraries;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

-- Restore new_library_id table for look-up
CREATE TABLE new_library_id (like library_id including all);
\copy new_library_id FROM 'library_id.sql';
--COPY new_library_id FROM 'library_id.sql'

SELECT add_bug_affected_libraries();

DROP FUNCTION add_bug_affected_libraries();

DROP TABLE new_library_id;
DROP TABLE new_bug_affected_library;

--
-- Update construct_id table
--

-- Update existing construct_ids without changing ids

CREATE FUNCTION delete_existing_construct_ids() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for construct_ids that are already in the database
	c_ids CURSOR FOR
		SELECT new_construct_id.id
			FROM construct_id, new_construct_id
			-- A construct_id entry is defined by all non id columns
			WHERE construct_id.lang = new_construct_id.lang
				AND construct_id.qname = new_construct_id.qname
				AND construct_id.type = new_construct_id.type;
	v_new_id bigint;
	v_counter bigint := 0;
BEGIN
	OPEN c_ids;
	LOOP
	FETCH c_ids INTO v_new_id;
		EXIT WHEN NOT FOUND;
		-- Delete the entry from the source table
		DELETE FROM new_construct_id WHERE id = v_new_id;
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_ids;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

SELECT delete_existing_construct_ids();

DROP FUNCTION delete_existing_construct_ids;

-- Add new construct_ids to the database and assign a new id

CREATE FUNCTION add_new_construct_ids() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for construct_ids that are new to the database
	c_new_construct_ids CURSOR FOR SELECT * FROM new_construct_id;
	r_new_construct_id new_construct_id%rowtype;
	v_counter bigint := 0;
BEGIN
	OPEN c_new_construct_ids;
	LOOP
	FETCH c_new_construct_ids INTO r_new_construct_id;
		EXIT WHEN NOT FOUND;
		-- Insert new bug, generate id from sequence
		INSERT INTO construct_id VALUES (
			nextval('hibernate_sequence'),
			r_new_construct_id.lang,
			r_new_construct_id.qname,
			r_new_construct_id.type
		);
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_new_construct_ids;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

SELECT add_new_construct_ids();

DROP FUNCTION add_new_construct_ids;

-- Drop the temporary new_construct_id table
DROP TABLE new_construct_id;

--
-- Update bug_construct_change table (references construct_id)
--

CREATE FUNCTION update_existing_bug_construct_changes() RETURNS integer
AS
$body$
DECLARE
	-- Cursor for bugs that are already in the database
	c_ids CURSOR FOR
		SELECT bug_construct_change.id, new_bug_construct_change.id
			FROM bug_construct_change, new_bug_construct_change
			WHERE bug_construct_change.bug = new_bug_construct_change.bug
				AND bug_construct_change.construct_id = new_bug_construct_change.construct_id;
	r_new_bug_construct_change new_bug_construct_change%rowtype;
	v_old_id bigint;
	v_new_id bigint;
	v_counter bigint := 0;
BEGIN
	OPEN c_ids;
	LOOP
	FETCH c_ids INTO v_old_id, v_new_id;
		EXIT WHEN NOT FOUND;
		SELECT * INTO r_new_bug_construct_change FROM new_bug_construct_change WHERE r_new_bug_construct_change.bug_id = v_new_id;
		-- Update existing entry, keep original id
		UPDATE bug_construct_change SET
			body_change = r_new_bug_construct_change.body_change,
			buggy_body = r_new_bug_construct_change.buggy_body,
			commit = r_new_bug_construct_change.commit,
			committed_at = r_new_bug_construct_change.committed_at,
			construct_change_type =r_new_bug_construct_change.construct_change_type,
			fixed_body = r_new_bug_construct_change.fixed_body,
			repo = r_new_bug_construct_change.repo,
			repo_path = r_new_bug_construct_change.repo_path
				WHERE id = v_old_id;
		-- Delete the entry from the source table
		DELETE FROM new_bug_construct_change WHERE id = v_new_id;
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_ids;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

SELECT update_existing_bug_construct_changes();

DROP FUNCTION update_existing_bug_construct_changes;

-- Add new bug_construct_changes to the database and assign a new id

CREATE FUNCTION add_new_bug_construct_changes() RETURNS integer
AS
$body$
DECLARE
	c_new_bug_construct_changes CURSOR FOR SELECT * FROM new_bug_construct_change;
	r_new_bug_construct_change new_bug_construct_change%rowtype;
	v_construct_id bigint;
	v_counter bigint := 0;
BEGIN
	OPEN c_new_bug_construct_changes;
	LOOP
	FETCH c_new_bug_construct_changes INTO r_new_bug_construct_change;
		EXIT WHEN NOT FOUND;
		-- Retrieve id of the referenced construct_id
		SELECT construct_id.id INTO v_construct_id FROM construct_id, new_construct_id
			WHERE construct_id.lang = new_construct_id.lang
				AND construct_id.qname = new_construct_id.qname
				AND construct_id.type = new_construct_id.type
				AND new_construct_id.id = r_new_bug_construct_change.construct_id;
		-- Insert new bug_construct_change, generate id from sequence
		INSERT INTO bug_construct_change VALUES (
			nextval('hibernate_sequence'),
			r_new_bug_construct_change.body_change,
			r_new_bug_construct_change.buggy_body,
			r_new_bug_construct_change.commit,
			r_new_bug_construct_change.committed_at,
			r_new_bug_construct_change.construct_change_type,
			r_new_bug_construct_change.fixed_body,
			r_new_bug_construct_change.repo,
			r_new_bug_construct_change.repo_path,
			r_new_bug_construct_change.bug,
			v_construct_id) ON CONFLICT DO NOTHING;
		v_counter := v_counter + 1;
	END LOOP;
	CLOSE c_new_bug_construct_changes;
	RETURN v_counter;
END;
$body$
LANGUAGE plpgsql;

-- Restore new_construct_id table for look-up
CREATE TABLE new_construct_id (like construct_id including all);
\copy new_construct_id FROM 'construct_id.sql';
--COPY new_construct_id FROM 'construct_id.sql'

SELECT add_new_bug_construct_changes();

DROP FUNCTION add_new_bug_construct_changes;

DROP TABLE new_construct_id;

DROP TABLE new_bug_construct_change;