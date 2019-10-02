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
import org.opengis.feature.simple.SimpleFeature;

import com.git.gdsbuilder.type.dt.feature.DTFeature;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.DTLayerList;
import com.git.gdsbuilder.type.validate.error.ErrorFeature;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;

/**
 * SimpleFeature의 그래픽 검수하는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 4. 18. 오후 3:34:07
 */
public interface FeatureGraphicValidator {

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:36:24
	 * @param simpleFeature
	 * @param neatLineSfc
	 * @param tolerence
	 * @return List<ErrorFeature>
	 * @decription ConBreak 검수 수행
	 */
	List<ErrorFeature> validateConBreak(DTFeature simpleFeature, DTLayer neatLine, OptionTolerance optionTolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:36:26
	 * @param tmpSimpleFeatureI
	 * @return List<ErrorFeature>
	 * @decription ConIntersected 검수 수행
	 */
	List<ErrorFeature> validateConIntersected(DTFeature tmpSimpleFeatureI);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:36:45
	 * @param tmpSimpleFeatureI
	 * @param tmpSimpleFeatureJ
	 * @return List<ErrorFeature>
	 * @decription ConIntersected 검수 수행
	 */
	List<ErrorFeature> validateConIntersected(DTFeature tmpSimpleFeatureI, DTFeature tmpSimpleFeatureJ);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:36:50
	 * @param simpleFeature
	 * @param degree
	 * @return List<ErrorFeature>
	 * @decription ConOverDegree 검수 수행
	 */
	List<ErrorFeature> validateConOverDegree(DTFeature simpleFeature, OptionTolerance optionTolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:36:57
	 * @param simpleFeature
	 * @return List<ErrorFeature>
	 * @decription UselessPoint 검수 수행
	 */
	List<ErrorFeature> validateUselessPoint(DTFeature simpleFeature);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:37:10
	 * @param feature
	 * @param optionTolerance
	 * @return ErrorFeature
	 * @decription SmallArea 검수 수행
	 */
	ErrorFeature validateSmallArea(DTFeature feature, OptionTolerance optionTolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:37:18
	 * @param simpleFeature
	 * @param length
	 * @return ErrorFeature
	 * @decription SmallLength 검수 수행
	 */
	ErrorFeature validateSmallLength(DTFeature simpleFeature, OptionTolerance optionTolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:37:30
	 * @param simpleFeature
	 * @param neatLineSfc
	 * @param tolerence
	 * @return List<ErrorFeature>
	 * @decription OverShoot 검수 수행
	 */
	List<ErrorFeature> validateOverShoot(DTFeature simpleFeature, DTLayer neatLine, OptionTolerance optionTolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:37:40
	 * @param feature
	 * @param reFeature
	 * @param tolerance
	 * @return List<ErrorFeature>
	 * @decription SelfEntity 검수 수행
	 */
	List<ErrorFeature> validateSelfEntity(DTFeature feature, DTFeature reFeature, OptionTolerance tolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:37:48
	 * @param feature
	 * @param relationLayer
	 * @param tole
	 * @return ErrorFeature
	 * @decription OutBoundary 검수 수행
	 */
	ErrorFeature validateOutBoundary(DTFeature feature, DTLayer relationLayer, OptionTolerance tole);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:37:57
	 * @param tmpSimpleFeatureI
	 * @param tmpSimpleFeatureJ
	 * @return ErrorFeature
	 * @decription EntityDuplicated 검수 수행
	 */
	ErrorFeature validateEntityDuplicated(DTFeature tmpSimpleFeatureI, DTFeature tmpSimpleFeatureJ);

	// /**
	// * @author DY.Oh
	// * @since 2018. 1. 30. 오후 2:38:07
	// * @param simpleFeature
	// * @return ErrorFeature
	// * @decription UselessEntity 검수 수행
	// */
	// ErrorFeature validateUselessEntity(DTFeature simpleFeature);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:38:13
	 * @param simpleFeature
	 * @param neatLineSfc
	 * @param tolerence
	 * @return ErrorFeature
	 * @decription EntityOpenMiss 검수 수행
	 */
	ErrorFeature validateEntityOpenMiss(DTFeature simpleFeature, DTLayer relationLayer, OptionTolerance tolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:38:23
	 * @param simpleFeature
	 * @param typeName
	 * @return ErrorFeature
	 * @decription LayerMiss 검수 수행
	 */
	ErrorFeature validateLayerFixMiss(SimpleFeature simpleFeature, String typeName);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:38:30
	 * @param simpleFeature
	 * @return ErrorFeature
	 * @decription TwistedPolygon 검수 수행
	 */
	ErrorFeature validateTwistedPolygon(DTFeature simpleFeature);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:38:38
	 * @param simpleFeature
	 * @param sfc
	 * @param relationSfc
	 * @param tolerence
	 * @return List<ErrorFeature>
	 * @decription NodeMiss 검수 수행
	 */
	List<ErrorFeature> validateNodeMiss(DTFeature simpleFeature, SimpleFeatureCollection sfc, DTLayer relationLayer,
			OptionTolerance tolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:38:47
	 * @param simpleFeature
	 * @return List<ErrorFeature>
	 * @decription PointDuplicated 검수 수행
	 */
	List<ErrorFeature> validatePointDuplicated(DTFeature simpleFeature);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:38:57
	 * @param simpleFeature
	 * @param relationSfc
	 * @return ErrorFeature
	 * @decription OneAcre 검수 수행
	 */
	ErrorFeature validateOneAcre(DTFeature simpleFeature, SimpleFeatureCollection relationSfc);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:39:09
	 * @param feature
	 * @param relationLayer
	 * @return List<ErrorFeature>
	 * @decription OneStage 검수 수행
	 */
	List<ErrorFeature> validateOneStage(DTFeature feature, DTLayer relationLayer);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:39:42
	 * @param simpleFeature
	 * @param relationSfc
	 * @param attributeJson
	 * @return ErrorFeature
	 * @decription BuildingSiteMiss 검수 수행
	 */
	ErrorFeature validateBuildingSiteMiss(DTFeature feature, DTLayerList relationLayers);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:39:40
	 * @param feature
	 * @param relationLayer
	 * @return ErrorFeature
	 * @decription BoundaryMiss 검수 수행
	 */
	ErrorFeature validateBoundaryMiss(DTFeature feature, DTLayer relationLayer);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:39:55
	 * @param feature
	 * @param relationLayer
	 * @return ErrorFeature
	 * @decription CenterLineMiss 검수 수행
	 */
	ErrorFeature validateCenterLineMiss(DTFeature feature, DTLayer relationLayer);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:40:04
	 * @param feature
	 * @return List<ErrorFeature>
	 * @decription HoleMisplacement 검수 수행
	 */
	List<ErrorFeature> validateHoleMissplacement(DTFeature feature);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:40:11
	 * @param feature
	 * @param relationLayer
	 * @param isEquals
	 * @return List<ErrorFeature>
	 * @decription EntityInHole 검수 수행
	 */
	List<ErrorFeature> validateEntityInHole(DTFeature feature, DTLayer relationLayer, boolean isEquals);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:40:19
	 * @param feature
	 * @param relationlayer
	 * @param tole
	 * @return List<ErrorFeature>
	 * @decription LinearDisconnection 검수 수행
	 */
	List<ErrorFeature> validateLinearDisconnection(DTFeature feature, DTLayer relationlayer, OptionTolerance tole);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:40:26
	 * @param simpleFeature
	 * @return List<ErrorFeature>
	 * @decription MultiPart 검수 수행
	 */
	List<ErrorFeature> validateMultiPart(DTFeature feature);

	/**
	 * @author DY.Oh
	 * @since 2018. 2. 28. 오전 9:22:01
	 * @param feature
	 * @param tmpSf
	 * @return ErrorFeature
	 * @decription
	 */
	ErrorFeature validateUNodeMiss(DTFeature feature, DTFeature reFeature);

	/**
	 * @author DY.Oh
	 * @since 2018. 2. 28. 오전 9:48:22
	 * @param feature
	 * @param reFeature
	 * @return List<ErrorFeature>
	 * @decription
	 */
	List<ErrorFeature> validateULeaderLine(DTFeature feature, DTFeature reFeature);

	/**
	 * @author hochul.Kim
	 * @since 2018. 3. 15. 오후 2:41:42
	 * @param feature
	 * @param reTolerance
	 * @return ErrorFeature
	 * @decription
	 */
	ErrorFeature validateUSymbolOut(DTFeature feature, DTLayer relationLayer);

	/**
	 * @author hochul.Kim
	 * @since 2018. 3. 15. 오후 2:41:42
	 * @param feature
	 * @param relationLayers
	 * @param reTolerances
	 * @return ErrorFeature
	 * @decription
	 */
	List<ErrorFeature> validateSymbolInLine(DTFeature feature, DTLayerList relationLayers,
			List<OptionTolerance> reTolerances);

	/**
	 * @author hochul.Kim
	 * @since 2018. 3. 15. 오후 2:41:42
	 * @param feature
	 * @param validatorLayer
	 * @return ErrorFeature
	 * @decription
	 */
	List<ErrorFeature> validateLineCross(DTFeature feature, DTLayer validatorLayer);

	/**
	 * @author hochul.Kim
	 * @since 2018. 3. 19. 오후 5:34:00
	 * @param feature
	 * @return List<ErrorFeature>
	 * @decription
	 */
	List<ErrorFeature> validateSymbolsDistance(DTFeature feature);

	/**
	 * @author DY.Oh
	 * @param textLayer
	 * @since 2018. 2. 28. 오전 10:09:08
	 * @param sf
	 * @param lineSfc
	 * @param textSfc
	 * @return ErrorFeature
	 * @decription
	 */
	ErrorFeature validateUAvrgDPH10(DTFeature feature, DTLayer relationLayerL, DTLayer relationLayerT,
			DTLayer textLayer);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 23. 오전 11:00:17
	 * @param feature
	 * @param validatorLayer
	 * @return List<ErrorFeature>
	 * @decription
	 */
	List<ErrorFeature> validateFEntityInHole(DTFeature feature, DTLayer validatorLayer);

	/**
	 * @author DY.Oh
	 * @since 2018. 4. 10. 오전 11:19:12
	 * @param feature
	 * @param relationLayers
	 * @return ErrorFeature
	 * @decription
	 */
	ErrorFeature validateSymbolOut(DTFeature feature, DTLayerList relationLayers);
}
