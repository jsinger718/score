package org.score.samples.controlactions;

import com.hp.score.api.EndBranchDataContainer;
import com.hp.score.lang.ExecutionRuntimeServices;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 14/08/14
 * Time: 10:36
 */
public class BranchActions {

    public static final String STEP_POSITION = "stepPosition";
    public static final String EXECUTION_PLAN_ID = "executionPlanId";

    public void split(ExecutionRuntimeServices executionRuntimeServices, Long stepPosition, String executionPlanId){
        executionRuntimeServices.addBranch(stepPosition, executionPlanId, new HashMap<String, Serializable>());
	}

    public void splitWithContext(ExecutionRuntimeServices executionRuntimeServices,
								 Map<String, Serializable> executionContext,
								 String flowUuid,
								 Map<String, Serializable> context,
								 List<String> inputKeysFromParentContext){
		Map<String, Serializable> initialContext = new HashMap<>();
		initialContext.putAll(context);

		if (inputKeysFromParentContext != null) {
			for (String inputKey : inputKeysFromParentContext) {
				initialContext.put(inputKey, executionContext.get(inputKey));
			}
		}

		executionRuntimeServices.addBranch(0L, flowUuid, initialContext);
    }

    public void join(ExecutionRuntimeServices executionRuntimeServices,Map<String, Serializable> executionContext){
        List<EndBranchDataContainer> branches = executionRuntimeServices.getFinishedChildBranchesData();
        for (EndBranchDataContainer branch : branches) {
            Map<String,Serializable> branchContext  = branch.getContexts();
            executionContext.putAll(branchContext);
        }
    }

    public void parallelSplit(ExecutionRuntimeServices executionRuntimeServices, Long stepPosition, String executionPlanId){
        executionRuntimeServices.addBranch(stepPosition, executionPlanId, new HashMap<String, Serializable>());
        executionRuntimeServices.addBranch(stepPosition, executionPlanId, new HashMap<String, Serializable>());
    }
}
