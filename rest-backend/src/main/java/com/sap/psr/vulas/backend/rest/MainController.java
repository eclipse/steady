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
package com.sap.psr.vulas.backend.rest;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.sap.psr.vulas.backend.repo.BugRepositoryImpl;
import com.sap.psr.vulas.backend.util.ReferenceUpdater;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;

/**
 * <p>MainController class.</p>
 *
 */
@Configuration
@ComponentScan({
  "com.sap.psr.vulas.backend.component,com.sap.psr.vulas.backend.rest,com.sap.psr.vulas.backend.util"
})
@EnableAutoConfiguration
@EnableCaching
@EntityScan({
  "com.sap.psr.vulas.backend.model"
}) // So that managed entities in the model package are discovered
@EnableJpaRepositories({
  "com.sap.psr.vulas.backend.repo"
}) // So that repos in the repo package are discovered
public class MainController extends SpringBootServletInitializer {

  /**
   * TODO: Autom. switch off in production.
   * Registers the Servlet for the H2 Web console, which allows checking DB content.
   * Connect using localhost:xxxx/backend/console (JDBC URL jdbc:h2:mem:testdb)
   * @return
   */
  /*@Bean
  @Profile("test")
     ServletRegistrationBean h2servletRegistration(){
         final ServletRegistrationBean registrationBean = new ServletRegistrationBean( new WebServlet());
         registrationBean.addUrlMappings("/console/*");
         registrationBean.addInitParameter("webAllowOthers", "true");
         return registrationBean;
     }*/

  /**
   * Used by custom repository methods to replace nested transient entities by managed ones.
   * See, for instance, {@link BugRepositoryImpl#customSave(com.sap.psr.vulas.backend.model.Bug)}.
   * @return
   */
  @Bean
  ReferenceUpdater refUpdater() {
    return new ReferenceUpdater();
  }

  /**
   * Returns the API info for Swagger.
   * @return
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(
            new Components()
                .addSecuritySchemes(
                    "tenant",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(In.HEADER)
                        .name(Constants.HTTP_TENANT_HEADER))
                .addSecuritySchemes(
                    "space",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(In.HEADER)
                        .name(Constants.HTTP_SPACE_HEADER)))
        .info(
            new Info()
                .title("Vulas REST API")
                .description("This is the REST API of Vulas")
                .version(
                    VulasConfiguration.getGlobal().getConfiguration().getString("shared.version")));
  }

  /**
   * <p>bugApi.</p>
   *
   * @return a {@link org.springdoc.core.GroupedOpenApi} object.
   */
  @Bean
  public GroupedOpenApi bugApi() {
    String paths[] = {"/bugs/**", "/coverage/**", "/cves/**"};
    return GroupedOpenApi.builder().setGroup("bug-api").pathsToMatch(paths).build();
  }

  /**
   * <p>userApi.</p>
   *
   * @return a {@link org.springdoc.core.GroupedOpenApi} object.
   */
  @Bean
  public GroupedOpenApi userApi() {
    String paths[] = {"/apps/**", "/hubIntegration/**", "/libs/**", "/libids/**", "/spaces/**"};
    return GroupedOpenApi.builder().setGroup("user-api").pathsToMatch(paths).build();
  }

  /**
   * <p>adminApi.</p>
   *
   * @return a {@link org.springdoc.core.GroupedOpenApi} object.
   */
  @Bean
  public GroupedOpenApi adminApi() {
    String paths[] = {"/configuration/**", "/tenants/**"};
    return GroupedOpenApi.builder().setGroup("admin-api").pathsToMatch(paths).build();
  }

  /**
   * Can be used to do some initialization at application startup, but does not do anything right now.
   *
   * @return a {@link org.springframework.http.converter.json.Jackson2ObjectMapperBuilder} object.
   */
  //	@Bean
  //	CommandLineRunner init() { return null; }

  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.defaultViewInclusion(true);
    // builder.indentOutput(true).dateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    return builder;
  }

  //  The following bean is commented out because it contains the default
  //  values used anyway by @EnableCaching
  //  @Bean
  //  public CacheManager cacheManager() {
  //      return new ConcurrentMapCacheManager("bug");
  //  }

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
