select * from 
(select 
man.id,
man.re,
man.ra,
man.fa, 
man.im, 
man.ot, 
char(man.rdat) as rdat,
man.npers,
man.changedate

from changes.man man 
<MAN_QUERY>
and man.changedate = (select max(mann.changedate) from changes.man mann where mann.id = man.id)
)man
	
with ur