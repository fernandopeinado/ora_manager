select

sid,
serial#,
username,
program,
blocking_session_status,
blocking_instance,
blocking_session

from {{ v$session }} where sid = :sid
