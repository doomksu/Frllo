SELECT 
flf.id as FILEID,
stat.statID, 
filename,
total,
total_result,
good,
error,
twins,
(good+twins) as all_positive,
not_loaded

from(
	(select 
		statID,
		sum(good_loaded) as good,
		sum(error_loaded) as error,
		sum(double_loaded) as twins,
		sum(total_loaded) as total_result,
		sum(not_loaded) as not_loaded,
		count(*) as total
		from
			(
			select file_id as statID, 
			case when isloaded = 1 then 1 else 0 end as good_loaded,
			case when isloaded < 0 then 1 else 0 end as error_loaded,
			case when isloaded = 2 then 1 else 0 end as double_loaded,
			case when isloaded <> 0 then 1 else 0 end as total_loaded,
			case when isloaded = 0 then 1 else 0 end as not_loaded 
			FROM NATA."FRLLO_PERSONS2"
			)
		group by statID
	) stat
		
	left join 
		(select filename, id 
			from NATA.frllo_load_files
		) flf
		on char(stat.statID) = char(flf.id)
	)
order by stat.statID