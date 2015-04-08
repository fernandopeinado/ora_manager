select 
df.tablespace_name as tablespace,
(df.totalspace - tu.totalfreespace) as usedMb,
totalfreespace as freeMb,
df.totalspace as totalMb
from
(select 
    tablespace_name,
    round(sum(bytes) / (1024 * 1024)) totalspace
    from dba_data_files 
    group by tablespace_name) df
inner join    
(select 
    round(sum(bytes) / (1024 * 1024)) totalfreespace, 
    tablespace_name
    from dba_free_space 
    group by tablespace_name) tu
on df.tablespace_name = tu.tablespace_name