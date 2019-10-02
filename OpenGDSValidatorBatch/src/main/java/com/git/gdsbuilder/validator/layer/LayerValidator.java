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

package com.git.gdsbuilder.validator.layer;

import java.io.IOException;
import java.util.List;

import org.geotools.feature.SchemaException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.DTLayerList;
import com.git.gdsbuilder.type.validate.error.ErrorLayer;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.git.gdsbuilder.type.validate.option.standard.FixedValue;
import com.vividsolutions.jts.geom.Geometry;

/**
 * DTLayer를 검수하는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 4. 18. 오후 3:32:00
 */
public interface LayerValidator {

	// 수치지도 검수
	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription ConBreak 검수 수행
	 */
	public ErrorLayer validateConBreak(DTLayerList relationLayers, OptionTolerance tole) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription ConIntersected 검수 수행
	 */
	public ErrorLayer validateConIntersected() throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription ConOverDegree 검수 수행
	 */
	public ErrorLayer validateConOverDegree(OptionTolerance tole) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription ZValueAmbiguous 검수 수행
	 */
	public ErrorLayer validateZValueAmbiguous(OptionFigure figure) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription UselessPoint 검수 수행
	 */
	public ErrorLayer validateUselessPoint()
			throws NoSuchAuthorityCodeException, SchemaException, FactoryException, TransformException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription SmallArea 검수 수행
	 */
	public ErrorLayer validateSmallArea(OptionTolerance tole) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription SmallLength 검수 수행
	 */
	public ErrorLayer validateSmallLength(OptionTolerance tole) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:14:45
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription OverShoot 검수 수행
	 */
	public ErrorLayer validateOverShoot(DTLayerList relationLayers, OptionTolerance tole) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @throws IOException
	 * @decription SelfEntity 검수 수행
	 */
	public ErrorLayer validateSelfEntity(DTLayerList relationLayers, OptionTolerance tolerance)
			throws SchemaException, IOException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription OutBoundary 검수 수행
	 */
	public ErrorLayer validateOutBoundary(DTLayerList relationLayers, OptionTolerance tole) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription EntityDuplicated 검수 수행
	 */
	public ErrorLayer validateEntityDuplicated() throws SchemaException;

	// /**
	// * @author DY.Oh
	// * @since 2018. 1. 30. 오후 3:13:23
	// * @param neatLayer
	// * @param tolerence
	// * @return ErrorLayer
	// * @throws SchemaException
	// * @decription UselessEntity 검수 수행
	// */
	// public ErrorLayer validateUselessEntity() throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription EntityOpenMiss 검수 수행
	 */
	public ErrorLayer validateEntityOpenMiss(DTLayerList relationLayers, OptionTolerance tole) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @param reFigures
	 * @param figure
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription BridgeNameMiss 검수 수행
	 */
	public ErrorLayer validateBridgeNameMiss(OptionFigure figure, DTLayerList relationLayers,
			List<OptionFigure> reFigures) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @param figure
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription AdminBoundaryMiss 검수 수행
	 */
	public ErrorLayer validateAdminBoundaryMiss(OptionFigure figure) throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription TwistedPolygon 검수 수행
	 */
	public ErrorLayer validateTwistedPolygon() throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription NodeMiss 검수 수행
	 */
	public ErrorLayer validateNodeMiss(DTLayerList relationLayers, String geomColumn, OptionTolerance tole)
			throws SchemaException, IOException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription PointDuplicated 검수 수행
	 */
	public ErrorLayer validatePointDuplicated();

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription OneAcre 검수 수행
	 */
	public ErrorLayer validateOneAcre(DTLayerList typeLayers, double spatialAccuracyTolorence);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription OneStage 검수 수행
	 */
	public ErrorLayer validateOneStage(DTLayerList typeLayers);

	/**
	 * @author DY.Oh
	 * @param figure
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription UFIDMiss 검수 수행
	 */
	public ErrorLayer validateUFIDMiss(String collectionName, String layerName, OptionFigure figure,
			DTLayerList relationLayers);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription BuildingSiteMiss 검수 수행
	 */
	public ErrorLayer validateBuildingSiteMiss(DTLayerList relationName);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription BoundaryMiss 검수 수행
	 */
	public ErrorLayer validateBoundaryMiss(DTLayerList relationLayers);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription CenterLineMiss 검수 수행
	 */
	public ErrorLayer validateCenterLineMiss(DTLayerList relationLayers);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription HoleMisplacement 검수 수행
	 */
	public ErrorLayer validateHoleMissplacement();

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription EntityInHole 검수 수행
	 */
	public ErrorLayer validateEntityInHole(DTLayerList relationLayers);

