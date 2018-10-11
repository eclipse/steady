--alter table app_goal_exe_statistics rename column goal_execution_id to app_goal_exe_id;
alter table app_constructs rename column app_id to application_id;
alter table app_dependency_reachable_construct_ids rename column app_dependency_id to dependency_id;

alter table app_goal_exe_configuration rename column app_goal_exe_id to goal_execution_id;
alter table app_goal_exe_system_info rename column app_goal_exe_id to goal_execution_id;

alter table lib_constructs rename column lib_id to library_id;

alter table lib_properties rename column lib_id to library_id;