/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.specific;

import java.util.List;



/**
 * @className GraphicMiss.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 19. 오전 10:34:58
 */

public class GraphicMiss {

	String option;
	List<OptionFilter> filter;
	List<OptionRelation> retaion;
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
	public List<OptionTolerance> getTolerance() {
		return tolerance;
	}
	public void setTolerance(List<OptionTolerance> tolerance) {
		this.tolerance = tolerance;
	}
	
}
