create table lib_bundled_library_ids (library_id int8 not null, bundled_library_ids_id int8 not null);
alter table lib_bundled_library_ids add constraint FKpfkpmqs18pra09mdrt5ln4qui foreign key (bundled_library_ids_id) references library_id;
alter table lib_bundled_library_ids add constraint FK3nbep3t70cu9sc3ggoscnd96 foreign key (library_id) references lib;
