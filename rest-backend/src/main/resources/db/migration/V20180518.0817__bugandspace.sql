create table space_properties (space_id int8 not null, properties_id int8 not null);


alter table bug add column bug_id_alt varchar(32);

alter table bug add column cvss_score float4;
alter table bug add column cvss_vector varchar(100);
alter table bug add column cvss_version varchar(5);

alter table bug add column maturity varchar(5) ;
update bug set maturity='READY';
alter table bug alter column maturity SET NOT NULL;


alter table bug add column origin varchar(6) ;
update bug set origin='PUBLIC';
alter table bug alter column origin SET NOT NULL;


ALTER TABLE bug DROP COLUMN source;