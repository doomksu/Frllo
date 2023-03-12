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
cast(
	coalesce((case when man.dsm is not null and man.pw = 1 --death
		then coalesce(man.vreg, man.vseg)
	else
		(case when man.pw = 2 --переезд в другой регион
			then man.vseg
			else 
				(case when man.pw = 50	-- переезд из РФ
					then man.vreg
					else 
					coalesce(man.vreg, man.vseg)
				end)
		end)
	end), gsp.vreg, gsp.vseg)
as varchar(255)) as SROKS,

cast(
coalesce(
	(case when man.dsm is not null and man.pw = 1 
	then coalesce(man.ireg, man.iseg) --смерть 
	else 
		(case when man.pw = 2 --переезд в другой регион
			then man.iseg
			else 
				(case when man.pw = 50	-- переезд из РФ
					then  man.ireg 
					else 
						coalesce(man.ireg, gsp.dpw, gsp.dat, gsp.sroks)
				end)
		end)
	end), rasgsp.srokpo, gsp.iseg, gsp.ireg) 
as varchar(255)) as SROKPO,
man.pw,
man.dpw,
man.iseg as iseg,

gsp.dat as gsp_dat,
gsp.sroks as gsp_sroks,
gsp.srokpo as gsp_srokpo,
gsp.dpw as gsp_dpw,

rasgsp.dat as rasgsp_dat,
rasgsp.datv as rasgsp_datv,
rasgsp.sroks as rasgsp_sroks,
rasgsp.srokpo as rasgsp_srokpo,

rasgsp.sroks as rasgspsroks,
rasgsp.oper,
gsp.dat,
gsp.datnp,
gsp.vreg as gspvreg,
gsp.vseg as gspvseg,
gsp.ireg as gspireg,
gsp.iseg as gspiseg,
man.dsm
FROM(
	(select id, max(changedate) as changedate from loads.loadman loadman 
		where loadman.loadtime >= '<DATE_START> 00:00:00' and loadman.loadtime <= '<DATE_START> 24:00:00'
		group by id
	)load  
		
	inner join 
	(select            
		man.npers, man.id, man.re, man.fa, man.im, man.ot, man.FA_ORIGIN, man.IM_ORIGIN, man.OT_ORIGIN, cast(man.rdat as varchar(255)) as rdat,
		man.pol, man.reg_ind, man.reg_re, man.reg_ra, man.reg_gorod, man.reg_punkt, man.reg_ul, man.reg_dom, man.reg_kor, man.reg_kva, man.fakt_re,
		man.fakt_ra, man.fakt_gorod, man.fakt_punkt, man.fakt_ul, man.fakt_dom, man.fakt_kor, man.fakt_kva, man.kdok, man.pass, man.pasn,
		man.pas_dat, man.pas_kem, man.grajdan, man.changedate, man.pw, man.dpw, man.iseg, man.ireg,man.vreg, man.vseg, man.dsm  from changes.man man 
			where man.changedate = (select max(mann.changedate) from changes.man mann where mann.id = man.id)
			and (man.dpw is null or (year(CURRENT DATE) - year(man.dpw)) <=1)
	)man
	on man.id = load.id
	and man.changedate = load.changedate
	
	inner join 
	(select 
		*                                            
		from changes.rasgsp rasgsp                                                  
		where rasgsp.sroks = (select max(sroks) from changes.rasgsp rasgsp2         
								where rasgsp2.id = rasgsp.id and                  
								rasgsp2.changedate = rasgsp.changedate and  
								rasgsp2.sroks < (CURRENT DATE))                                                      
			and (rasgsp.srokpo is null or rasgsp.srokpo >= (CURRENT DATE))        
			and rasgsp.dat = (select max(dat) from changes.rasgsp rasgsp2         
								where rasgsp2.id = rasgsp.id and                    
								rasgsp2.changedate = rasgsp.changedate and    
								rasgsp2.sroks = rasgsp.sroks)                                                    
			and lower(rasgsp.oper) in ('пре','сня')
	)rasgsp
	on rasgsp.id = man.id
	and rasgsp.changedate = man.changedate
	
	inner join 
	(select 
		*
		from changes.gsp gsp                            
		where (gsp.pgp is null or gsp.pgp = 1)
		and (gsp.dpw is null or (year(CURRENT DATE) - year(gsp.dpw)<=1))
	)gsp
	on gsp.id = man.id
	and gsp.changedate = man.changedate
	and gsp.dat = rasgsp.dat
	--and gsp.pw=1
)
where (rasgsp.srokpo >= '2021-01-01' or rasgsp.srokpo is null)
and (gsp.srokpo >= '2021-01-01' or gsp.srokpo is null)
and (man.dpw >= '2021-01-01' or man.dpw is null)
with ur