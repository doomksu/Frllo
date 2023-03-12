 
 
 select * from 
 
	(select  man.id as id,
	man.changedate as changedate
	
 	from changes.man man where man.npers = '014-692-013 29'
	and man.changedate = (select max(mann.changedate) from changes.man mann where mann.id = man.id)
	)man
	
	
	left join changes.d65 d65
	on d65.id = man.id
	and d65.changedate = man.changedate
	