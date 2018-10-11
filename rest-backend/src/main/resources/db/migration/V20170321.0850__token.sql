create table token (id int8 not null, created_at timestamp, scope varchar(255) not null, token varchar(64) not null, primary key (id)) ;
alter table token drop constraint if exists UK_pddrhgwxnms2aceeku9s2ewy5 ;
alter table token add constraint UK_pddrhgwxnms2aceeku9s2ewy5 unique (token) ;