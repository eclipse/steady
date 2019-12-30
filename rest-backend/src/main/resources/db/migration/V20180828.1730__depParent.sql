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

alter table app_dependency add column origin varchar(255);
alter table app_dependency add column relative_path text;
alter table app_dependency add column parent int8;

alter table app_dependency add constraint FK3q24nj7pisqslyss56g82t7n4 foreign key (parent) references app_dependency;

alter table app_dependency drop constraint uk_bp7iv9k79w4galqwpris6yedl; -- unique (lib, app)

create index IF NOT EXISTS app_dep_index on app_dependency (lib, app);

CREATE UNIQUE INDEX IF NOT EXISTS dep_app_lib_index ON app_dependency (app,lib) where parent is NULL and relative_path is NULL;

CREATE UNIQUE INDEX IF NOT EXISTS dep_app_lib_parent_index ON app_dependency (app,lib,parent) where relative_path is NULL;

CREATE UNIQUE INDEX IF NOT EXISTS dep_app_lib_relpath_index ON app_dependency (app,lib,relative_path) where parent is NULL;