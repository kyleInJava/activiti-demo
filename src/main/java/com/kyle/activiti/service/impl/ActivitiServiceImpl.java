package com.kyle.activiti.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.kyle.activiti.dao.ActivitiMapper;
import com.kyle.activiti.service.ActivitiService;

@Service
public class ActivitiServiceImpl implements ActivitiService {
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService ;
	@Autowired
	private TaskService taskService ;
	@Autowired
	private HistoryService historyService ;
	@Autowired
	private ActivitiMapper activitiMapper;
	

	@Override
	public Deployment deployProcessDefinition(String bpmnFilePath,String pngFilePath,String processName) {
		Deployment deployment = repositoryService.createDeployment()
				.addClasspathResource(bpmnFilePath)
				.addClasspathResource(pngFilePath)
				.name(processName)
				.deploy();
		return deployment; 
	}

	@Override
	public void startProcess(String processDefinetionKey,String businessKey,Map<String, Object> variables ) {
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinetionKey,businessKey,variables);
	}
	
	@Override
	public List<String> findPersonalTaskList(String person,String processDefinitionKey) {
		TaskQuery taskQuery = taskService.createTaskQuery().taskAssignee(person);
		if(!StringUtils.isEmpty(processDefinitionKey)) {
			taskQuery = taskQuery.processDefinitionKey(processDefinitionKey);
		}
		List<Task> list = taskQuery.active().orderByTaskId().desc().list();
		List<String> resultList = new ArrayList<>();
		list.forEach(t -> resultList.add(t.getId()));
		return resultList;
	}


	@Override
	public void completeTask(String taskId,String comment) {
		
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if(task != null) {
			String processInstanceId = task.getProcessInstanceId();
			//设置评论
			taskService.addComment(taskId, processInstanceId, comment);
			taskService.complete(taskId);//完成当前task后，要设置后续的task的处理人
			//找到后续的节点，然后设置节点的处理人
			List<Task> tasks = taskService.createTaskQuery().active().processInstanceId(processInstanceId).list();
			for(Task t : tasks) {
				if(t.getAssignee() == null ) {
					//这里就可以根据我们在流程图中自定义的信息结合数据库处理
					String taskDefinitionKey = t.getTaskDefinitionKey();
					taskService.claim(t.getId(), taskDefinitionKey);
				}
			}
		}
		
	}
	
	@Override
	public void cancelTask(String taskId,String reason) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		String processInstanceId = task.getProcessInstanceId();
		runtimeService.deleteProcessInstance(processInstanceId, reason);
	}
	
	
	@Override
	public void rejectTask(String taskId,String backTaskId,String comment) {
		Task curTask = taskService.createTaskQuery().taskId(taskId).singleResult();
		String processDefinitionId = curTask.getProcessDefinitionId();
		String processInstanceId = curTask.getProcessInstanceId();
		BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
		List<HistoricActivityInstance> historicActivityInstances =historyService
					.createHistoricActivityInstanceQuery()
					.processInstanceId(processInstanceId)
					.finished().list();
		HistoricActivityInstance backActInstance =null;
		for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
			if(backTaskId.equals(historicActivityInstance.getTaskId())){
				backActInstance = historicActivityInstance;
				break;
			}
		}
		FlowNode backNode = (FlowNode)model.getMainProcess().getFlowElement(backActInstance.getActivityId());
		Execution execution =runtimeService.createExecutionQuery()
						.executionId(curTask.getExecutionId()).singleResult();
		FlowNode curNode = (FlowNode)model.getMainProcess().getFlowElement(execution.getActivityId());
		
		//获取当前节点的出链接，然后进行备份后
		List<SequenceFlow> outgoingFlows = curNode.getOutgoingFlows();
		List<SequenceFlow> temp = new ArrayList<>(outgoingFlows);
		outgoingFlows.clear();
		//创建新的连接，并将当前节点和要退回的节点连接起来
		SequenceFlow sf = new SequenceFlow();
		sf.setId("backSequenceFlow");
		sf.setSourceFlowElement(curNode);
		sf.setTargetFlowElement(backNode);
		outgoingFlows.add(sf);
		curNode.setOutgoingFlows(outgoingFlows);
		//存在并行节点，就要判断回退的节点是在并行网关之前还是之后，如果是之后，说明
		//找到当前所有的任务
