package neltia.bloguide.api.infrastructure.driver;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@OpenAPIDefinition(
        info = @Info(
                title = "Spring API Development Base Code",
                description = "Java Spring API Base Code With MariaDB & Elasticsearch",
                version = "0.0.1"
        )
)
@Configuration
public class SwaggerConfig {
    @Bean
    @Profile("!prod") // 운영 환경에서는 Swagger 비활성화
    public OpenAPI apiDocket() {
        Components components = new Components();
        return new OpenAPI()
                .components(components);
    }
}
