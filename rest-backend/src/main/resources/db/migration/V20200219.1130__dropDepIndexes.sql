DROP INDEX if exists public.dep_app_lib_index;
DROP INDEX if exists public.dep_app_lib_parent_index;
DROP INDEX if exists public.dep_app_lib_relpath_index;
ALTER TABLE app_dependency ALTER COLUMN relativePath TYPE varchar(1024);
ALTER TABLE app_dependency ADD CONSTRAINT UKnueog86fts45j2wcql6idbqwn UNIQUE (lib, app, parent, relative_path);