alter table bug_affected_library add column modified_at timestamp;
update bug_affected_library set modified_at = now();
alter table bug_affected_library alter column modified_at set not null;
