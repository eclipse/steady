package com.sap.vulas.gradle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface GradleTestProject {
    String name();
}