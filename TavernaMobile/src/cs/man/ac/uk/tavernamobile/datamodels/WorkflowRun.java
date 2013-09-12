package cs.man.ac.uk.tavernamobile.datamodels;

/**
 * Class represent Data that used to display for workflow runs
 * retrieved from server
 * (More fields can be added when necessary) 
 * 
 * @author Hyde Zhang
 */
public class WorkflowRun extends WorkflowBE{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7352709238890676098L;
	private String runState;
	private String startTime;
	private String endTime;

	public String getRunState() {
		return runState;
	}
	public void setRunState(String runState) {
		this.runState = runState;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
}
