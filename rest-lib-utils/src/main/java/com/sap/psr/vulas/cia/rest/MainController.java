package com.sap.psr.vulas.cia.rest;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.sap.psr.vulas.java.sign.ASTConstructBodySignature;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import com.sap.psr.vulas.java.sign.gson.ASTConstructBodySignatureDeserializer;
import com.sap.psr.vulas.java.sign.gson.ASTConstructBodySignatureSerializer;
import com.sap.psr.vulas.java.sign.gson.ASTSignatureChangeSerializer;
import com.sap.psr.vulas.python.sign.PythonConstructDigest;
import com.sap.psr.vulas.python.sign.PythonConstructDigestSerializer;
import com.sap.psr.vulas.shared.util.Constants;


import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
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
@ComponentScan
@EnableAutoConfiguration
@EnableSwagger2
public class MainController extends SpringBootServletInitializer {
	
	private static Logger log = LoggerFactory.getLogger(MainController.class);

	/**
	 * <p>backendApi.</p>
	 *
	 * @return a {@link springfox.documentation.spring.web.plugins.Docket} object.
	 */
	@Bean
	public Docket backendApi() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build().pathMapping("/")
				.apiInfo(new springfox.documentation.service.ApiInfo("Vulas CIA", "RESTful API for discovering and analyzing alternative Maven artifacts", "1.1.0-SNAPSHOT", "SAP", null, "commercial", null))
				.directModelSubstitute(LocalDate.class, String.class).genericModelSubstitutes(ResponseEntity.class)
				.alternateTypeRules(newRule(typeResolver.resolve(DeferredResult.class, typeResolver.resolve(ResponseEntity.class, WildcardType.class)), typeResolver.resolve(WildcardType.class)))
				.useDefaultResponseMessages(false)
				.globalResponseMessage(RequestMethod.GET, newArrayList(new ResponseMessageBuilder().code(500).message("500 message").responseModel(new ModelRef("Error")).build()));
		//.securitySchemes(newArrayList(apiKey())).securityContexts(newArrayList(securityContext()));
	}

	@Autowired
	private TypeResolver typeResolver;

	private ApiKey apiKey() {
		return new ApiKey("mykey", "api_key", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth()).forPaths(PathSelectors.regex("/anyPath.*")).build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return newArrayList(new SecurityReference("mykey", authorizationScopes));
	}

	@Bean
	SecurityConfiguration security() {
		return new SecurityConfiguration("abc", "123", "pets", "petstore", Constants.HTTP_TENANT_HEADER, ApiKeyVehicle.HEADER, "", ",");
	}
//
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
//
	@Bean
	public Jackson2ObjectMapperBuilder jacksonBuilder() {
		final Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

		builder.defaultViewInclusion(true);
		//builder.indentOutput(true).dateFormat(new SimpleDateFormat("yyyy-MM-dd"));

		// Custom serializers
		Map<Class<?>,JsonSerializer<?>> custom_serializers = new HashMap<Class<?>,JsonSerializer<?>>();
		custom_serializers.put(ASTSignatureChange.class, new ASTSignatureChangeSerializer());
		custom_serializers.put(ASTConstructBodySignature.class, new ASTConstructBodySignatureSerializer());
		custom_serializers.put(PythonConstructDigest.class, new PythonConstructDigestSerializer());
		builder.serializersByType(custom_serializers);

		// Custom de-serializers
		Map<Class<?>,JsonDeserializer<?>> custom_deserializers = new HashMap<Class<?>,JsonDeserializer<?>>();
		custom_deserializers.put(ASTConstructBodySignature.class, new ASTConstructBodySignatureDeserializer());
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
