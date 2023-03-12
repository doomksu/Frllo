delete from <DB_SCHEMA>.FRLLO_RESULTS_ERROR fre 
where exists(
	select npers from <DB_SCHEMA>.FRLLO_PERSONS2 p2 
	where p2.npers = fre.npers 
	and p2.ISLOADED > 0 
) or npers is null