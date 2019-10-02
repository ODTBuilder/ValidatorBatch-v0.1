/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.specific;

import java.util.List;



/**
 * @className OptionFilgure.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 16. 오후 3:38:51
 */


public class OptionFigure {

	String name;
	String code;
	List<AttributeFigure> figure;
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
	public List<AttributeFigure> getFigure() {
		return figure;
	}
	public void setFigure(List<AttributeFigure> figure) {
		this.figure = figure;
	}

}
