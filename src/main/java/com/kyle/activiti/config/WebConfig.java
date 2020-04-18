package com.kyle.activiti.config;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;

@Configuration
public class WebConfig {
	
	//配置fastjson
	@Bean
	public HttpMessageConverter configureMessageConverters() {
	    FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
	    FastJsonConfig config = new FastJsonConfig();
	    config.setSerializerFeatures(
	    //保留map空的字段
		SerializerFeature.WriteMapNullValue,
		//将String类型的null转成""
		SerializerFeature.WriteNullStringAsEmpty,
		//将Number类型的null转成0
		SerializerFeature.WriteNullNumberAsZero,
		//将List类型的null转成[]
		SerializerFeature.WriteNullListAsEmpty,
		//将Boolean类型的null转成false
		SerializerFeature.WriteNullBooleanAsFalse,
		//避免循环引用
		 SerializerFeature.DisableCircularReferenceDetect);
		
		converter.setFastJsonConfig(config);
		converter.setDefaultCharset(Charset.forName("UTF-8"));
		List<MediaType> mediaTypeList = new ArrayList<>();
		// 解决中文乱码问题，相当于在Controller上的@RequestMapping中加了个属性produces = "application/json"
		mediaTypeList.add(MediaType.APPLICATION_JSON);
		converter.setSupportedMediaTypes(mediaTypeList);
		return converter;
	}
	
	
	/**
	 * 跨域配置
	 * @Title: corsFilter  
	 * @Description: TODO(这里用一句话描述这个方法的作用)  
	 * @param @return    参数  
	 * @return CorsFilter    返回类型
	 */
	@Bean
	public CorsFilter corsFilter() {
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", corsConfig());
	    return new CorsFilter(source);
	}
	
	private CorsConfiguration corsConfig() {
	    CorsConfiguration corsConfiguration = new CorsConfiguration();
	    /*
	    * 请求常用的三种配置，*代表允许所有，当时你也可以自定义属性（比如header只能带什么，只能是post方式等等）
	    */
	    corsConfiguration.addAllowedOrigin("*");
	    corsConfiguration.addAllowedHeader("*");
	    corsConfiguration.addAllowedMethod("*");
	    corsConfiguration.setAllowCredentials(true);
	    corsConfiguration.setMaxAge(3600L);
	    return corsConfiguration;
	}

}
