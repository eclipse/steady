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

create table lib_bundled_library_ids (library_id int8 not null, bundled_library_ids_id int8 not null);
alter table lib_bundled_library_ids add constraint FKpfkpmqs18pra09mdrt5ln4qui foreign key (bundled_library_ids_id) references library_id;
alter table lib_bundled_library_ids add constraint FK3nbep3t70cu9sc3ggoscnd96 foreign key (library_id) references lib;
