DROP INDEX if exists public.dep_app_lib_index;
DROP INDEX if exists public.dep_app_lib_parent_index;
DROP INDEX if exists public.dep_app_lib_relpath_index;
alter table app_dependency add constraint UKnueog86fts45j2wcql6idbqwn unique (lib, app, parent, relative_path);