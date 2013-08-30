package cs.man.ac.uk.tavernamobile.datamodels;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "search")
public class WorkflowSearchResults {
	
	@Attribute
	protected String type;
	
	@Attribute
	protected String query;
	
	@ElementList(inline = true, required = false, entry = "workflow")
	protected List<Workflow> workflows;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<Workflow> getWorkflows() {
		return workflows;
	}

	public void setWorkflows(List<Workflow> workflows) {
		this.workflows = workflows;
	}
}
