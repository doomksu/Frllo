select distinct 
	nctype,
	sum(countByType) as count_By_Type,
	sum(countByIndex) as count_By_Index,
	sum(countByDeposite) as count_By_Deposite,
	avg(shiftByIndex) as avg_shift_By_Index,
	avg(shiftByDeposite) as avg_shift_By_Deposite
	from(
	select 
		nctype,
		countByType,
		countByIndex,
		countByDeposite,
		shiftByIndex,
		shiftByDeposite
	
	from(
		select 
			afterRecalc.nctype as nctype, 
			1 as countByType,
			beforeRecalc.ncs as sum_before,
			afterRecalc.ncs as sum_after,			
			case 
				when (afterRecalc.nctype = 1) then round(((beforeRecalc.ncs * 1.063663641248) - beforeRecalc.ncs),2)
				when (afterRecalc.nctype = 2) then round(((beforeRecalc.ncs * 1.063254240013) - beforeRecalc.ncs),2)
			end as shiftByIndex,
			
			case when (
				case 
					when (afterRecalc.nctype = 1) then round(((beforeRecalc.ncs * 1.063663641248) - beforeRecalc.ncs),2)
					when (afterRecalc.nctype = 2) then round(((beforeRecalc.ncs * 1.063254240013) - beforeRecalc.ncs),2)
				end)> 0 then 1 else 0 
			end as countByIndex, 
			
			case 
				when (afterRecalc.nctype = 1) then (case when round((afterRecalc.ncs - (beforeRecalc.ncs * 1.063663641248)),2) > 0 
				then round((afterRecalc.ncs - (beforeRecalc.ncs * 1.063663641248)),2) else 0 end)
						
				when (afterRecalc.nctype = 2) then (case when round((afterRecalc.ncs - (beforeRecalc.ncs * 1.063254240013)),2) > 0
				then round((afterRecalc.ncs - (beforeRecalc.ncs * 1.063254240013)),2) else 0 end)
			end as shiftByDeposite,
			
			case when (
				case 
					when (afterRecalc.nctype = 1) then round((afterRecalc.ncs - (beforeRecalc.ncs * 1.063663641248)),2)
					when (afterRecalc.nctype = 2) then round((afterRecalc.ncs - (beforeRecalc.ncs * 1.063254240013)),2)
				end) > 0 then 1 else 0
			end as countByDeposite
		
		from(
		
			(select 
				rasnc.id,
				rasnc.dat,
				rasnc.changedate,
				rasnc.sroks,
				rasnc.nctype,
				rasnc.ncs,
				rasnc.nresh,
				rasnc.nct,
				rasnc.dresh
				
				from changes.rasnc rasnc 
				where rasnc.dat = (select max(ras.dat) from changes.rasnc ras 
										where ras.id = rasnc.id
										and ras.sroks = '2021-08-01'
										and ras.nctype = rasnc.nctype
										and ras.changedate = rasnc.changedate)
				and rasnc.sroks = '2021-08-01'
				and rasnc.nctype  in (1,2)
				and rasnc.changedate = (select max(ra.changedate) from changes.rasnc ra 
											where ra.id = rasnc.id 
											and ra.dat = rasnc.dat
											and ra.sroks = rasnc.sroks
											and ra.nctype = rasnc.nctype)
				and year(rasnc.ncdnaz) < 2021
			) as afterRecalc -- current period
			
			-- определяем текущий период после индексации
			
			inner join
			(select 
				rasnc.id,
				rasnc.dat,
				rasnc.changedate,
				rasnc.sroks,
				rasnc.nctype,
				rasnc.ncs,
				rasnc.nresh,
				rasnc.nct,
				rasnc.dresh
				
				from changes.rasnc rasnc 
				where rasnc.dat = (select max(ras.dat) from changes.rasnc ras 
					where ras.id = rasnc.id
					and ras.sroks = rasnc.sroks
					and ras.id = rasnc.id
					and ras.nctype=rasnc.nctype)
				and rasnc.sroks = (select max(ras.sroks) from changes.rasnc ras 
									where ras.sroks < '2021-08-01'
									and ras.id = rasnc.id
									and ras.dat=rasnc.dat
									and ras.changedate = rasnc.changedate)
				and rasnc.nctype  in (1,2)
				and rasnc.changedate = (select max(ra.changedate) from changes.rasnc ra 
											where ra.id = rasnc.id 
											and ra.dat = rasnc.dat
											and ra.sroks = rasnc.sroks
											and ra.nctype = rasnc.nctype)
				and year(rasnc.ncdnaz) < 2021
			)beforeRecalc -- before current -second RS
			-- определяем последний период перед индексацией
			on afterRecalc.id = beforeRecalc.id
			and afterRecalc.nctype = beforeRecalc.nctype
			and afterRecalc.changedate = beforeRecalc.changedate
			and afterRecalc.dat = beforeRecalc.dat
		)
	)	where (shiftByIndex>0 or shiftByDeposite>0)
	
)group by nctype