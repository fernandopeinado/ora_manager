select
column_name,
data_type,
data_length,
data_precision,
data_scale,
nullable,
last_analyzed
from {{ dba_tab_columns }}
where owner = :owner and table_name = :tableName
order by column_id
