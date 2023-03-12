select 
	fre.npers,
	fre.guid,
	fre.ERROR_CASE,
	errcode.name,
	persons.region

from (
	

	(select 
	fre.NPERS,
	fre.GUID,
	fre.ERROR_CASE
	from <DB_SCHEMA>.FRLLO_RESULTS_ERROR fre) fre
	
	inner join 
	(select 
	npers,
	ID_NVP,
	region,
	guid
	from <DB_SCHEMA>.FRLLO_PERSONS2 persons) persons
	on persons.npers = fre.npers
	and persons.guid = fre.guid 
	
	inner join 
	(select 
	id,
	name
	from <DB_SCHEMA>.RESULT_ERROR_CODE errcode) errcode
	on fre.ERROR_CASE = errcode.id
)