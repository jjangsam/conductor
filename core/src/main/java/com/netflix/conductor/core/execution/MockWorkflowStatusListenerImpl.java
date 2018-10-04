package com.netflix.conductor.core.execution;

import com.netflix.conductor.common.run.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy listener to be used in other places
 */
public class MockWorkflowStatusListenerImpl implements WorkflowStatusListener {

    private static final Logger LOG = LoggerFactory.getLogger(MockWorkflowStatusListenerImpl.class);

    @Override
    public void onWorkflowCompleted(Workflow workflow) {
        LOG.debug("Workflow {} is completed", workflow.toString());
    }

    @Override
    public void onWorkflowTerminated(Workflow workflow) {
        LOG.debug("Workflow {} is terminated", workflow.toString());
    }

    @Override
    public void onWorkflowPaused(Workflow workflow) {
        LOG.debug("Workflow {} is paused", workflow.toString());

    }
}
