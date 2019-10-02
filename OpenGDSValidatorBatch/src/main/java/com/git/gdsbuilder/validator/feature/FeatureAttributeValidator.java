/*
 *    OpenGDS/Builder
 *    http://git.co.kr
 *
 *    (C) 2014-2017, GeoSpatial Information Technology(GIT)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 3 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2012, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package com.git.gdsbuilder.validator.feature;

import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;

import com.git.gdsbuilder.type.dt.feature.DTFeature;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.validate.error.ErrorFeature;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.standard.FixedValue;

/**
 * @className FeatureAttributeValidator.java
 * @description DTFeature의 속성을 검수하는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 2:33:43
 */
public interface FeatureAttributeValidator {

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:34:46
	 * @param DTFeature
	 * @param attributeKey
	 * @return ErrorFeature
	 * @decription ZvalueAmbiguous 검수 수행
	 */
	ErrorFeature validateZvalueAmbiguous(DTFeature feature, OptionFigure figure);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:34:47
	 * @param DTFeature
	 * @param relationDTFeature
	 * @param figure
	 * @param reFigure
	 * @return ErrorFeature
	 * @decription BridgeNameMiss 검수 수행
	 */
	ErrorFeature validateBridgeNameMiss(DTFeature feature, DTFeature relationDTFeature, OptionFigure figure,
			OptionFigure reFigure);

	/**
	 * @author DY.Oh
	 * @param nameFigure
	 * @since 2018. 1. 30. 오후 2:34:49
	 * @param DTFeature
	 * @return ErrorFeature
	 * @decription AdminBoundaryMiss 검수 수행
	 */
	ErrorFeature validateAdminBoundaryMiss(DTFeature feature, OptionFigure nameFigure);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:34:55
	 * @param DTFeature
	 * @param attribute
	 * @param condition
	 * @param figure
	 * @return ErrorFeature
	 * @decription NumericalValue 검수 수행
	 */
	ErrorFeature validateNumericalValue(DTFeature feature, OptionFigure figure);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:35:07
	 * @param DTFeatureI
	 * @param DTFeatureJ
	 * @return ErrorFeature
	 * @decription EntityDuplicated 검수 수행
	 */
	ErrorFeature validateEntityDuplicated(DTFeature featureI, DTFeature featureJ);

	/**
	 * @author DY.Oh
	 * @param attrFigure
	 * @param reAttrFigure
	 * @since 2018. 2. 28. 오전 9:44:54
	 * @param DTFeature
	 * @param lineAvrgKey
	 * @param relationSfc
	 * @param dphAvrgKey
	 *            void
	 * @decription
	 */
	ErrorFeature validateUAvrgDPH20(DTFeature feature, List<AttributeFigure> attrFigures, DTLayer relationLayer,
			List<AttributeFigure> reAttrFigures);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 6. 오후 2:13:16
	 * @param DTFeature
	 * @param fixedValue
	 * @return ErrorFeature
	 * @decription
	 */
	ErrorFeature validateLayerFixMiss(DTFeature feature, List<FixedValue> fixedValue);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 20. 오후 1:54:43
	 * @param feature
	 * @param attrFigure
	 * @param reFeature
	 * @return ErrorFeature
	 * @decription
	 */
	ErrorFeature validateUSymbolDirection(DTFeature feature, List<AttributeFigure> attrFigures, DTFeature reFeature);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 21. 오후 3:20:30
	 * @param feature
	 * @param figures
	 * @return ErrorFeature
	 * @decription
	 */
	ErrorFeature validateFcodeLogicalAttribute(DTFeature feature, OptionFigure figure);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 21. 오후 5:28:33
	 * @param feature
	 * @param figure
	 * @return ErrorFeature
	 * @decription
	 */
	ErrorFeature validateFLabelLogicalAttribute(DTFeature feature, OptionFigure figure);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 22. 오후 11:09:58
	 * @param feature
	 * @param collectionName
	 * @param layerName
	 * @param figure
	 * @param relationLayer
	 * @return ErrorFeature
	 * @decription
	 */
	ErrorFeature validateUFIDMiss(DTFeature feature, String collectionName, String layerName, OptionFigure figure,
			DTLayer relationLayer);

	/**
	 * @author hochul.kim
	 * @since 2018. 3. 27.
	 * @param feature
	 * @param figure
	 * @return ErrorFeature
	 * @decription
	 */
	List<ErrorFeature> validateDissolve(DTFeature feature, SimpleFeatureCollection selfSfc, OptionFigure figure);
}
