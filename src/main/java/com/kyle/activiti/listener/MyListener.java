package com.kyle.activiti.listener;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("MyListener")
public class MyListener implements ExecutionListener,TaskListener {
	private static final long serialVersionUID = 1L;
	
	@Autowired
    private TaskService taskService;


	@Override
	public void notify(DelegateExecution execution) {
		String eventName = execution.getEventName();
		switch(eventName) {
			case "start":
				System.out.println(eventName);
				break;
			case "end":
				System.out.println(eventName);
				break;
			case "take":
				System.out.println(eventName);
				break;
		}
	}

	@Override
	public void notify(DelegateTask delegateTask) {
		String eventName = delegateTask.getEventName();
		String taskId = delegateTask.getId();
		switch(eventName) {
		case "create":
			System.err.println(taskId+":"+eventName);
			break;
		case "assignment":
			System.err.println(taskId+":"+eventName);
			break;
		case "complete":
			Object variable = taskService.getVariable(taskId, "money");
			System.out.println(variable);
			System.err.println(taskId+":"+eventName);
			break;
		case "delete":
			System.err.println(taskId+":"+eventName);
			break;
		}
	}

}
