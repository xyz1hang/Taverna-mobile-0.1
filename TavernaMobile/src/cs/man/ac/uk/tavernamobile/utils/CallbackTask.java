package cs.man.ac.uk.tavernamobile.utils;

/**
 * Listeners Interface.
 * 
 * Interface that help with program logic delegation
 * between threads, classes etc.
 * 
 * @author Hyde
 *
 */
public interface CallbackTask {
	
	Object onTaskInProgress(Object... param);
	
	Object onTaskComplete(Object... result);
}
