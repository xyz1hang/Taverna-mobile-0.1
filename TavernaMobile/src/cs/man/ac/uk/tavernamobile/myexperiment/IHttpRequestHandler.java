package cs.man.ac.uk.tavernamobile.myexperiment;

public interface IHttpRequestHandler {

	// handle GET request
	public abstract <T> Object Get(String uri, Class<T> classType);

}