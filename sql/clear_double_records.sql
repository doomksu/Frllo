delete from  NATA."FRLLO_PERSONS2" pers 
where pers.isloaded = 0
and exists (
		select * from  NATA."FRLLO_PERSONS2" perss 
		where perss.npers = pers.npers 
		and perss.file_id = pers.file_id 
		and perss.isloaded <> pers.isloaded
)