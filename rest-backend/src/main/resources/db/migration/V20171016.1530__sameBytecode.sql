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

create table bug_affected_construct_change_same_bytecode_lids (affected_construct_change_id int8 not null, same_bytecode_lids_id int8 not null);
alter table bug_affected_construct_change_same_bytecode_lids add constraint FKfirn9bju9powr15m6rd5883g3 foreign key (same_bytecode_lids_id) references library_id;
alter table bug_affected_construct_change_same_bytecode_lids add constraint FK2j92tqd59rkb75xo97r70w465 foreign key (affected_construct_change_id) references bug_affected_construct_change;
ALTER TABLE bug_affected_construct_change_same_bytecode_lids ADD PRIMARY KEY (affected_construct_change_id,same_bytecode_lids_id);
alter table bug_affected_construct_change drop column ast_equal;