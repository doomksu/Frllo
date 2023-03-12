select 
	pers.ID, 
	pers.NPERS, 
	pers.ID_NVP, 
	pers.GUID, 
	pers.FILE_ID, 
	pers.ISLOADED, 
	pers.FA, 
	pers.IM, 
	pers.OT, 
	pers.RDAT, 
	pers.SEX, 
	pers.CITIZENSHIP, 
	pers.DOCTYPE, 
	pers.SERIAL, 
	pers.DOCNUMBER, 
	pers.ISSUE, 
	pers.REGION, 
	pers.BENEFIT, 
	pers.RECEIVE_DATE, 
	pers.CANCEL_DATE, 
	pers.NSU 
	from (select * from 
	NATA."FRLLO_PERSONS2" pers 
		where (pers.mfile_id is null or pers.mfile_id=0) 
		and pers.nsu = 0 
	)pers
	
	
		
	
