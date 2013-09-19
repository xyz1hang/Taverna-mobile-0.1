package cs.man.ac.uk.tavernamobile.datamodels;

import java.util.ArrayList;

/**
 * Class representation of output values.
 * Values here are used for display purpose
 * hence only file (image) or string type supported
 * at the moment and only one of them can be not-null.
 * 
 * Only file path is stored in order to 
 * avoid passing File object around
 * 
 * the errorValue refer to message of exceptions
 * happened in the application. Workflow Run error
 * should be saved as stringValue
 * 
 * @author Hyde
 */
public class OutputValue {
	
	private String fileValue;
	private String stringValue;
	private ArrayList<OutputValue> listValue;
	private String errorValue;
	private String valueType;
	
	public String getFileValue() {
		return fileValue;
	}
	public void setFileValue(String fileValue) {
		this.fileValue = fileValue;
	}
	public String getStringValue() {
		return stringValue;
	}
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	public String getValueType() {
		return valueType;
	}
	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
	public ArrayList<OutputValue> getListValue() {
		return listValue;
	}
	public void setListValue(ArrayList<OutputValue> listValue) {
		this.listValue = listValue;
	}
	public String getErrorValue() {
		return errorValue;
	}
	public void setErrorValue(String errorValue) {
		this.errorValue = errorValue;
	}
	
	public boolean hasFileValue(){
		return fileValue == null ? false : true;
	}
	
	public boolean hasStringValue(){
		return stringValue == null ? false : true;
	}
	
	public boolean hasErrorValue(){
		return errorValue == null ? false : true;
	}
	
	public boolean hasListValue(){
		return listValue == null ? false : true;
	}
}
