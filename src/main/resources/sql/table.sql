select 
t.owner
, t.table_name 
, t.tablespace_name
, t.logging
, t.num_rows
, t.avg_row_len
, t.sample_size
, t.last_analyzed
from 
ALL_TABLES t
where 
t.owner = :owner
and t.table_name = :tableName