/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.specific;

import java.util.List;

/**
 * @className OptionRelation.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 14. 오후 1:40:54
 */

public class OptionRelation {

	String name;
	List<OptionFilter> filters;
	List<OptionFigure> figures;
	List<OptionTolerance> tolerances;

	public OptionFilter getFilter(String code) {

		if (filters != null) {
			for (OptionFilter filter : filters) {
				if (filter.getCode().equals(code)) {
					return filter;
				}
			}
		}
		return null;
	}

	public OptionFigure getFigure(String code) {
		if (figures != null) {
			for (OptionFigure figure : figures) {
				if (figure.getCode().equals(code)) {
					return figure;
				}
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<OptionFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<OptionFilter> filters) {
		this.filters = filters;
	}

	public List<OptionFigure> getFigures() {
		return figures;
	}

	public void setFigures(List<OptionFigure> figures) {
		this.figures = figures;
	}

	public List<OptionTolerance> getTolerances() {
		return tolerances;
	}

	public void setTolerances(List<OptionTolerance> tolerances) {
		this.tolerances = tolerances;
	}

}
