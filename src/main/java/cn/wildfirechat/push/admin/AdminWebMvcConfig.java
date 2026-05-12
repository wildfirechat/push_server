package cn.wildfirechat.push.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthFilter adminAuthFilter;

    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminAuthFilterRegistration() {
        FilterRegistrationBean<AdminAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(adminAuthFilter);
        registration.addUrlPatterns("/admin/*", "/api/admin/*");
        registration.setOrder(1);
        return registration;
    }
}
