 
	select 
		pe.id,
		pe.sroks,
		pe.srokpo,
		pe.pw,
		pe.dpw,
		pe.pgp,
		pe.np,
		pe.changedate
		
		from changes.pe pe
		where pe.id = (select DISTINCT man.id from changes.man man where man.npers = '052-061-885 32')
		
		order by pe.changedate