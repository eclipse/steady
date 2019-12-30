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

create table bug_references (bug_id int8 not null, reference varchar(1024) not null, primary key (bug_id,reference));
alter table bug_references add constraint FKb05vl8sr4x7u4dsvrfupb79nh foreign key (bug_id) references bug;
alter table app_dependency add column path text;

INSERT INTO bug_references (bug_id,reference) SELECT id,url FROM bug where url is not null and not url='';

ALTER TABLE bug DROP COLUMN url;