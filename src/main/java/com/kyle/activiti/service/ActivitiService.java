package com.kyle.activiti.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

public interface ActivitiService {

	Deployment deployProcessDefinition(String bpmnFilePath,String pngFilePath,String processName);

	void startProcess(String processDefinetionKey,String businessKey, Map<String, Object> variables );

	List<String> findPersonalTaskList(String person, String processDefinitionKey);

	void completeTask(String taskId,String comment);
	
	void cancelTask(String taskId, String reason);

	void rejectTask(String taskId, String nodeId, String comment);
	
	List<Map<String,Object>> getTaskList(String processInstanceId);

	List<Map<String,Object>> queryProceccDefinition(String processDefinitionKey);

	void deleteDeployment(String id, boolean cascade);

	List<HistoricActivityInstance> getHistory(String processInstanceId);
	
	List<Map<String,Object>> queryProcessInstance(String processDefinitionKey);
	
	void suspendOrActivateProcessDefinition(String processDefinitionId);
	
	void suspendOrActiveProcessInstance(String processInstanceId);
	
	InputStream getProcessResources(String processDefinitionId);

}
