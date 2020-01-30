alter table space add column is_read_only boolean;

update space set is_read_only=false;

