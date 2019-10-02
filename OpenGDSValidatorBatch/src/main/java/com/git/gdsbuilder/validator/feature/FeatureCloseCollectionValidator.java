package com.git.gdsbuilder.validator.feature;

import com.git.gdsbuilder.type.dt.feature.DTFeature;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.validate.error.ErrorFeature;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @className FeatureCloseCollectionValidator.java
 * @description 인접 SimpleFeature를 검수하는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 2:33:56
 */
public interface FeatureCloseCollectionValidator {

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:34:12
	 * @param feature
	 * @param closeLayer
	 * @param closeBoundary
	 * @param tolerance
	 * @return List<ErrorFeature>
	 * @decription RefEntityNone 검수 수행
	 */
	public ErrorFeature validateFRefEntityNone(DTFeature feature, DTLayer closeLayer, Geometry closeBoundary,
			OptionTolerance tolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:34:12
	 * @param feature
	 * @param closeLayer
	 * @param closeBoundary
	 * @param tolerance
	 * @return List<ErrorFeature>
	 * @decription RefEntityNone 검수 수행
	 */
	public ErrorFeature validateDRefEntityNone(DTFeature feature, DTLayer closeLayer, Geometry closeBoundary,
			OptionTolerance tolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:34:13
	 * @param feature
	 * @param closeLayer
	 * @param closeBoundary
	 * @param tolerance
	 * @param figure
	 * @return ErrorFeature
	 * @decription RefAttributeMiss 검수 수행
	 */
	public ErrorFeature validateRefAttributeMiss(DTFeature feature, DTLayer closeLayer, Geometry closeBoundary,
			OptionTolerance tolerance, OptionFigure figure);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:34:14
	 * @param feature
	 * @param closeLayer
	 * @param closeBoundary
	 * @param tolerance
	 * @param figure
	 * @return ErrorFeature
	 * @decription RefZValueMiss 검수 수행
	 */
	public ErrorFeature validateRefZValueMiss(DTFeature feature, DTLayer closeLayer, Geometry closeBoundary,
			OptionTolerance tolerance, OptionFigure figure);
}
