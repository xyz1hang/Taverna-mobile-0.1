package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "workflow")
public class WorkflowBrief extends ElementBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2572602124236017423L;
	
	@Text
	protected String value;
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
