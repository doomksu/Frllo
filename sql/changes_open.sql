select 
man.npers, man.id, man.re,
coalesce(man.FA_ORIGIN, man.FA) as fa,
coalesce(man.IM_ORIGIN, man.IM) as im,
coalesce(man.OT_ORIGIN, man.OT) as ot,
cast(man.rdat as varchar(255)) as RDAT,
man.pol, man.reg_ind, man.reg_re, man.reg_ra, man.reg_gorod, man.reg_punkt, man.reg_ul, man.reg_dom, man.reg_kor, man.reg_kva, man.fakt_re, man.fakt_ra, man.fakt_gorod, man.fakt_punkt,
man.fakt_ul, man.fakt_dom, man.fakt_kor, man.fakt_kva, man.kdok, man.pass, man.pasn,
cast(man.pas_dat as varchar(255)) as PAS_DAT,
man.pas_kem, man.grajdan, gsp.vreg, rasgsp.l1, rasgsp.l2, rasgsp.nsu1,
man.changedate,
cast(gsp.sroks as varchar(255)) as SROKS,
cast(case when rasgsp.srokpo>'2050-01-01' then null else rasgsp.srokpo end as varchar(255)) as SROKPO,
man.pw,
man.dpw,
man.dsm

FROM(
	(select id, max(changedate) as changedate from loads.loadman loadman 
		where loadman.loadtime >= '<DATE_START> 00:00:00' and loadman.loadtime <= '<DATE_START> 24:00:00'
		group by id
	)load 
		
	inner join 
	(select 
		man.npers, man.id, man.re, man.fa, man.im, man.ot, man.FA_ORIGIN, man.IM_ORIGIN, man.OT_ORIGIN, man.rdat, man.pol, man.reg_ind, man.reg_re, man.reg_ra, 
		man.reg_gorod, man.reg_punkt, man.reg_ul, man.reg_dom, man.reg_kor, man.reg_kva, man.fakt_re, man.fakt_ra, man.fakt_gorod, man.fakt_punkt, man.fakt_ul, 
		man.fakt_dom, man.fakt_kor, man.fakt_kva, man.kdok, man.pass, man.pasn, man.pas_dat, 
		man.pas_kem, man.grajdan, man.changedate, man.pw, man.dpw, man.dsm from changes.man man 
			where  man.changedate = (select max(mann.changedate) from changes.man mann where mann.id = man.id)
			and (man.pw = 0 or man.pw is null or (man.dpw > (CURRENT DATE) and man.pw not in (2, 50)))
	)man
	on man.id = load.id
	and man.changedate = load.changedate
	
	inner join 
	(select 
		*
		from changes.rasgsp rasgsp 
		where rasgsp.sroks = (select max(sroks) from changes.rasgsp rasgsp2 
								where rasgsp2.id = rasgsp.id 
								and rasgsp2.changedate = rasgsp.changedate
								and rasgsp2.sroks < (CURRENT DATE)) 
			and (rasgsp.srokpo is null or rasgsp.srokpo >= (CURRENT DATE)) 
			and rasgsp.dat = (select max(dat) from changes.rasgsp rasgsp2 
								where rasgsp2.id = rasgsp.id
								and rasgsp2.changedate = rasgsp.changedate
								and rasgsp2.sroks = rasgsp.sroks) 
			and (lower(rasgsp.oper) not in ('пре','сня') or rasgsp.oper is null)
	)rasgsp
	on rasgsp.id = man.id
	and rasgsp.changedate = man.changedate
	
	inner join 
	(select 
		* 
		from changes.gsp gsp 
		where (gsp.pgp is null or gsp.pgp = 1)
	)gsp
	on gsp.id = man.id
	and gsp.changedate = man.changedate
	and gsp.dat = rasgsp.dat
)
with ur