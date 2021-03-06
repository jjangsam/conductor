package com.netflix.conductor.core.execution.mapper;

import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.core.execution.ParametersUtils;
import com.netflix.conductor.core.execution.TerminateWorkflowException;
import com.netflix.conductor.core.utils.IDGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DynamicTaskMapperTest {

    private ParametersUtils parametersUtils;
    private DynamicTaskMapper dynamicTaskMapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        parametersUtils = mock(ParametersUtils.class);
        dynamicTaskMapper = new DynamicTaskMapper(parametersUtils);
    }

    @Test
    public void getMappedTasks() throws Exception {

        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName("DynoTask");
        workflowTask.setDynamicTaskNameParam("dynamicTaskName");
        TaskDef taskDef = new TaskDef();
        taskDef.setName("DynoTask");
        workflowTask.setTaskDefinition(taskDef);

        Map<String, Object> taskInput = new HashMap<>();
        taskInput.put("dynamicTaskName", "DynoTask");

        when(parametersUtils.getTaskInput(anyMap(), any(Workflow.class), any(TaskDef.class), anyString())).thenReturn(taskInput);

        String taskId = IDGenerator.generate();

        Workflow workflow = new Workflow();
        WorkflowDef workflowDef = new WorkflowDef();
        workflow.setWorkflowDefinition(workflowDef);

        TaskMapperContext taskMapperContext = TaskMapperContext.newBuilder()
                .withWorkflowInstance(workflow)
                .withWorkflowDefinition(workflowDef)
                .withTaskDefinition(workflowTask.getTaskDefinition())
                .withTaskToSchedule(workflowTask)
                .withTaskInput(taskInput)
                .withRetryCount(0)
                .withTaskId(taskId)
                .build();

        List<Task> mappedTasks = dynamicTaskMapper.getMappedTasks(taskMapperContext);

        assertEquals(1, mappedTasks.size());

        Task dynamicTask = mappedTasks.get(0);
        assertEquals(taskId, dynamicTask.getTaskId());
    }

    @Test
    public void getDynamicTaskName() throws Exception {
        Map<String, Object> taskInput = new HashMap<>();
        taskInput.put("dynamicTaskName", "DynoTask");

        String dynamicTaskName = dynamicTaskMapper.getDynamicTaskName(taskInput, "dynamicTaskName");

        assertEquals("DynoTask", dynamicTaskName);
    }

    @Test
    public void getDynamicTaskNameNotAvailable() throws Exception {
        Map<String, Object> taskInput = new HashMap<>();

        expectedException.expect(TerminateWorkflowException.class);
        expectedException.expectMessage(String.format("Cannot map a dynamic task based on the parameter and input. " +
                "Parameter= %s, input= %s", "dynamicTaskName", taskInput));

        dynamicTaskMapper.getDynamicTaskName(taskInput, "dynamicTaskName");

    }

    @Test
    public void getDynamicTaskDefinition() throws Exception {
        //Given
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName("Foo");
        TaskDef taskDef = new TaskDef();
        taskDef.setName("Foo");
        workflowTask.setTaskDefinition(taskDef);

        //when
        TaskDef dynamicTaskDefinition = dynamicTaskMapper.getDynamicTaskDefinition(workflowTask);

        assertEquals(dynamicTaskDefinition, taskDef);
    }

    @Test
    public void getDynamicTaskDefinitionNull() {

        //Given
        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.setName("Foo");

        expectedException.expect(TerminateWorkflowException.class);
        expectedException.expectMessage(String.format("Invalid task specified.  Cannot find task by name %s in the task definitions",
                workflowTask.getName()));

        dynamicTaskMapper.getDynamicTaskDefinition(workflowTask);

    }

}