//		List<Task> tasks = taskService.createTaskQuery().active().processInstanceId(processInstanceId).list();
//		if(tasks.size() > 1) {
//			for(Task t: tasks) {
//				if(!taskId.equals(t.getId())) {
//					FlowNode tNode = (FlowNode)model.getProcessById(processInstanceId).getFlowElement(t.getExecutionId());
//					List<SequenceFlow> incomingFlows = tNode.getOutgoingFlows();
//					tNode.setOutgoingFlows(new ArrayList<>());
//					taskService.complete(t.getId());
//					tNode.setOutgoingFlows(incomingFlows);
//				}
//			}
//		}
		
		//完成当前任务
		completeTask(taskId,comment);
		//恢复原来的连接
		curNode.setOutgoingFlows(temp);
	}
	
	
	
	@Override
	@SuppressWarnings("deprecation")
	public List<Map<String,Object>> getTaskList(String processInstanceId) {
		List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
		.processInstanceId(processInstanceId).activityType("userTask").orderByHistoricActivityInstanceId().asc().list();
		
		List<Map<String,Object>> resultList = new ArrayList<>();
		List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);
		for(HistoricActivityInstance instance : list) {
			Map<String,Object> map = new HashMap<>();
			map.put("processInstanceId", instance.getProcessInstanceId());
			map.put("taskId", instance.getTaskId());
			map.put("activityId", instance.getActivityId());
			map.put("activityName", instance.getActivityName());
			map.put("assignee", instance.getAssignee());
			map.put("startTime", instance.getStartTime());
			map.put("endTime", instance.getEndTime());
			for(Comment c : comments) {
				if(c.getTaskId().equals(instance.getTaskId())) {
					map.put("comment",c.getFullMessage());
				}
			}
			resultList.add(map);
		}
		return resultList;
	}
	
	
	

	@Override
	public List<Map<String,Object>> queryProceccDefinition(String processDefinitionKey) {
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
									.processDefinitionKey(processDefinitionKey)
									.orderByProcessDefinitionVersion().desc().list(); 
		List<Map<String,Object>> result = new ArrayList<>();
		for (ProcessDefinition processDefinition : list) {
			Map<String,Object> map = new HashMap<>();
			map.put("processDefinitionId", processDefinition.getId());
			map.put("processDefinitionName", processDefinition.getName());
			map.put("processDefinitionKey", processDefinition.getKey());
			result.add(map);
		}
		
		return result;
	}

	@Override
	public void deleteDeployment(String deploymentId, boolean cascade) {
		//删除流程定义
		repositoryService.deleteDeployment(deploymentId, cascade);
	}
	
	@Override
	public List<HistoricActivityInstance> getHistory(String processInstanceId) {
		List<HistoricActivityInstance> list  = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();
		return list;
	}

	@Override
	public List<Map<String,Object>> queryProcessInstance(String processDefinitionKey) {
		List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).list();
		List<Map<String,Object>> result = new ArrayList<>();
		for (ProcessInstance processInstance : list) {
			Map<String,Object> map = new HashMap<>();
			map.put("processInstanceId", processInstance.getProcessInstanceId());
			map.put("processDefinitionId", processInstance.getProcessDefinitionId());
			map.put("idEnded", processInstance.isEnded());
			map.put("isSuspended", processInstance.isSuspended());
			map.put("activityId", processInstance.getActivityId());
			result.add(map);
		}
		
		return result;
	}

	@Override
	public void suspendOrActivateProcessDefinition(String processDefinitionId) {
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
		boolean suspend = processDefinition.isSuspended(); 
		if(suspend) {//如果是暂停的就激活
			repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
		}else {//如果是激活的就挂起
			repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null); 
		}
	}

	@Override
	public void suspendOrActiveProcessInstance(String processInstanceId) {
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
		boolean suspended = processInstance.isSuspended();
		if(suspended) {
			runtimeService.activateProcessInstanceById(processInstanceId); 
		}else {
			runtimeService.suspendProcessInstanceById(processInstanceId); 
		}
	}

	@Override
	public InputStream getProcessResources(String processDefinitionId){
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.processDefinitionId(processDefinitionId).singleResult(); 
		//String resourceName = processDefinition.getResourceName();
		String diagramResourceName = processDefinition.getDiagramResourceName();
		InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), diagramResourceName);
		return resourceAsStream;
	}

}
