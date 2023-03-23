package com.example.swagger_test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

/**
 * @author xiaoZhao
 * @date 2022/10/24
 * @describe
 */
@Configuration
@EnableSwagger2 // 开启Swagger2
public class SwaggerConfig {

    // 配置Swagger Docket
    @Bean
    public Docket docket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo());
    }

    // 配置Swagger信息
    private ApiInfo apiInfo(){
        // 作者信息
        Contact contact = new Contact("小赵", "https://blog.csdn.net/Zp_insist?type=blog", "test@qq.com");

        return new ApiInfo("测试 Swagger API",
                "一个工程用来测试Swagger的使用",
                "1.0",
                "https://blog.csdn.net/Zp_insist?type=blog",
                contact,
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList<VendorExtension>());
    }
}