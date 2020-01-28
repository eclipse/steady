--
-- This file is part of Eclipse Steady.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
-- SPDX-License-Identifier: Apache-2.0
--
-- Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
--

--alter table app_goal_exe_statistics rename column goal_execution_id to app_goal_exe_id;
alter table app_constructs rename column app_id to application_id;
alter table app_dependency_reachable_construct_ids rename column app_dependency_id to dependency_id;

alter table app_goal_exe_configuration rename column app_goal_exe_id to goal_execution_id;
alter table app_goal_exe_system_info rename column app_goal_exe_id to goal_execution_id;

alter table lib_constructs rename column lib_id to library_id;

alter table lib_properties rename column lib_id to library_id;