select command_type, command_name
from {{ v$sqlcommand }}
where command_name is not null
