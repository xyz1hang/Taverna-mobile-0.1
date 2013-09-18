package cs.man.ac.uk.tavernamobile.datamodels;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "workflows")
public class WorkflowBriefCollection {

	@ElementList(inline = true, required = false, entry = "workflow")
	protected List<WorkflowBrief> workflowBrief;

	public List<WorkflowBrief> getWorkflowBrief() {
		return workflowBrief;
	}

	public void setWorkflowBrief(List<WorkflowBrief> workflowBrief) {
		this.workflowBrief = workflowBrief;
	}
	
}
