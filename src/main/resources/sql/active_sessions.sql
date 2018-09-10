select
sid,
serial#,
decode(type, 'BACKGROUND', substr(program, -5, 4), username) as username,
program,
sql_id,
sql_child_number,
decode(wait_time, 0, event, 'CPU + CPU Wait') as event,
decode(wait_time, 0, wait_class, 'CPU + CPU Wait') as wait_class
from
{{ v$session }}
where
(program <> 'OraManager' or program is null)
and ((wait_time <> 0 and status = 'ACTIVE') or wait_class <> 'Idle')
