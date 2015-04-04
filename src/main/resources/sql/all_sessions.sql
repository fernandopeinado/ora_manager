select
sid,
serial#,
username,
program,
sql_id,
sql_child_number,
decode(wait_time, 0, event, 'CPU + CPU Wait') as event,
decode(wait_time, 0, wait_class, 'CPU + CPU Wait') as wait_class
from
v$session