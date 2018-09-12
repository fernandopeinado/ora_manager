select
owner,
table_name,
tablespace_name,
num_rows,
last_analyzed,

( /* TABLE, TABLE PARTITION */
select sum(bytes)
from {{ dba_segments }}
where owner = t.owner and segment_name = t.table_name
) table_bytes,

( /* LOBSEGMENT */
select sum(bytes)
from {{ dba_lobs }} l
join {{ dba_segments }} s on s.owner = l.owner
  and s.segment_name = l.segment_name
where l.owner = t.owner and l.table_name = t.table_name
) lob_bytes,

( /* INDEX, INDEX PARTITION, LOBINDEX */
select sum(bytes)
from {{ dba_indexes }} i
join {{ dba_segments }} s on s.owner = i.owner
  and s.segment_name = i.index_name
where i.owner = t.owner and i.table_name = t.table_name
) index_bytes

from {{ dba_tables }} t
where owner = :owner and table_name = :tableName
