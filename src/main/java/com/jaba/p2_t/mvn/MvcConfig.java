package com.jaba.p2_t.mvn;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

        @SuppressWarnings("null")
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {

                String announsmetnts = "file:/var/lib/asterisk/sounds/voicemessages/";

                ///////////////////////////////////////////////////////////

                registry
                                .addResourceHandler("/announcement/**")
                                .addResourceLocations(
                                                announsmetnts

                                )
                                .setCachePeriod(0);
        }


        @Override
        public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                                .allowedOrigins("*")
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedHeaders("*");
        }

}
