select

plan_hash_value,
min(child_number) as min_child,

count(1) as child_cursors,
sum(loads) as loads,
sum(invalidations) as invalidations,

sum(parse_calls) as parse_calls,
sum(executions) as executions,
sum(fetches) as fetches,
sum(rows_processed) as rows_processed,
sum(buffer_gets) as buffer_gets,
sum(disk_reads) as disk_reads,
sum(direct_writes) as direct_writes,

sum(elapsed_time) as elapsed_time,
sum(cpu_time) as cpu_time,
sum(application_wait_time) as application_wait_time,
sum(cluster_wait_time) as cluster_wait_time,
sum(concurrency_wait_time) as concurrency_wait_time,
sum(user_io_wait_time) as user_io_wait_time

from {{ v$sql }} where sql_id = :sqlId
group by plan_hash_value
