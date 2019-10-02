/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.specific;

import java.util.List;



/**
 * @className CloseMiss.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 22. 오전 9:53:10
 */


public class CloseMiss {

	String option;
	List<OptionFilter> filter;
	List<OptionRelation> retaion;
	List<OptionFigure> figure;
	List<OptionTolerance> tolerance;
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
	public List<OptionFilter> getFilter() {
		return filter;
	}
	public void setFilter(List<OptionFilter> filter) {
		this.filter = filter;
	}
	public List<OptionRelation> getRetaion() {
		return retaion;
	}
	public void setRetaion(List<OptionRelation> retaion) {
		this.retaion = retaion;
	}
	public List<OptionFigure> getFigure() {
		return figure;
	}
	public void setFigure(List<OptionFigure> figure) {
		this.figure = figure;
	}
	public List<OptionTolerance> getTolerance() {
		return tolerance;
	}
	public void setTolerance(List<OptionTolerance> tolerance) {
		this.tolerance = tolerance;
	}

}
