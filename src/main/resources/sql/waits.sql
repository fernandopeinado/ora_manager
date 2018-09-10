select wait_class as wait_class, sum(time_waited_micro) as time_waited_micros
from {{ v$system_event }} where wait_class <> 'Idle' group by wait_class

union all

select 'CPU', sum(value) from {{ v$sys_time_model }} where stat_name in ('DB CPU', 'background cpu time')
