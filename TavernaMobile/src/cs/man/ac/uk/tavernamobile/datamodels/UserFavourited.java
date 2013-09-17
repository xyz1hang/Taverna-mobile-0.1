package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

@Root(name = "favourited")
public class UserFavourited implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5773371568262548937L;
	
	@ElementList(required = false)
	@ElementListUnion({
		  @ElementList(entry = "workflow", inline = true, type = WorkflowBrief.class, required = false),
		  @ElementList(entry = "file", inline = true, type = FileBrief.class, required = false),
		  @ElementList(entry = "pack", inline = true, type = PackBrief.class, required = false)
	})
	protected List<ElementBase> favourited;

	public List<ElementBase> getFavouritedEntity() {
		return favourited;
	}

	public void setFavouritedEntity(List<ElementBase> favourited) {
		this.favourited = favourited;
	}
}
