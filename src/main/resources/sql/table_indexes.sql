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
from 
ALL_INDEXES i
where 
i.owner = :owner
and i.table_name = :tableName