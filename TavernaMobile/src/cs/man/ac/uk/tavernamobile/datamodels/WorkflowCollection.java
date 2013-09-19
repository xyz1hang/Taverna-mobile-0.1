package cs.man.ac.uk.tavernamobile.datamodels;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "workflows")
public class WorkflowCollection {
	
	@ElementList(inline = true, required = false, entry = "workflow")
	protected List<Workflow> workflows;

	public List<Workflow> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<Workflow> workflows) {
		this.workflows = workflows;
	}
}
