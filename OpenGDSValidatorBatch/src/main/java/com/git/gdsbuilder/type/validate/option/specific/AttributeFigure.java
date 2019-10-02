/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.specific;

import java.util.List;

/**
 * @className AttributeFigure.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 16. 오후 5:01:12
 */

public class AttributeFigure {

	String key;
	List<Object> values;
	Double number;
	String condition;
	Double interval;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public List<Object> getValues() {
		return values;
	}
	public void setValues(List<Object> values) {
		this.values = values;
	}
	public Double getNumber() {
		return number;
	}
	public void setNumber(Double number) {
		this.number = number;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public Double getInterval() {
		return interval;
	}
	public void setInterval(Double interval) {
		this.interval = interval;
	}
	
	
}
