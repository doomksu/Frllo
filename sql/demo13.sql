select  
man.npers, man.re, man.ra, man.fa, man.im, man.ot, man.rdat, 
 from 
(select  demo.id, demo.KAT_DEMO, demo.changedate
	from CHANGES.DEMO demo
	where demo.KAT_DEMO = 13
	and demo.dat = (select max (demoo.dat) from CHANGES.DEMO demoo where demoo.id = demo.id)
	and demo.sroks = (select max (demoo.sroks) from CHANGES.DEMO demoo where demoo.id = demo.id)
	and demo.changedate = (select max(demoo.changedate) from changes.demo demoo where demoo.id = demo.id)
	and (demo.pw is null or demo.pw = 0) 
	and (demo.dpw is null or demo.dpw > CURRENT DATE)
)demo

inner join 

(select	man.npers, man.id, man.re, man.ra, man.fa, man.im, man.ot, man.rdat, man.changedate
		from changes.man man 
			where man.changedate = (select max(mann.changedate) from changes.man mann where mann.id = man.id)
			and (man.dpw is null or man.dpw > (CURRENT DATE))
)man
on man.id = demo.id
and man.changedate = demo.changedate