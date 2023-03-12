delete from <DB_SCHEMA>.FRLLO_FBDP_ERRORS ffe 
where exists(
	select npers from <DB_SCHEMA>.FRLLO_PERSONS2 p2 
	where p2.npers = ffe.npers 
	and p2.ISLOADED > 0 
) or (npers is null or LENGTH(npers) = 0)