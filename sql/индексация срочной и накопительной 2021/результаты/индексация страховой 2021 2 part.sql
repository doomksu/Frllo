select 2, count(*),avg(delta) from (	
	select afterIndex.id, case when (afterIndex.strah - beforeIndex.strah) > 0 then 1 else 0 end as countStrah,
	afterIndex.strah as newStrah, (afterIndex.vznos_sc - beforeIndex.vznos_sc) as delta
from ((select raspen.id, raspen.dat, raspen.changedate, raspen.sroks, raspen.strah, raspen.vznos_sc
		from changes.raspen raspen
		where raspen.sroks = '2021-08-01'
		and raspen.dat = (select max(raa.dat)  from changes.raspen raa where raa.id = raspen.id  and raa.oper='ВЗН' and  raa.sroks = raspen.sroks and RANGE_KEY = raspen.RANGE_KEY)
		and raspen.changedate = (select max(ra.changedate) from changes.raspen ra  where ra.id = raspen.id and ra.dat = raspen.dat and ra.sroks = raspen.sroks and RANGE_KEY = raspen.RANGE_KEY)
		and raspen.RANGE_KEY >='010' and raspen.RANGE_KEY <='019') as afterIndex		
	inner join 
	(select 
		raspen.id, raspen.dat, raspen.changedate, raspen.sroks, raspen.strah, raspen.vznos_sc
		from changes.raspen raspen
		where raspen.sroks = (select max(sroks) from CHANGES.RASPEN where id = raspen.id 
				and sroks < '2021-08-01' and changedate = raspen.changedate and RANGE_KEY = raspen.RANGE_KEY)
		and raspen.dat = (select max(raa.dat) from changes.raspen raa where raa.id = raspen.id and raa.sroks = raspen.sroks
				and raa.changedate = raspen.changedate and RANGE_KEY = raspen.RANGE_KEY)
		and raspen.changedate = (select max(ra.changedate) from changes.raspen ra where ra.id = raspen.id and ra.dat = raspen.dat and ra.sroks = raspen.sroks and RANGE_KEY = raspen.RANGE_KEY)
		and raspen.RANGE_KEY >='010' and raspen.RANGE_KEY <='019') as beforeIndex
	on afterIndex.id = beforeIndex.id)
)where delta>0 with ur

