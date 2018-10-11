alter table app_dependency add column origin varchar(255);
alter table app_dependency add column relative_path text;
alter table app_dependency add column parent int8;

alter table app_dependency add constraint FK3q24nj7pisqslyss56g82t7n4 foreign key (parent) references app_dependency;

alter table app_dependency drop constraint uk_bp7iv9k79w4galqwpris6yedl; -- unique (lib, app)

create index IF NOT EXISTS app_dep_index on app_dependency (lib, app);

CREATE UNIQUE INDEX IF NOT EXISTS dep_app_lib_index ON app_dependency (app,lib) where parent is NULL and relative_path is NULL;

CREATE UNIQUE INDEX IF NOT EXISTS dep_app_lib_parent_index ON app_dependency (app,lib,parent) where relative_path is NULL;

CREATE UNIQUE INDEX IF NOT EXISTS dep_app_lib_relpath_index ON app_dependency (app,lib,relative_path) where parent is NULL;