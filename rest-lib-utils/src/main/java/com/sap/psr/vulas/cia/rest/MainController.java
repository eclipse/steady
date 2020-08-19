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
package com.sap.psr.vulas.cia.rest;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.sap.psr.vulas.java.sign.ASTConstructBodySignature;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import com.sap.psr.vulas.java.sign.gson.ASTConstructBodySignatureDeserializer;
import com.sap.psr.vulas.java.sign.gson.ASTConstructBodySignatureSerializer;
import com.sap.psr.vulas.java.sign.gson.ASTSignatureChangeSerializer;
import com.sap.psr.vulas.python.sign.PythonConstructDigest;
import com.sap.psr.vulas.python.sign.PythonConstructDigestSerializer;

/**
 * <p>MainController class.</p>
 *
 */
@Configuration
@ComponentScan({"com.sap.psr.vulas.cia.util,com.sap.psr.vulas.cia.rest"})
@EnableAutoConfiguration
public class MainController extends SpringBootServletInitializer {

  private static Logger log = LoggerFactory.getLogger(MainController.class);

  /**
   * <p>backendApi.</p>
   *
   * @return a {@link springfox.documentation.spring.web.plugins.Docket} object.
   */
  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder().setGroup("public").pathsToMatch("/**").build();
  }

  /**
   * Can be used to do some initialization at application startup, but does not do anything right now.
   *
   * @return a {@link org.springframework.http.converter.json.Jackson2ObjectMapperBuilder} object.
   */
  //	@Bean
  //	CommandLineRunner init() { return null; }
  //
  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

    builder.defaultViewInclusion(true);
    // builder.indentOutput(true).dateFormat(new SimpleDateFormat("yyyy-MM-dd"));

    // Custom serializers
    Map<Class<?>, JsonSerializer<?>> custom_serializers =
        new HashMap<Class<?>, JsonSerializer<?>>();
    custom_serializers.put(ASTSignatureChange.class, new ASTSignatureChangeSerializer());
    custom_serializers.put(
        ASTConstructBodySignature.class, new ASTConstructBodySignatureSerializer());
    custom_serializers.put(PythonConstructDigest.class, new PythonConstructDigestSerializer());
    builder.serializersByType(custom_serializers);

    // Custom de-serializers
    Map<Class<?>, JsonDeserializer<?>> custom_deserializers =
        new HashMap<Class<?>, JsonDeserializer<?>>();
    custom_deserializers.put(
        ASTConstructBodySignature.class, new ASTConstructBodySignatureDeserializer());
    builder.deserializersByType(custom_deserializers);

    return builder;
  }

  /**
   * <p>main.</p>
   *
   * @param args an array of {@link java.lang.String} objects.
   */
  public static void main(String[] args) {
    SpringApplication.run(MainController.class, args);
  }

  /** {@inheritDoc} */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(MainController.class);
  }
}
