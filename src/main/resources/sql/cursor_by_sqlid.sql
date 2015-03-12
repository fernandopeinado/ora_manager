select

sql_text,
command_name

from v$sqlarea sa
left outer join v$sqlcommand sc on sc.command_type = sa.command_type

where sql_id = :sqlId
