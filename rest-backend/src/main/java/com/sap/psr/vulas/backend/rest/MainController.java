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

import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;
import com.sap.psr.vulas.backend.repo.BugRepositoryImpl;
import com.sap.psr.vulas.backend.util.ReferenceUpdater;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.AuthorizationScopeBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * <p>MainController class.</p>
 *
 */
@Configuration
@ComponentScan({
    "com.sap.psr.vulas.backend.component,com.sap.psr.vulas.backend.rest,com.sap.psr.vulas.backend.util"
})
@EnableAutoConfiguration
// @EnableWebMvc
// @SpringBootApplication
@EnableCaching
@EntityScan({
    "com.sap.psr.vulas.backend.model"
}) // So that managed entities in the model package are discovered
@EnableJpaRepositories({
    "com.sap.psr.vulas.backend.repo"
}) // So that repos in the repo package are discovered
@EnableSwagger2
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
    private final ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title("Vulas REST API")
                .description("This is the REST API of Vulas")
                .version(
                        VulasConfiguration.getGlobal()
                                .getConfiguration()
                                .getString("shared.version"))
                .build();
    }

    /**
     * Paths related to vulnerabilities.
     * @return
     */
    @SuppressWarnings("unchecked")
    private Predicate<String> bugPaths() {
        return or(regex("/bugs.*"), regex("/coverage.*"), regex("/cves.*"));
    }

    /**
     * Paths that require tenant selection.
     * @return
     */
    @SuppressWarnings("unchecked")
    private Predicate<String> userPaths() {
        return or(
                regex("/apps.*"),
                regex("/hubIntegration.*"),
                regex("/libs.*"),
                regex("/libids.*"),
                regex("/spaces.*"));
    }

    /**
     * Paths related to configuration and tenant management.
     * @return
     */
    private Predicate<String> configPaths() {
        return or(regex("/configuration.*"), regex("/tenants.*"));
    }

    /**
     * <p>bugApi.</p>
     *
     * @return a {@link springfox.documentation.spring.web.plugins.Docket} object.
     */
    @Bean
    public Docket bugApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("bug-api")
                .apiInfo(this.getApiInfo())
                .select()
                // .apis(RequestHandlerSelectors.any())
                .paths(this.bugPaths())
                .build()
        // .pathMapping("/")
        ;
    }

    /**
     * <p>userApi.</p>
     *
     * @return a {@link springfox.documentation.spring.web.plugins.Docket} object.
     */
    @Bean
    public Docket userApi() {
        AuthorizationScope[] authScopes = new AuthorizationScope[1];

        authScopes[0] =
                new AuthorizationScopeBuilder().scope("read").description("read access").build();

        SecurityReference securityReference1 =
                SecurityReference.builder().reference("tenant").scopes(authScopes).build();

        SecurityReference securityReference2 =
                SecurityReference.builder().reference("space").scopes(authScopes).build();

        ArrayList<SecurityContext> securityContexts =
                newArrayList(
                        SecurityContext.builder()
                                .securityReferences(
                                        newArrayList(securityReference1, securityReference2))
                                .build());

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("user-api")
                .apiInfo(this.getApiInfo())
                .select()
                // .apis(RequestHandlerSelectors.any())
                .paths(this.userPaths())
                .build()
                // .pathMapping("/")
                .securitySchemes(newArrayList(this.tenantKey(), this.spaceKey()))
                .securityContexts(securityContexts);
    }

    /**
     * <p>adminApi.</p>
     *
     * @return a {@link springfox.documentation.spring.web.plugins.Docket} object.
     */
    @Bean
    public Docket adminApi() {
        AuthorizationScope[] authScopes = new AuthorizationScope[1];

        authScopes[0] =
                new AuthorizationScopeBuilder().scope("read").description("read access").build();

        SecurityReference securityReference =
                SecurityReference.builder().reference("test").scopes(authScopes).build();

        ArrayList<SecurityContext> securityContexts =
                newArrayList(
                        SecurityContext.builder()
                                .securityReferences(newArrayList(securityReference))
                                .build());

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(this.getApiInfo())
                .groupName("config-api")
                .select()
                // .apis(RequestHandlerSelectors.any())
                .paths(this.configPaths())
                .build()
                // .pathMapping("/")
                .securitySchemes(newArrayList(new BasicAuth("test")))
                .securityContexts(securityContexts);

        /*.directModelSubstitute(LocalDate.class, String.class).genericModelSubstitutes(ResponseEntity.class)
        .alternateTypeRules(newRule(typeResolver.resolve(DeferredResult.class, typeResolver.resolve(ResponseEntity.class, WildcardType.class)), typeResolver.resolve(WildcardType.class)))
        .useDefaultResponseMessages(false)
        .globalResponseMessage(RequestMethod.GET, newArrayList(new ResponseMessageBuilder().code(500).message("500 message").responseModel(new ModelRef("Error")).build()))
        .securitySchemes(newArrayList(this.tenantKey())).securityContexts(newArrayList(securityContext()));*/
    }

    @Autowired private TypeResolver typeResolver;

    @Bean
    SecurityScheme tenantKey() {
        return new ApiKey("tenant", Constants.HTTP_TENANT_HEADER, "header");
    }

    @Bean
    SecurityScheme spaceKey() {
        return new ApiKey("space", Constants.HTTP_SPACE_HEADER, "header");
    }

    /**
     * <p>securityInfo.</p>
     *
     * @return a {@link springfox.documentation.swagger.web.SecurityConfiguration} object.
     */
    @Bean
    public SecurityConfiguration securityInfo() {
        return new SecurityConfiguration(
                "abc",
                "123",
                "pets",
                "petstore",
                Constants.HTTP_TENANT_HEADER,
                ApiKeyVehicle.HEADER,
                "",
                ",");
    }

    //	@Bean
    //	UiConfiguration uiConfig() {
    //		return new UiConfiguration("validatorUrl");
    //	}

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
