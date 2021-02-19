package net.g1park.throwingmoney.business.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("ThrowMoneyExample")
                .description("Mini project for Money throwing")
                .build();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("g1park")
                .apiInfo(this.apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("net.g1park.throwingmoney.business.controller"))
                .paths(PathSelectors.ant("/**")).build();
    }


}
