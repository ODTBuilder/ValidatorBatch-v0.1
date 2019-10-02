/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.specific;



/**
 * @className OptionTolerance.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 14. 오후 1:41:08
 */


public class OptionTolerance {

	String name;
	String code;
	Double value;
	String condition;
	Double interval;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
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
