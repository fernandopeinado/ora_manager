select
owner,
table_name,
tablespace_name,
num_rows,
last_analyzed
from {{ dba_tables }}
where owner = :owner
order by table_name
