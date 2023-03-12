select * from 
(select id, max(changedate) as changedate from loads.loadman loadman 
group by id
) loads 

right join nata.frllo_persons2 persons
on loads.id = persons.id_nvp

with ur