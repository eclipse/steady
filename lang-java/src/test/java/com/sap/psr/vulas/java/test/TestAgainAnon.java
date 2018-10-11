package com.sap.psr.vulas.java.test;

import java.io.Serializable;

public class TestAgainAnon {

	// Member class
	class Class_A {

		void method_A() {

			// Anonymous class (as method variable)
			final Serializable anon = new Serializable() {
				void method_B() {
				}
			};
		}

		void method_C() {

			// Anonymous class (as method variable)
			final Serializable anon = new Serializable() {
				void method_D() {
				}
			};
		}
	}
	
	//////////////////////////////////
	

	public void method_E() {

		int i = 0;

		// // Anonymous class (as method variable)
		final Serializable anon = new Serializable() {
			void method_F() {
				final Serializable anon1 = new Serializable() {
					void method_G() {
						return;
					}
				};
				final Serializable anon2 = new Serializable() {
					void method_H() {
						return;
					}
				};
			}
		};
		//
		// Named class
		class Class_B {

			void method_L() {

				// Anonymous class (as method variable)
				final Serializable anon = new Serializable() {
					void method_M() {
						// Anon inside anon
						final Serializable anon2 = new Serializable() {
							void method_N() {
							}
						};
					}
				};
			}
		}

	}

}