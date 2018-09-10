select distinct wait_class from {{ v$event_name }} where wait_class <> 'Idle'
