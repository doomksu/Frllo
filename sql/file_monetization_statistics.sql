SELECT
flf.id as FILEID,
mfile_id,
filename, 
total_result,
total,
monetization_writed,
good,
error,
not_loaded

from(
	(select
		mfile_id,
		sum(monetization) as monetization,
		sum(lo) as lo,
		sum(monetization_writed) as monetization_writed,
		sum(good_loaded) as good,
		sum(error_loaded) as error,
		sum(not_loaded) as not_loaded,
		sum(good_loaded + error_loaded + not_loaded) as total_result,
		count(*) as total	
		from (
			select 
			mfile_id,
			case when nsu = 0 then 1 else 0 end as monetization,
			case when nsu = 1 then 1 else 0 end as lo,
			case when mfile_id <> 0 then 1 else 0 end as monetization_writed,
			case when ismloaded > 0 then 1 else 0 end as good_loaded,
			case when ismloaded = 0 then 1 else 0 end as not_loaded,
			case when ismloaded < 0 then 1 else 0 end as error_loaded
			from (
				select npers,
				nsu,
				mfile_id,
				max(ismloaded) as ismloaded
				FROM NATA."FRLLO_PERSONS2"
				where mfile_id <> -1
				group by npers, nsu, mfile_id
			)
		)group by mfile_id
	)stat
	
	left OUTER JOIN 
		(select filename, id from NATA.FRLLO_MONETIZATION_FILES) flf
		on char(stat.mfile_id) = char(flf.id)
)
order by mfile_id
