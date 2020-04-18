package com.kyle.activiti.controller;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.history.HistoricActivityInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyle.activiti.service.ActivitiService;

@RestController
@RequestMapping("activiti")
public class ActivitiController {
	
	@Autowired
	private ActivitiService activitiService;
	
	
	/**
	 * 部署流程定义
	 */
	@RequestMapping("deploy")
	public void deployProcessDefinition(String bpmnFilePath,String pngFilePath,String processName) {
		activitiService.deployProcessDefinition(bpmnFilePath,pngFilePath,processName);
	}
	
	/**
	 * 启动一个流程实例
	 * @param key 流程定义的key
	 */
	@RequestMapping("start")
	public void startProcess(String processDefinetionKey,String businessKey) {
		 Map<String, Object> variables = new HashMap<>();
		 variables.put("sponsor", "eric");
		 variables.put("money", 1000);
		activitiService.startProcess(processDefinetionKey,businessKey,variables);
	}
	
	
	/**
	 * 查询人员需要处理的任务
	 * @param person
	 */
	@RequestMapping("findPersonTaskList")
	public List<String> findPersonalTaskList(String person, String processDefinitionKey) {
		return activitiService.findPersonalTaskList(person,processDefinitionKey);
	}
	
	/**
	 * 完成任务
	 * @param taskId
	 */
	@RequestMapping("completeTask")
	public void completeTask(String taskId,String comment) {
		activitiService.completeTask(taskId,comment);
	}
	
	
	/**
	 * 取消流程
	 * @param taskId
	 */
	@RequestMapping("cancelTask")
	public void cancelTask(String taskId,String reason) {
		activitiService.cancelTask(taskId,reason);
	}
	
	
	/**
	   *     驳回流程
	 * @param taskId
	 * @param backTaskId
	 * @param comment
	 */
	@RequestMapping("rejectTask")
	public void rejectTask(String taskId,String backTaskId, String comment){
		activitiService.rejectTask(taskId, backTaskId,comment);
	}
	
	/**
	   *   查询某个流程的审批进度
	 * @param processInstanceId
	 * @return
	 */
	@RequestMapping("taskList")
	public List<Map<String,Object>> getTaskList(String processInstanceId) {
		 return activitiService.getTaskList(processInstanceId);
	}
	
	
	/**
	   * 查询流程定义
	 * @param processDefinitionKey
	 */
	@RequestMapping("queryProceccDefinition")
	public List<Map<String,Object>> queryProceccDefinition(String processDefinitionKey) {
		return activitiService.queryProceccDefinition(processDefinitionKey);
	}
	
	/**
	   *     删除流程定义 
	 * @param deploymentId 为act_re_deployment 表中的id
	 */
	@RequestMapping("deleteDeployment")
	public void deleteDeployment(String deploymentId, boolean cascade) {
		activitiService.deleteDeployment(deploymentId,cascade);
	}
	
	/**
	 * 流程历史信息查询
	 * @param id  act_hi_procinst表中的id
	 */
	@RequestMapping("history")
	public List<HistoricActivityInstance> getHistory(String processInstanceId) {
		return activitiService.getHistory(processInstanceId);
	}
	
	
	/**
	   *   查询流程实例
	 * @param key  流程的key
	 */
	@RequestMapping("queryProcessInstance")
	public List<Map<String,Object>> queryProcessInstance(String processDefinitionKey) {
		return activitiService.queryProcessInstance(processDefinitionKey);
	}
	
	/**
	 * 挂起、激活所有相关流程
	 * @param id 流程的定义id
	 */
	@RequestMapping("suspendOrActivateProcessDefinition")
	public void suspendOrActivateProcessDefinition(String processDefinitionId) {
		activitiService.suspendOrActivateProcessDefinition(processDefinitionId);
	}
	
	/**
	 * 挂起或激活单个流程
	 * @param id ProcessInstancde 的id
	 */
	@RequestMapping("suspendOrActiveProcessInstance")
	public void suspendOrActiveProcessInstance(String processInstanceId) {
		activitiService.suspendOrActiveProcessInstance(processInstanceId);
	}
	

	/**
	 * 流程定义资源查询 
	 * @param processDefinitionId
	 * @throws IOException 
	 * @throws Exception
	 */
	@RequestMapping("getProcessResources")
	public void getProcessResources(String processDefinitionId,HttpServletResponse response) throws IOException{
		InputStream processResources = activitiService.getProcessResources(processDefinitionId);
		ServletOutputStream outputStream = response.getOutputStream();
		
		byte[] b = new byte[1024];   
		int len = -1;   
		while ((len = processResources.read(b, 0, 1024)) != -1) {
			outputStream.write(b, 0, len);   
		} 
		response.setContentType("image/png");
		response.flushBuffer();
		processResources.close();
		outputStream.close();
	}
	
}
