select * from 
(select * from nata.FRLLO_PERSONS2 p2 where p2.npers = '002-640-651 99')p2
left join nata.FRLLO_LOAD_FILES loadf on loadf.id = p2.file_id
left join nata.FRLLO_MONETIZATION_FILES mloadf on mloadf.id  = p2.mfile_id
with ur

