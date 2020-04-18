package com.kyle.activiti.config;

import javax.sql.DataSource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ActivitiConfig {

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Bean
	public SpringProcessEngineConfiguration processEngineConfiguration() {
		SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
		configuration.setDataSource(dataSource);
		configuration.setTransactionManager(transactionManager);
		configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		configuration.setDbHistoryUsed(true);
		configuration.setHistoryLevel(HistoryLevel.FULL);
		configuration.setAsyncExecutorActivate(false);
		configuration.setDeploymentMode("single-resource");
		return configuration;
	}


	@Bean
	public RepositoryService repositoryService(ProcessEngine processEngine) {
	    return processEngine.getRepositoryService();
	}
	
	
	@Bean
	public RuntimeService runtimeService(ProcessEngine processEngine) {
	    return processEngine.getRuntimeService();
	}
	
	@Bean
	public TaskService taskService(ProcessEngine processEngine) {
	    return processEngine.getTaskService();
	}
	
	@Bean
	public HistoryService historyService(ProcessEngine processEngine) {
	    return processEngine.getHistoryService();
	}
	
	@Bean
    public UserDetailsService userDetailsService() {
    	return new UserDetailsService() {
			
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				return null;
			}
		};
    }

}
