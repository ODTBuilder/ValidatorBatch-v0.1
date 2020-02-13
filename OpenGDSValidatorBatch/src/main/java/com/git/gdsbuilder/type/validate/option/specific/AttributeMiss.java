package com.git.gdsbuilder.type.validate.option.specific;

import java.util.List;

/**
 * @className GeometryMiss.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 14. 오전 11:17:42
 */

public class AttributeMiss {

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

	/**
	 * layerID에 해당하는 {@link com.git.gdsbuilder.type.validate.option.OptionFilter} 반환
	 * 
	 * @param layerID layerID
	 * @return OptionFilter layerID에 해당하는
	 *         {@link com.git.gdsbuilder.type.validate.option.OptionFilter}
	 * 
	 * @author DY.Oh
	 */
	public OptionFilter getLayerFilter(String layerID) {

		if (filter != null) {
			for (OptionFilter layerFilter : filter) {
				String code = layerFilter.getCode();
				if (layerID.equals(code)) {
					return layerFilter;
				}
			}
		}
		return null;
	}

	/**
	 * layerID에 해당하는 {@link com.git.gdsbuilder.type.validate.option.OptionFigure} 반환
	 * 
	 * @param layerID layerID
	 * @return OptionFigure layerID에 해당하는
	 *         {@link com.git.gdsbuilder.type.validate.option.OptionFigure}
	 * 
	 * @author DY.Oh
	 */
	public OptionFigure getLayerFigure(String layerID) {

		if (figure != null) {
			for (OptionFigure layerFigure : figure) {
				String code = layerFigure.getCode();
				if (layerID.equals(code)) {
					return layerFigure;
				}
			}
		}
		return null;
	}

	/**
	 * layerID에 해당하는 {@link com.git.gdsbuilder.type.validate.option.OptionTolerance}
	 * 반환
	 * 
	 * @param layerID layerID
	 * @return OptionTolerance layerID에 해당하는
	 *         {@link com.git.gdsbuilder.type.validate.option.OptionTolerance}
	 * 
	 * @author DY.Oh
	 */
	public OptionTolerance getLayerTolerance(String layerID) {

		if (tolerance != null) {
			for (OptionTolerance layerTolerance : tolerance) {
				String code = layerTolerance.getCode();
				if (code == null || layerID.equals(code)) {
					return layerTolerance;
				}
			}
		}
		return null;
	}

}
