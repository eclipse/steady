ALTER TABLE lib_constructs
  ADD PRIMARY KEY (lib_id,constructs_id);
ALTER TABLE app_constructs
  ADD PRIMARY KEY (app_id,constructs_id);
ALTER TABLE app_goal_exe_configuration
  ADD PRIMARY KEY (app_goal_exe_id,configuration_id);
ALTER TABLE app_goal_exe_system_info
  ADD PRIMARY KEY (app_goal_exe_id,system_info_id);
ALTER TABLE lib_properties
  ADD PRIMARY KEY (lib_id,properties_id);