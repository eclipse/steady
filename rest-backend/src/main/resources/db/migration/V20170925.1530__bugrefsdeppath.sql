create table bug_references (bug_id int8 not null, reference varchar(1024) not null, primary key (bug_id,reference));
alter table bug_references add constraint FKb05vl8sr4x7u4dsvrfupb79nh foreign key (bug_id) references bug;
alter table app_dependency add column path text;

INSERT INTO bug_references (bug_id,reference) SELECT id,url FROM bug where url is not null and not url='';

ALTER TABLE bug DROP COLUMN url;