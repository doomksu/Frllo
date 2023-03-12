export to "c:\export\frllo" of del modified by chardel"" coldel; messages "c:\export\m_mes" 
select 
man.npers,
man.id,
man.fa,
man.im,
man.ot,
cast(man.rdat as varchar(255)),
man.pol,
man.reg_ind,
man.reg_re,
man.reg_ra,
man.reg_gorod,
man.reg_punkt,
man.reg_ul,
man.reg_dom,
man.reg_kor,
man.reg_kva,
man.fakt_re,
man.fakt_ra,
man.fakt_gorod,
man.fakt_punkt,
man.fakt_ul,
man.fakt_dom,
man.fakt_kor,
man.fakt_kva,
man.kdok,
man.pass,
man.pasn,
cast(man.pas_dat as varchar(255)),
man.pas_kem,
man.grajdan,
gsp.vreg,
rasgsp.l1,
rasgsp.l2,
rasgsp.nsu1,
cast(gsp.sroks as varchar(255)) as SROKS,
cast(case when rasgsp.srokpo>'2050-01-01' then null else rasgsp.srokpo end as varchar(255)) as SROKPO,
man.re,
man.ra

     from changes.gsp as gsp
    , changes.rasgsp as rasgsp
    ,changes.man man
	
  where gsp.id = rasgsp.id 
		and gsp.changedate = rasgsp.changedate 
		and gsp.dat = rasgsp.dat
		and man.id=gsp.id 
		and man.changedate=gsp.changedate
		and gsp.changedate = (select max(changedate) from changes.man man where man.id=gsp.id) --берем последний исторический слой
		
		and (gsp.pgp is null or gsp.pgp = 1) -- берем обращения с признаком готовности
		
		and rasgsp.sroks =(select max(sroks) from changes.rasgsp rasgsp2
                          where rasgsp.id = rasgsp2.id
                                 and rasgsp.changedate = rasgsp2.changedate
                                 and rasgsp2.sroks<sysdate
                         ) 
      and (rasgsp.srokpo is null or rasgsp.srokpo >= sysdate) 
	  
      and rasgsp.dat=(select max(dat) from changes.rasgsp rasgsp2 
                         where rasgsp2.id=rasgsp.id 
						 and rasgsp2.changedate = rasgsp.changedate 
						 and rasgsp2.sroks=rasgsp.sroks ) -- если на один период было 2 расчета берем последний
						 
      and (rasgsp.oper not in ('пре','сня') or rasgsp.oper is null) -- не берем прекращенные, приостановленные и снятые
      and (man.pw=0 or man.pw is null or (man.dpw>sysdate and man.pw not in (2,50)))
	  
      and (select count(*) from loads.loadman where loads.loadman.id=man.id
            and loadtime>='2020-11-01' --дата начала изменений
            and loadtime<'2020-11-02'  --дата окончания изменений
               )>0
  --   group by gsp.id
   --  having count(*)>1
  with ur