/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
import java.io.Serializable;

public class NestedDeclarationMess {

	// Member interface
	interface DoSomethingElse {
		
		// Member class
		class DoThis {
			
			void doThis() {
				
				// Anonymous class (as method variable)
				final Serializable anon = new Serializable() {
					void doThat() {}
				};
			}
		}
		
		public void doSomethingElse();
	}
	
	// Member class (anon)
	final DoSomethingElse doSomethingElse = new DoSomethingElse() {
		public void doSomethingElse() {}
	};
	
	// Member class
	class DoThis {
		
		void doThis() {
			
			// Anonymous class (as method variable)
			final Serializable anon = new Serializable() {
				void doThat() {}
			};
		}
	}
	
	public void doSomething() {
		
		// Anonymous class (as method variable)
		final Serializable anon = new Serializable() {
			void doSomething() {}
		};
		
		// Named class
		class DoThat {
			
			void doThat() {
				
				// Anonymous class (as method variable)
				final Serializable anon = new Serializable() {
					void doThis() {}
				};
			}
		}
	}
	
	public enum Foo {
		A, B;
		void bar() {
			// Named class
			class DoThis {
				
				void doThis() {
					
					// Anonymous class (as method variable)
					final Serializable anon = new Serializable() {
						void doThat() {}
					};
				}
			}
		}
	};
}
