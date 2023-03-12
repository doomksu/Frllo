--total; total_result; not_loaded; good; error
select total, total_result, not_loaded, good, error
from (select
	sum(good_loaded) as good,
	sum(error_loaded) as error,
	sum(not_loaded) as not_loaded,
	sum(good_loaded + error_loaded + not_loaded) as total_result,
	count(*) as total
	from (
		select  
		case when max(isloaded) = 0 then 1 else 0 end as not_loaded,
		case when max(isloaded) > 0 then 1 else 0 end as good_loaded,
		case when max(isloaded) < 0 then 1 else 0 end as error_loaded 
		FROM NATA."FRLLO_PERSONS2"
		group by npers
	)
)