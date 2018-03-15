package example;

import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        return new FilterRegistrationBean(new DoogiesRequestLoggerFilter());
    }
    @Bean
    public ServletRegistrationBean servletRegistrationBean(
            @Value("${servlet.path:/}") String servletPath,
            @Value("${target.url:http://www.google.fi}") String targetPath,
            @Value("${logs.enabled:false}") String logsEnabled
    ){
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new ProxyServlet(), servletPath);
        servletRegistrationBean.addInitParameter("targetUri", targetPath);
        servletRegistrationBean.addInitParameter(ProxyServlet.P_LOG, logsEnabled);
        return servletRegistrationBean;
    }
}