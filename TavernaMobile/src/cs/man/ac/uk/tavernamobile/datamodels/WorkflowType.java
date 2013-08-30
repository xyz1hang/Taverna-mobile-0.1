package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.Serializable;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "type")
public class WorkflowType extends ElementBase implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3897479048489334822L;
	
	@Text
	protected String value;

	public String getValue() {
		return value;
	}
}