	/**
	 * @author DY.Oh
	 * @param tole
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription LinearDisconnection 검수 수행
	 */
	public ErrorLayer valildateLinearDisconnection(DTLayerList relationLayers, OptionTolerance tole)
			throws SchemaException;

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription LinearDisconnection 검수 수행
	 */
	public ErrorLayer validateMultiPart();

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription RefEntityNone 검수 수행
	 */
	public ErrorLayer validateFRefEntityNone(DTLayer closeLayer, Geometry closeBoundary, OptionTolerance tolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription RefEntityNone 검수 수행
	 */
	public ErrorLayer validateDRefEntityNone(DTLayer closeLayer, Geometry closeBoundary, OptionTolerance tolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription RefAttributeMiss 검수 수행
	 */
	public ErrorLayer validateRefAttributeMiss(DTLayer closeLayer, Geometry closeBoundary, OptionFigure figure,
			OptionTolerance tolerance);

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 3:13:23
	 * @param neatLayer
	 * @param tolerence
	 * @return ErrorLayer
	 * @throws SchemaException
	 * @decription RefZValueMiss 검수 수행
	 */
	public ErrorLayer validateRefZValueMiss(DTLayer closeLayer, Geometry closeBoundary, OptionFigure figure,
			OptionTolerance tolerance);

	// 지하시설물 검수
	public ErrorLayer validateUNodeMiss(DTLayerList relationLayers);

	public ErrorLayer validateUAvrgDPH20(List<OptionFigure> figures, DTLayerList relationLayers,
			List<OptionFigure> reFigure);

	public ErrorLayer validateULeaderline(DTLayerList relationLayers) throws SchemaException;

	public ErrorLayer validateUAvrgDPH10(DTLayerList relationLayers, List<OptionTolerance> reTolerances);

	public ErrorLayer validateUSymbolDirection(List<OptionFigure> figures, DTLayerList relationLayers,
			List<OptionFigure> reFigures);

	/**
	 * @author hochul.Kim
	 * @since 2018. 2. 28.
	 * @param DTLayerList
	 * @return ErrorLayer
	 * @decription 심볼 단독 존재 오류
	 */
	public ErrorLayer validateUSymbolOut(DTLayerList lines);

	/**
	 * @author hochul.Kim
	 * @param reTolerances
	 * @since 2018. 3. 6.
	 * @param DTLayerList
	 * @return ErrorLayer
	 * @decription 라인 버텍스 내 심볼 미존재 오류
	 */
	public ErrorLayer validateSymbolInLine(DTLayerList points, List<OptionTolerance> reTolerances);

	/**
	 * @author hochul.Kim
	 * @since 2018. 3. 6.
	 * @return ErrorLayer
	 * @decription 라인 버텍스(심볼)간의 거리 오류
	 */
	public ErrorLayer validateSymbolsDistance();

	/**
	 * @author hochul.Kim
	 * @since 2018. 3. 6.
	 * @param DTLayerList
	 * @return ErrorLayer
	 * @decription 라인 간의 교차 지점 버텍스 미존재 오류
	 */
	public ErrorLayer validateLineCross();

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 15. 오후 5:02:17
	 * @param geometry
	 * @param fixedValue
	 * @return ErrorLayer
	 * @decription
	 */
	public ErrorLayer validateLayerFixMiss(String geometry, List<FixedValue> fixedValue);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 21. 오후 2:30:17
	 * @param figures
	 * @return ErrorLayer
	 * @decription
	 */
	public ErrorLayer validateFFcodeLogicalAttribute(List<OptionFigure> figures);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 21. 오후 5:28:01
	 * @param figures
	 * @return ErrorLayer
	 * @decription
	 */
	public ErrorLayer validateFLabelLogicalAttribute(List<OptionFigure> figures);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 22. 오후 10:48:11
	 * @param figure
	 * @return ErrorLayer
	 * @decription
	 */
	public ErrorLayer validateNumericalValue(OptionFigure figure);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 23. 오전 10:56:22
	 * @param tole
	 * @return ErrorLayer
	 * @decription
	 */
	public ErrorLayer validateFEntityInHole();

	/**
	 * @author hochul.Kim
	 * @since 2018. 3. 27.
	 * @param attrList
	 * @return ErrorLayer
	 * @decription 임상도 인접 속성 오류
	 */
	public ErrorLayer validateDissolve(List<OptionFigure> figures);

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 30. 오후 1:43:12
	 * @param figures
	 * @param closeLayer
	 * @return ErrorLayer
	 * @decription
	 */
	public ErrorLayer validateDissolve(List<OptionFigure> figures, DTLayer closeLayer);

	/**
	 * @author DY.Oh
	 * @since 2018. 4. 5. 오전 11:56:38
	 * @param tolerance
	 * @return
	 * @throws SchemaException
	 * @throws IOException
	 *             ErrorLayer
	 * @decription
	 */
	ErrorLayer validateSelfEntity(OptionTolerance tolerance) throws SchemaException, IOException;

	/**
	 * @author DY.Oh
	 * @since 2018. 4. 10. 오전 11:18:17
	 * @param relationLayers
	 * @return ErrorLayer
	 * @decription
	 */
	public ErrorLayer validateSymbolOut(DTLayerList relationLayers);
}
