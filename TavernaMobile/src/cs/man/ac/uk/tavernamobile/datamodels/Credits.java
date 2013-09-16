package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

@Root(name = "credits")
public class Credits implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2295201264230798132L;
	
	@ElementList(required = false)
	@ElementListUnion({
		  @ElementList(entry = "user", inline = true, type = CreditUser.class, required = false),
		  @ElementList(entry = "group", inline = true, type = CreditGroup.class, required = false)
	})
	protected List<ElementBase> credits;

	public List<ElementBase> getCreditEntity() {
		return credits;
	}

	public void setCreditEntity(List<ElementBase> credits) {
		this.credits = credits;
	}
}
