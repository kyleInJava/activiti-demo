package com.kyle.activiti.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;

//添加druid监控功能
@Configuration
public class DruidConfig {
  
  //创建datasource并向其中注入属性
  @Bean
  @ConfigurationProperties(prefix = "spring.datasource.druid")
  public DruidDataSource druid() {
      return new DruidDataSource();
  }
  
  /**
   * Druid的servlet
   * @return
   */
  @Bean
  public ServletRegistrationBean<StatViewServlet> statViewServlet() {
      ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<StatViewServlet>(new StatViewServlet());
      Map<String, String> initParams = new HashMap<>();
      initParams.put("loginUsername", "admin");//登录用户名
      initParams.put("loginPassword", "123");//登录密码
      initParams.put("allow","127.0.0.1");//允许访问的ip
      bean.setInitParameters(initParams);
      bean.setUrlMappings(Arrays.asList("/druid/*"));//访问路径
      return bean;
  }
  
  @Bean
  public FilterRegistrationBean<WebStatFilter> webStatFilter() {
      FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>(new WebStatFilter());
      HashMap<String, String> initParams = new HashMap<>();
      initParams.put("exclusions", "*.js,*.css,/druid/*");
      bean.setInitParameters(initParams);
      bean.setUrlPatterns(Arrays.asList("/*"));
      return bean;
  }
}