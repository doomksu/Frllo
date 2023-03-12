delete from FRLLO_PERSONS2 where FILE_ID in (select id from FRLLO_LOAD_FILES where CREATION_DATE>=? and CREATION_DATE<?);
delete from FRLLO_LOAD_FILES where CREATION_DATE>=? and CREATION_DATE<?;