/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.specific;

import java.util.List;


/**
 * @className AttributeCondition.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 14. 오후 1:10:47
 */

public class AttributeFilter {

	// 해당값들만 검수
	String key;
	List<Object> values;
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
	@Override
	public String toString() {
		return "AttributeFilter [key=" + key + ", values=" + values + "]";
	}
	
	

}
