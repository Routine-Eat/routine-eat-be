package com.likelion.routineeatbe.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // application.yml의 server url 값을 유동적으로 받는다.(local, dev, prod), 없으면 localhost로 지정해준다.
    @Value("${server.url:http://localhost:8080}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {

        Server localServer = new Server();
        localServer.setUrl(contextPath);
        localServer.setDescription("Local Server");

        return new OpenAPI()
                .addServersItem(localServer)
                .info(new Info().title("Routine-Eat API 명세서").version("1.0").description("2026 멋쟁이 사자처럼 중앙해커톤 API 명세서"));
                //API 전체 설정 정보
    }

    @Bean
    public GroupedOpenApi customGroupedOpenApi() {
        return GroupedOpenApi.builder().group("api").pathsToMatch("/**").build();
    }
}

