select 
df.tablespace_name as tablespace,
totalusedspace as usedMb,
(df.totalspace - tu.totalusedspace) as freeMb,
df.totalspace as totalMb
from
(select 
    tablespace_name,
    round(sum(bytes) / 1048576) totalspace
    from dba_data_files 
    group by tablespace_name) df,
(select 
    round(sum(bytes) / (1024 * 1024)) totalusedspace, 
    tablespace_name
    from dba_segments 
    group by tablespace_name) tu
where 
df.tablespace_name (+)= tu.tablespace_name