select

o.owner,
o.object_name,
o.object_type,
l.locked_mode

from v$locked_object l
inner join dba_objects o on l.object_id = o.object_id

where l.session_id = :sid
