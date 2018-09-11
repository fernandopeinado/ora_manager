select sql_text, command_type from {{ v$sqlarea }} where sql_id = :sqlId
