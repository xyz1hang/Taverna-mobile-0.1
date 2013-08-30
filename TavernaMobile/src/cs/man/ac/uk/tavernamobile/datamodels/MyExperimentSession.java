package cs.man.ac.uk.tavernamobile.datamodels;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "session")
public class MyExperimentSession {

	@Element
	protected String username;
	
	@Element
	protected String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
