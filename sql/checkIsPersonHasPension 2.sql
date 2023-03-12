select * from 

(select 
man.re,
man.ra,
man.fa, 
man.im, 
man.ot, 
char(man.rdat),
man.npers

from changes.man man 
<MAN_QUERY>)man

left join
	(select distinct
		pe.id as pep_id,
		pe.changedate
		from changes.pe pe
		where pe.dat = (select max(pee.dat) from changes.pe pee 
			where pee.id = pe.id
			and (pee.pgp = 1)
			and (pee.SROKPO > (CURRENT_DATE) or pee.srokpo is null)
			and (pee.np!='ПРЕ' and pee.np!='ПРИ'))
		)pep
		
	on pep.pep_id = man.id
	and pep.changedate = man.changedate

	
left join 
	(select distinct
		ra.id as raspen_id,
		ra.changedate,
		ra.cpen,
		ra.sroks,
		ra.srokpo,
		ra.oper,
		from changes.raspen ra
		where srokpo > (CURRENT DATE) or srokpo is null
		and ra.dat = (select max(pe.dat) from changes.pe pe where pe.id=ra.id)
		and ra.sroks = (select max(raa.sroks) from changes.raspen raa where raa.id=ra.id)
		and ra.cpen > 0
		)raspen 
	on raspen.raspen_id = man.id
	and raspen.changedate = man.changedate