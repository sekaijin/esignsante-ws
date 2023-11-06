/**
 * (c) Copyright 1998-2021, ANS. All rights reserved.
 */

package fr.gouv.esante.api.sign.config;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import fr.gouv.esante.api.sign.service.impl.utils.Version;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.paths.DefaultPathProvider;
import springfox.documentation.spring.web.plugins.Docket;

import jakarta.servlet.ServletContext;
import java.text.ParseException;

/**
 * The Class OpenAPIDocumentationConfig.
 */
@Configuration
public class OpenAPIDocumentationConfig {

    /** Default ESignSante major version. */
    private static final int MAJOR = 2;

    /**
     * The log.
     */
    Logger log = LoggerFactory.getLogger(OpenAPIDocumentationConfig.class);

    /** ESignSante Build Properties. */
    @Autowired
    private BuildProperties buildProperties;

    /**
     * Api info.
     *
     * @return the api info
     */
    ApiInfo apiInfo() {
        Version wsVersion = new Version(MAJOR, 0, 0, 0);
        try {
            // assign the current version of esignsante-webservices
            wsVersion = new Version(buildProperties.getVersion());
        } catch (final ParseException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return new ApiInfoBuilder().title("ESignSante").description(
                "API du composant ESignSante.  Ce composant dit de \"signature\" mutualise et homogénéise " +
                        "la mise en oeuvre des besoins autour de la signature.  Il permet aux partenaires " +
                        "de l'ANS de signer leurs documents ainsi que de vérifier la validité " +
                        "d'une signature ou d'un certificat.     ")
                .license("").licenseUrl("http://unlicense.org").termsOfServiceUrl("").version(wsVersion.getVersion())
                .contact(new Contact("", "", "esignsante@asipsante.fr")).build();
    }

    /**
     * Custom implementation.
     * @param <T>
     *
     * @param basePath the base path
     * @return the docket
     */
    @SuppressWarnings("unchecked")
	@Bean
    public <T> Docket customImplementation(@Value("${openapi.aSIPSign.base-path:/}") final String basePath) {
        return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false).select()
                .apis(RequestHandlerSelectors.basePackage("fr.gouv.esante.api.sign.ws.api"))
                .paths((Predicate<String>) Predicates.not((Predicate<T>) PathSelectors.regex("/error.*"))).build()
                .directModelSubstitute(org.threeten.bp.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(org.threeten.bp.OffsetDateTime.class, java.util.Date.class).apiInfo(apiInfo());
    }

    /**
     * The Class BasePathAwareRelativePathProvider.
     */
    static class BasePathAwareRelativePathProvider extends DefaultPathProvider {

        /** The base path. */
        private String basePath;

        /**
         * Instantiates a new base path aware relative path provider.
         *
         * @param servletContext the servlet context
         * @param basePath       the base path
         */
        public BasePathAwareRelativePathProvider(final ServletContext servletContext, final String basePath) {
            this.basePath = basePath;
        }

        /*
         * (non-Javadoc)
         * 
         * @see springfox.documentation.spring.web.paths.AbstractPathProvider#
         * getOperationPath(java.lang.String)
         */
        @Override
        public String getOperationPath(final String operationPath) {
            final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/");
            return Paths.removeAdjacentForwardSlashes(uriComponentsBuilder.path(operationPath.replaceFirst(
                    "^" + basePath, "")).build().toString());
        }
    }

}