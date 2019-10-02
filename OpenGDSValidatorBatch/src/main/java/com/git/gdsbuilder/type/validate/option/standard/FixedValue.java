/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.standard;

import java.util.List;



/**
 * @className FixedValue.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 14. 오후 1:42:56
 */


public class FixedValue {

	String name;
	String type;
	boolean isnull;
	Long length;
	List<Object> values;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isIsnull() {
		return isnull;
	}
	public void setIsnull(boolean isnull) {
		this.isnull = isnull;
	}
	public Long getLength() {
		return length;
	}
	public void setLength(Long length) {
		this.length = length;
	}
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}

}
