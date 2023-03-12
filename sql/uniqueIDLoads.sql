select DISTINCT id from loads.loadman loadman 
		where loadman.loadtime >= '2021-01-01 00:00:00' and loadman.loadtime <= '2021-09-21 24:00:00'
		group by id
with ur