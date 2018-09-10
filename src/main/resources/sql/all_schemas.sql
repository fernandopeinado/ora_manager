select
username as owner
from
{{ all_users }}
order by 1
