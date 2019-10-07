/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.specific;

import java.util.List;

/**
 * @className OptionCondition.java
 * @description
 * @author DY.Oh
 * @date 2018. 3. 14. 오후 1:40:46
 */

public class OptionFilter {

	String name;
	String code;
	List<AttributeFilter> filter;

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

	public List<AttributeFilter> getFilter() {
		return filter;
	}

	public void setFilter(List<AttributeFilter> filter) {
		this.filter = filter;
	}

}
