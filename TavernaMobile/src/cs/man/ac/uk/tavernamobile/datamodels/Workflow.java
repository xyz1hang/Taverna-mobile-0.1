package cs.man.ac.uk.tavernamobile.datamodels;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "workflow")
public class Workflow extends ElementBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4742092124269485712L;

	@Element(required = false)
	protected String id;
	
	@Element(required = false)
	protected String title;
	
	@Element(required = false)
	protected String description;
	
	@Element(required = false)
	protected WorkflowType type;
	
	@Element(required = false)
	protected WorkflowUploader uploader;
	
	@Element(name = "created-at", required = false)
	protected String created_at;
	
	@Element(required = false)
	protected String preview;
	
	@Element(required = false)
	protected String svg;
	
	@Element(name = "license-type" , required = false)
	protected LicenseType license_type;
	
	@Element(name = "content-uri", required = false)
	protected String content_uri;
	
	@Element(name = "content-type", required = false)
	protected String content_type;
	
	@Element(name = "updated-at", required = false)
	protected String updated_at;
	
	@Element(name = "thumbnail-big", required = false)
	protected String thumbnail;
	
	@ElementList(required = false)
	protected List<Rating> ratings;
	
	@ElementList(inline = true, required = false, entry = "user")
	protected List<Credit> credits;
	
	@ElementList(required = false)
	protected List<Tag> tags;
	
	@ElementList(required = false)
	protected List<Privilege> privileges;

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public WorkflowType getType() {
		return type;
	}

	public WorkflowUploader getUploader() {
		return uploader;
	}

	public String getCreated_at() {
		return created_at;
	}

	public String getPreview() {
		return preview;
	}

	public String getSvg() {
		return svg;
	}

	public LicenseType getLicense_type() {
		return license_type;
	}

	public String getContent_uri() {
		return content_uri;
	}

	public String getContent_type() {
		return content_type;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setType(WorkflowType type) {
		this.type = type;
	}

	public void setUploader(WorkflowUploader uploader) {
		this.uploader = uploader;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}

	public void setSvg(String svg) {
		this.svg = svg;
	}

	public void setLicense_type(LicenseType license_type) {
		this.license_type = license_type;
	}

	public void setContent_uri(String content_uri) {
		this.content_uri = content_uri;
	}

	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public List<Rating> getRatings() {
		return ratings;
	}

	public void setRatings(List<Rating> ratings) {
		this.ratings = ratings;
	}

	public List<Credit> getCredits() {
		return credits;
	}

	public void setCredits(List<Credit> credits) {
		this.credits = credits;
	}

	public List<Privilege> getPrivileges() {
		return privileges;
	}

	public void setPrivileges(List<Privilege> privileges) {
		this.privileges = privileges;
	}
}
