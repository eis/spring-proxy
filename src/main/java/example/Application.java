package example;

import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        return new FilterRegistrationBean(new HTTPTrafficLoggerFilter());
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

    // this is needed for proxy filter to work properly
    // https://github.com/mitre/HTTP-Proxy-Servlet/issues/83
    @Bean
    public FilterRegistrationBean registration(HiddenHttpMethodFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }
}