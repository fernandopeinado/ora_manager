select
c.owner
, c.table_name
, c.column_name
, c.data_type
, c.data_length
, c.data_precision
, c.data_scale
, c.nullable
, c.column_id
, c.last_analyzed
from
{{ all_tab_columns }} c
where
c.owner = :owner
and c.table_name = :tableName
