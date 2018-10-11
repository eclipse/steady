alter table bug add column description_alt text;

update bug set description_alt=description;

update bug set description=null;

