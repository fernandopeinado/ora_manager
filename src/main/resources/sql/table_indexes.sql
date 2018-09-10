select
i.owner
, i.index_name
, i.table_name
, i.uniqueness
, i.tablespace_name
, i.logging
, i.blevel
, i.leaf_blocks
, i.distinct_keys
, i.num_rows
, i.sample_size
, i.last_analyzed
, c.column_name
from
{{ all_indexes }} i
inner join {{ all_ind_columns }} c on i.owner = c.index_owner and i.index_name = c.index_name
where
i.owner = :owner
and i.table_name = :tableName
order by c.column_position
