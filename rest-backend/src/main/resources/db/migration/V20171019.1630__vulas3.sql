
alter table app add column space int8; --add column as nullable as we have table with values, it will be modified to not null at the end, after all data will be updated
alter table app drop constraint uk_apcod7vgdms2hvqj0r88hg5is;
alter table app add constraint UKknc4gox22ud8lqyea1lsmby4f unique (space, mvn_group, artifact, version);

--drop all foreign key constraints on sha1 to be able to recreate constraints with digest within the name
alter table app_dependency drop constraint fk_3mlqqni3f6jkaqocyr6hmwk99; 
alter table app_path drop constraint fk_fitmj70jike9xsrr5gisuh3gs;
alter table app_path_path drop constraint fk_sgpx08w3s0u2ukd2wq270wrvt;
alter table app_trace drop constraint fk_1353i6occf8ric1hxt80s0mhr;
alter table bug_affected_library drop constraint fk_95etulg7h15qv5n5p1gmgwdoq;


--drop view
drop view v_app_vulndep;
drop view v_app_vulndep_cc;
drop view v_app_vulndep_config;




alter table lib drop constraint sha1_index;
alter table lib  rename column sha1 to digest;


--recreate all constraints
alter table lib add constraint digest_index unique (digest);
alter table app_dependency add constraint FK73rma7xomek1tr8hfy764wru0 foreign key (lib) references lib (digest);
alter table app_path add constraint FKqtvs606b8h7ys4rhi8eglivv0 foreign key (lib) references lib (digest);
alter table app_path_path add constraint FKek8519yw5qgd3myx44jsurkmc foreign key (lib) references lib (digest);
alter table app_trace add constraint FK7c9ydxi9hoegtogjf90lkibya foreign key (lib) references lib (digest);
alter table bug_affected_library add constraint FKd8v4sl3vxpv9mrqawvnvxfv0b foreign key (lib) references lib (digest);


-- add new/rename column
alter table lib add column digest_algorithm varchar(255);
alter table lib add column digest_verification_url varchar(255);
alter table lib  rename column wellknown_sha1 to wellknown_digest;

-- porting of data
update lib set digest_algorithm='SHA1';
--make digest algorithm not null 
alter table lib alter column digest_algorithm SET NOT NULL;








--remove token table and add the new ones
drop table token;
create table tenant (id int8 not null, created_at timestamp, last_modified timestamp, tenant_name varchar(1024) not null, is_default boolean not null, tenant_token varchar(64) not null, primary key (id));
create table space (id int8 not null, bug_filter int4, created_at timestamp, export_configuration varchar(255), is_public boolean, is_default boolean not null, last_modified timestamp, space_description varchar(255) not null, space_name varchar(1024) not null, space_token varchar(64) not null, tenant int8 not null, primary key (id));
create table space_owners (space_id int8 not null, space_owners varchar(255));


--new constraints
alter table tenant add constraint UK56lfg99jsu221fba9yd16go27 unique (tenant_token);
alter table space add constraint UKbrnfhl8nnvmgpil1lpdpd2aa9 unique (space_token);
alter table app add constraint FK7n6drfrrq5aev5sju47ymgip4 foreign key (space) references space;
alter table space add constraint FKod8n5b8w0nxjs1u3g4n2owmxx foreign key (tenant) references tenant;
alter table space_owners add constraint FKdg17umi0d65597dr1028dv9e foreign key (space_id) references space;

-- recreate views
alter view v_affected_library_sha1 rename to v_affected_library_digest;

create view v_app_vulndep_cc as 
(select distinct a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.filename,d.scope,d.transitive,l.digest,cc.bug,lid.mvn_group,lid.artifact,lid.version,al.affected
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.digest
join lib_constructs lc on l.id=lc.library_id
join bug_construct_change cc on lc.constructs_id=cc.construct_id
join construct_id c on cc.construct_id=c.id
left outer join library_id lid on l.library_id_id=lid.id
left outer join v_affected_library_gav al on al.library_id=lid.id and al.bug_id=cc.bug
where not lid.id is null and  not c.type='PACK')
UNION
(select distinct a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.filename,d.scope,d.transitive,l.digest,cc.bug,lid.mvn_group,lid.artifact,lid.version,aldigest.affected
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.digest
join lib_constructs lc on l.id=lc.library_id
join bug_construct_change cc on lc.constructs_id=cc.construct_id
join construct_id c on cc.construct_id=c.id
left outer join library_id lid on l.library_id_id=lid.id
left outer join v_affected_library_digest aldigest on aldigest.lib=l.digest and aldigest.bug_id=cc.bug
where lid.id is null and  not c.type='PACK');

--Vulnerable dependencies for the current applications (with priority applied) FOR configuration bugs
create view v_app_vulndep_config as  
select distinct a.mvn_group as app_group,a.artifact as app_artifact,a.version as app_version,d.filename,d.scope,d.transitive,
l.digest,al.bug_id,lid.mvn_group,lid.artifact,lid.version,al.affected
from app a join app_dependency d on a.id=d.app
join lib l on d.lib=l.digest
join library_id lid on l.library_id_id=lid.id
join v_affected_library_gav al on al.library_id=lid.id
left join bug_construct_change cc on al.bug_id=cc.bug
where cc.bug is null;

--ALL Vulnerable dependencies for the current applications (with priority applied) 
create view v_app_vulndep as
select distinct * from v_app_vulndep_cc
UNION
select distinct * from v_app_vulndep_config;


-- initialize space and tenants
insert into tenant (created_at, last_modified, is_default, tenant_name, tenant_token,id) select  now(),now(),'TRUE','default','603EFBA1EA9B98ADB4B548682597E6D0',nextval ('hibernate_sequence');
insert into space (bug_filter, created_at, last_modified, export_configuration, is_public, is_default, space_description, space_name, space_token, id, tenant) 
	select '1',now(),now(),'DETAILED','TRUE', 'TRUE', 'Default space','public','A5344E8A6D26617C92A0CAD02F10C89C',nextval ('hibernate_sequence'),id from tenant;
		
insert into space (bug_filter, created_at, last_modified, export_configuration, is_public, is_default, space_description, space_name, space_token, id, tenant) 
	select '1',now(),now(),'OFF','TRUE', 'FALSE', 'Test space','test','BA0A5620222605DE6728594CDA4F4391',nextval ('hibernate_sequence'),id from tenant;
	
-- update existing applications
update app set space = (select id from space where space_token='A5344E8A6D26617C92A0CAD02F10C89C' and tenant =(select id from tenant where tenant_token='603EFBA1EA9B98ADB4B548682597E6D0'))
	where not mvn_group='com.acme';
	
update app set space = (select id from space where space_token='BA0A5620222605DE6728594CDA4F4391' and tenant =(select id from tenant where tenant_token='603EFBA1EA9B98ADB4B548682597E6D0'))
	where mvn_group='com.acme';


-- make space column not nullable now that all rows are populated 
alter table app alter column space SET NOT NULL;