select

index_name,

(
select listagg(column_name, ',') within group (order by column_position)
from {{ dba_ind_columns }} c where c.index_owner = i.owner and c.index_name = i.index_name
) columns,

uniqueness,
tablespace_name,
logging,
blevel,
leaf_blocks,
distinct_keys,
num_rows,
sample_size,
last_analyzed,

(
select sum(bytes)
from {{ dba_segments }} s
where s.owner = i.owner and s.segment_name = i.index_name
) bytes

from {{ dba_indexes }} i
where owner = :owner and table_name = :tableName
order by index_name
