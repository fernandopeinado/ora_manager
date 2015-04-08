select t.owner, t.table_name, null as index_name, 'TABLE' as type, sum(bytes) / (1024 * 1024) as sizeMb
from DBA_SEGMENTS ex inner join ALL_TABLES t on ex.segment_name = t.table_name and ex.owner = t.owner
where ex.SEGMENT_TYPE = 'TABLE' and t.owner = :owner and t.table_name = :tableName
group by t.owner, t.table_name
union
select i.owner, i.table_name, i.index_name, 'INDEX' as type, sum(bytes) / (1024 * 1024) as sizeMb
from DBA_SEGMENTS ex inner join ALL_INDEXES i on ex.segment_name = i.index_name and ex.owner = i.owner
where ex.SEGMENT_TYPE = 'INDEX' and i.owner = :owner and i.table_name = :tableName
group by i.owner, i.table_name, i.index_name
union
select l.owner, l.table_name, null as index_name, 'LOBSEGMENT' as type, sum(bytes) / (1024 * 1024) as sizeMb
from DBA_SEGMENTS ex inner join ALL_LOBS l on ex.segment_name = l.segment_name and ex.owner = l.owner
where ex.SEGMENT_TYPE = 'LOBSEGMENT' and l.owner = :owner and l.table_name = :tableName
group by l.owner, l.table_name
union
select l.owner, l.table_name, null as index_name, 'LOBINDEX' as type, sum(bytes) / (1024 * 1024) as sizeMb
from DBA_SEGMENTS ex inner join ALL_LOBS l on ex.segment_name = l.index_name and ex.owner = l.owner
where ex.SEGMENT_TYPE = 'LOBINDEX' and l.owner = :owner and l.table_name = :tableName
group by l.owner, l.table_name
