/**
 * This package contains a large share of the tool's data model.
 * 
 * They facilitate data
 * sharing between different modules, either by explicit (de)serialization through
 * {@link com.sap.psr.vulas.shared.json.JacksonUtil} or implicit through the use
 * of Spring Rest.
 * 
 * Many of those classes also exist as JPA entities in module vulas-backend. While their
 * JSON representation MUST be compatible, they implement individual helper methods for
 * their respective context.
 * 
 */
package com.sap.psr.vulas.shared.json.model;