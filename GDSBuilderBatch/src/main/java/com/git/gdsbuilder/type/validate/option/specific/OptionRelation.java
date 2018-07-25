/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.specific;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className OptionRelation.java
 * @description
 * @author DY.Oh
 * @date 2018. 3. 14. 오후 1:40:54
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionRelation {

	String name;
	List<OptionFilter> filters;
	List<OptionFigure> figures;
	List<OptionTolerance> tolerances;

	public OptionFilter getFilter(String code) {
		for (OptionFilter filter : filters) {
			if (filter.getCode().equals(code)) {
				return filter;
			}
		}
		return null;
	}

	public OptionFigure getFigure(String code) {
		for (OptionFigure figure : figures) {
			if (figure.getCode().equals(code)) {
				return figure;
			}
		}
		return null;
	}
}
