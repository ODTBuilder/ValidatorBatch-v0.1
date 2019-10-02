/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.standard;

import java.util.List;


/**
 * @className LayerFix.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 14. 오전 11:17:05
 */

public class LayerFixMiss {

	String option;
	String code;
	String geometry;
	List<FixedValue> fix;
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getGeometry() {
		return geometry;
	}
	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}
	public List<FixedValue> getFix() {
		return fix;
	}
	public void setFix(List<FixedValue> fix) {
		this.fix = fix;
	}

}
