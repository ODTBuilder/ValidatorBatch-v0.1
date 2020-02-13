package com.git.gdsbuilder.validator.layer;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.git.gdsbuilder.type.dt.feature.DTFeature;
import com.git.gdsbuilder.type.dt.layer.OpenDTLayer;
import com.git.gdsbuilder.type.dt.layer.OpenDTLayerList;
import com.git.gdsbuilder.type.validate.error.ErrorFeature;
import com.git.gdsbuilder.type.validate.error.ErrorLayer;
import com.git.gdsbuilder.type.validate.option.en.LangType;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.git.gdsbuilder.type.validate.option.standard.FixedValue;
import com.git.gdsbuilder.validator.feature.OpenFeatureAttributeValidator;
import com.git.gdsbuilder.validator.feature.OpenFeatureGraphicValidator;
import com.git.gdsbuilder.validator.feature.filter.FeatureFilter;
import com.git.gdsbuilder.validator.quad.Quadtree;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.TopologyException;

public class OpenLayerValidator {

	OpenDTLayer validatorLayer;
	String typeName;
	OpenFeatureAttributeValidator attrValidator;
	OpenFeatureGraphicValidator grapValidator;
	LangType langType;
	String epsg;

	public OpenLayerValidator(OpenDTLayer validatorLayer, LangType langType) {
		this.validatorLayer = validatorLayer;
		this.typeName = validatorLayer.getTypeName();
		this.attrValidator = new OpenFeatureAttributeValidator(langType);
		this.grapValidator = new OpenFeatureGraphicValidator(langType);
		this.langType = langType;
	}

	public OpenLayerValidator(OpenDTLayer validatorLayer, LangType langType, String epsg) {
		this.validatorLayer = validatorLayer;
		this.typeName = validatorLayer.getTypeName();
		this.attrValidator = new OpenFeatureAttributeValidator(langType);
		this.grapValidator = new OpenFeatureGraphicValidator(langType);
		this.langType = langType;
		this.epsg = epsg;
	}

	// 허용범위 이하 길이 (SmallLength)
	public ErrorLayer validateSmallLength(OptionFilter filter, OptionTolerance tolerance) {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errorFeatures = grapValidator.validateSmallLength(feature, tolerance, epsg);
			if (errorFeatures != null) {
				for (ErrorFeature errorFeature : errorFeatures) {
					errorFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(errorFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 허용범위 이하 면적 (SmallArea)
	public ErrorLayer validateSmallArea(OptionFilter filter, OptionTolerance tolerance) {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errorFeatures = grapValidator.validateSmallArea(feature, tolerance, epsg);
			if (errorFeatures != null) {
				for (ErrorFeature errorFeature : errorFeatures) {
					errorFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(errorFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 문자의 정확성(Text Accuracy)
	public ErrorLayer validateFixValues(OptionFilter filter, OptionFigure figure) {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errorFeatures = attrValidator.validateFixValues(feature, figure);
			if (errorFeatures != null) {
				for (ErrorFeature errorFeature : errorFeatures) {
					errorFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(errorFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 단독존재오류 (Self Entity)
	public ErrorLayer validateSelfEntity(OptionFilter filter, OptionTolerance tolerance, Envelope sfcEnvel,
			int enSize) {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		validatorLayer.buildQuad();
		Quadtree quad = validatorLayer.getQuadTree();
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();

		List<Envelope> envelopes = new ArrayList<Envelope>();
		Map<String, Object> valiMap = new HashMap<>();
		envelopes.add(sfcEnvel);
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
		for (int en = 0; en < envelopes.size(); en++) {
			Envelope envelope = envelopes.get(en);
			if (en == 0) {
				if (sfc.size() < enSize) {
					valiMap.put(envelope.toString(), sfc);
					break;
				}
			}
			List<Envelope> halfEnvels = getGrids(envelope, envelope.getHeight() / 2);
			for (Envelope halfEnvel : halfEnvels) {
				List halfItems = quad.query(halfEnvel);
				int tmpSize = halfItems.size();
				SimpleFeatureCollection dfc = new DefaultFeatureCollection();
				for (int i = 0; i < tmpSize; i++) {
					SimpleFeature sf = (SimpleFeature) halfItems.get(i);
					if (sf != null) {
						((DefaultFeatureCollection) dfc).add(sf);
					}
				}
				Filter intFilter = ff.intersects(ff.property("the_geom"), ff.literal(halfEnvel));
				dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);
				DefaultFeatureCollection tfc = new DefaultFeatureCollection();
				SimpleFeatureIterator iter = dfc.features();
				while (iter.hasNext()) {
					SimpleFeature isf = getIntersection(halfEnvel, (SimpleFeature) iter.next());
					if (isf != null) {
						tfc.add(isf);
					}
				}
				iter.close();
				if (tfc.size() > enSize) {
					envelopes.add(halfEnvel);
				} else {
					if (tfc.size() > 0) {
						valiMap.put(halfEnvel.toString(), tfc);
					}
				}
			}
		}
		Iterator iter = valiMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			SimpleFeatureCollection tarSfc = (SimpleFeatureCollection) valiMap.get(key);
			SimpleFeatureIterator simpleFeatureIterator = tarSfc.features();
			List<DTFeature> tmpsSimpleFeatures = new ArrayList<DTFeature>();
			while (simpleFeatureIterator.hasNext()) {
				SimpleFeature simpleFeature = simpleFeatureIterator.next();
				String layerID = simpleFeature.getFeatureType().getName().toString();
				DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
				tmpsSimpleFeatures.add(feature);
			}
			simpleFeatureIterator.close();
			int tmpSize = tmpsSimpleFeatures.size();
			for (int i = 0; i < tmpSize - 1; i++) {
				DTFeature feature = tmpsSimpleFeatures.get(i);
				// self
				for (int j = i + 1; j < tmpSize; j++) {
					DTFeature tmpSimpleFeatureJ = tmpsSimpleFeatures.get(j);
					List<ErrorFeature> errFeatures = grapValidator.validateSelfEntity(feature, tmpSimpleFeatureJ,
							tolerance);
					if (errFeatures != null) {
						for (ErrorFeature errFeature : errFeatures) {
							errFeature.setFeatureID(feature.getSimefeature().getID());
							errorLayer.addErrorFeature(errFeature);
						}
					}
				}
			}
			tarSfc = null;
		}
		sfc = null;
		quad = null;
		envelopes = null;
		valiMap = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 단독존재오류 (Self Entity)
	public ErrorLayer validateSelfEntity(OptionFilter filter, OptionTolerance tolerance, OpenDTLayer reLayer,
			Envelope sfcEnvel, int enSize) {

		ErrorLayer errorLayer = new ErrorLayer();
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();

		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		List<Envelope> envelopes = new ArrayList<Envelope>();
		envelopes.add(sfcEnvel);
		SimpleFeatureCollection reSfc = reLayer.getSimpleFeatureCollection();
		String retypeName = reLayer.getTypeName();
		OptionFilter reAttrFilter = reLayer.getFilter();
		List<AttributeFilter> reAttrConditions = null;
		if (reAttrFilter != null) {
			reAttrConditions = reAttrFilter.getFilter();
		}

		// tar
		validatorLayer.buildQuad();
		Quadtree tarQuad = validatorLayer.getQuadTree();
		// re
		reLayer.buildQuad();
		Quadtree reQuad = reLayer.getQuadTree();

		Map<String, Object> validMap = new HashMap<>();
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
		for (int en = 0; en < envelopes.size(); en++) {
			Envelope envelope = envelopes.get(en);
			if (en == 0) {
				if (sfc.size() < enSize && reSfc.size() < enSize) {
					List<SimpleFeatureCollection> sfcList = new ArrayList<>();
					sfcList.add(sfc);
					sfcList.add(reSfc);
					validMap.put(envelope.toString(), sfcList);
					break;
				}
			}
			List<Envelope> halfEnvels = getGrids(envelope, envelope.getWidth() / 2);
			for (Envelope halfEnvel : halfEnvels) {
				List halfTarItems = tarQuad.query(halfEnvel);
				int tmpSize = halfTarItems.size();
				SimpleFeatureCollection tarTmp = new DefaultFeatureCollection();
				for (int i = 0; i < tmpSize; i++) {
					SimpleFeature sf = (SimpleFeature) halfTarItems.get(i);
					if (sf != null) {
						((DefaultFeatureCollection) tarTmp).add(sf);
					}
				}
				Filter tarGeomFilter = ff.intersects(ff.property("the_geom"), ff.literal(halfEnvel));
				tarTmp = (SimpleFeatureCollection) tarTmp.subCollection(tarGeomFilter);
				DefaultFeatureCollection tarDfc = new DefaultFeatureCollection();
				SimpleFeatureIterator iter = tarTmp.features();
				while (iter.hasNext()) {
					SimpleFeature isf = getIntersection(halfEnvel, (SimpleFeature) iter.next());
					if (isf != null) {
						tarDfc.add(isf);
					}
				}
				iter.close();
				int tarDfcSize = tarDfc.size();
				if (tarDfcSize == 0) {
					continue;
				}
				List halfReItems = reQuad.query(halfEnvel);
				int tmpReSize = halfReItems.size();
				SimpleFeatureCollection reTmp = new DefaultFeatureCollection();
				for (int i = 0; i < tmpReSize; i++) {
					SimpleFeature sf = (SimpleFeature) halfReItems.get(i);
					if (sf != null) {
						((DefaultFeatureCollection) reTmp).add(sf);
					}
				}
				SimpleFeatureCollection reTarDfc = new DefaultFeatureCollection();
				Filter reGeomFilter = ff.intersects(ff.property("the_geom"), ff.literal(halfEnvel));
				SimpleFeatureCollection tmpReDfc = (SimpleFeatureCollection) reTmp.subCollection(reGeomFilter);
				SimpleFeatureIterator reiter = tmpReDfc.features();
				while (reiter.hasNext()) {
					SimpleFeature isf = getIntersection(halfEnvel, (SimpleFeature) reiter.next());
					if (isf != null) {
						((DefaultFeatureCollection) reTarDfc).add(isf);
					}
				}
				reiter.close();
				int reTarDfcSize = reTarDfc.size();
				if (reTarDfcSize == 0) {
					continue;
				}
				if (tarDfcSize > enSize || reTarDfcSize > enSize) {
					envelopes.add(halfEnvel);
				} else {
					if (tarDfcSize > 0 && reTarDfcSize > 0) {
						List<SimpleFeatureCollection> sfcList = new ArrayList<>();
						sfcList.add(tarDfc);
						sfcList.add(reTarDfc);
						validMap.put(halfEnvel.toString(), sfcList);
					}
				}
			}
		}

		Iterator iter = validMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			List<SimpleFeatureCollection> sfcList = (List<SimpleFeatureCollection>) validMap.get(key);

			SimpleFeatureCollection tarValSfc = sfcList.get(0);
			SimpleFeatureCollection reValSfc = sfcList.get(1);

			SimpleFeatureIterator sfIter = tarValSfc.features();
			while (sfIter.hasNext()) {
				SimpleFeature simpleFeature = sfIter.next();
				String layerID = simpleFeature.getFeatureType().getName().toString();
				DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
				SimpleFeatureIterator reSfcIter = reValSfc.features();
				while (reSfcIter.hasNext()) {
					SimpleFeature reSf = reSfcIter.next();
					String relayerID = reSf.getFeatureType().getName().toString();
					DTFeature reFeature = new DTFeature(retypeName, relayerID, reSf, reAttrConditions);
					List<ErrorFeature> errFeatures = grapValidator.validateSelfEntity(feature, reFeature, tolerance);
					if (errFeatures != null) {
						for (ErrorFeature errFeature : errFeatures) {
							errFeature.setFeatureID(simpleFeature.getID());
							errorLayer.addErrorFeature(errFeature);
						}
					}
				}
				reSfcIter.close();
			}
			sfIter.close();
			tarValSfc = null;
			reValSfc = null;
			sfIter = null;
		}
		validMap = null;
		envelopes = null;
		tarQuad = null;
		reQuad = null;
		sfc = null;
		reSfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}

	}

	// 필수속성오류 (AttributeFix)
	public ErrorLayer validateAttributeFixMiss(List<FixedValue> fixedValue) {

		ErrorLayer errorLayer = new ErrorLayer();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, null);
			// attr
			if (fixedValue != null) {
				ErrorFeature attrErrFeature = attrValidator.validateAttributeFixMiss(feature, fixedValue);
				if (attrErrFeature != null) {
					attrErrFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(attrErrFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 요소중복오류 (EntityDuplicated)
	public ErrorLayer validateEntityDuplicated(OptionFilter filter) {

		ErrorLayer errorLayer = new ErrorLayer();

		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();

		Map<String, Object> geomMap = new HashMap<String, Object>();
		Map<String, Object> errMap = new HashMap<>();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature sf = simpleFeatureIterator.next();
			boolean isTrue = true;
			if (attrConditions != null) {
				isTrue = FeatureFilter.filter(sf, attrConditions);
			}
			if (isTrue) {
				Geometry geom = (Geometry) sf.getDefaultGeometry();
				if (geom != null && !geom.isEmpty()) {
					String geomStr = geom.toString();
					if (!geomMap.containsKey(geomStr)) {
						geomMap.put(geomStr, sf);
					} else {
						SimpleFeature tarSf = (SimpleFeature) geomMap.get(geomStr);
						String tarLayerID = tarSf.getFeatureType().getName().toString();
						DTFeature tarFeature = new DTFeature(typeName, tarLayerID, tarSf, attrConditions);
						ErrorFeature tarAttrErrFeature = grapValidator.validateEntityDuplicated(tarFeature);
						if (tarAttrErrFeature != null) {
							tarAttrErrFeature.setFeatureID(tarSf.getID());
							errMap.put(tarSf.getID(), tarAttrErrFeature);
						}
						String layerID = sf.getFeatureType().getName().toString();
						DTFeature feature = new DTFeature(typeName, layerID, sf, attrConditions);
						ErrorFeature attrErrFeature = grapValidator.validateEntityDuplicated(feature);
						if (attrErrFeature != null) {
							attrErrFeature.setFeatureID(sf.getID());
							errMap.put(sf.getID(), attrErrFeature);
						}
					}
				}
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		// 중복 제거 후 errlayer merge
		Set keyset = errMap.keySet();
		if (keyset.size() > 0) {
			Iterator iter = keyset.iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();
				errorLayer.addErrorFeature((ErrorFeature) errMap.get(key));
			}
		}
		geomMap = null;
		errMap = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}

	}

	// 등고선 꺾임 오류 (ConOverDegree)
	public ErrorLayer validateConOverDegree(OptionTolerance tolerance) {

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		ErrorLayer errorLayer = new ErrorLayer();
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errFeatures = grapValidator.validateConOverDegree(feature, tolerance);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 등고선교차오류 (ConIntersected)
	public ErrorLayer validateConIntersected(OptionFilter filter, Envelope sfcEnvel, int enSize) {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		validatorLayer.buildQuad();
		Quadtree quad = validatorLayer.getQuadTree();
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();

		List<Envelope> envelopes = new ArrayList<Envelope>();
		Map<String, Object> valiMap = new HashMap<>();
		envelopes.add(sfcEnvel);
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
		for (int en = 0; en < envelopes.size(); en++) {
			Envelope envelope = envelopes.get(en);
			if (en == 0) {
				if (sfc.size() < enSize) {
					valiMap.put(envelope.toString(), sfc);
					break;
				}
			}
			List<Envelope> halfEnvels = getGrids(envelope, envelope.getHeight() / 2);
			for (Envelope halfEnvel : halfEnvels) {
				List halfItems = quad.query(halfEnvel);
				int tmpSize = halfItems.size();
				SimpleFeatureCollection dfc = new DefaultFeatureCollection();
				for (int i = 0; i < tmpSize; i++) {
					SimpleFeature sf = (SimpleFeature) halfItems.get(i);
					if (sf != null) {
						((DefaultFeatureCollection) dfc).add(sf);
					}
				}
				Filter intFilter = ff.intersects(ff.property("the_geom"), ff.literal(halfEnvel));
				dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);
				DefaultFeatureCollection tfc = new DefaultFeatureCollection();
				SimpleFeatureIterator iter = dfc.features();
				while (iter.hasNext()) {
					SimpleFeature isf = getIntersection(halfEnvel, (SimpleFeature) iter.next());
					if (isf != null) {
						tfc.add(isf);
					}
				}
				iter.close();
				if (tfc.size() > enSize) {
					envelopes.add(halfEnvel);
				} else {
					if (tfc.size() > 0) {
						valiMap.put(halfEnvel.toString(), tfc);
					}
				}
			}
		}
		Iterator iter = valiMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			SimpleFeatureCollection tarSfc = (SimpleFeatureCollection) valiMap.get(key);
			SimpleFeatureIterator simpleFeatureIterator = tarSfc.features();
			List<DTFeature> tmpsSimpleFeatures = new ArrayList<DTFeature>();
			while (simpleFeatureIterator.hasNext()) {
				SimpleFeature simpleFeature = simpleFeatureIterator.next();
				String layerID = simpleFeature.getFeatureType().getName().toString();
				DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
				tmpsSimpleFeatures.add(feature);
			}
			simpleFeatureIterator.close();
			int tmpSize = tmpsSimpleFeatures.size();
			for (int i = 0; i < tmpSize - 1; i++) {
				DTFeature feature = tmpsSimpleFeatures.get(i);
				// self
				for (int j = i + 1; j < tmpSize; j++) {
					DTFeature reFeature = tmpsSimpleFeatures.get(j);
					List<ErrorFeature> errFeatures = grapValidator.validateConIntersected(feature, reFeature);
					if (errFeatures != null) {
						for (ErrorFeature errFeature : errFeatures) {
							errFeature.setFeatureID(feature.getSimefeature().getID());
							errorLayer.addErrorFeature(errFeature);
						}
					}
				}
			}
			tarSfc = null;
		}
		sfc = null;
		quad = null;
		envelopes = null;
		valiMap = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}

	}

	// 등고선 끊김오류 (ConBreak)
	public ErrorLayer validateConBreak(OptionFilter filter, OptionTolerance tolerance, OptionFigure figure,
			OpenDTLayerList reDTLayers, Geometry bEnvelope) throws IOException {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errFeatures = grapValidator.validateConBreak(feature, tolerance, figure, sfc, reDTLayers,
					bEnvelope);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 등고선 직선화미처리오류(UselessPoint)
	public ErrorLayer validateUselessPoint(OptionFilter filter) throws IOException {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errFeatures = grapValidator.validateUselessPoint(feature);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 고도값오류 (Z-Value Abmiguous)
	public ErrorLayer validateZvalueAmbiguous(OptionFilter filter, OptionFigure figure) throws IOException {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errFeatures = attrValidator.validateZvalueAmbiguous(feature, figure);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 중복점오류(DuplicatedPoint)
	public ErrorLayer validatePointDuplicated(OptionFilter filter) {

		ErrorLayer errorLayer = new ErrorLayer();

		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errorFeatures = grapValidator.validatePointDuplicated(feature);
			if (errorFeatures != null) {
				for (ErrorFeature errorFeature : errorFeatures) {
					errorLayer.addErrorFeature(errorFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 경계초과오류 (OutBoundary)
	public ErrorLayer validateOutBoundary(OptionFilter filter, OptionTolerance tolerance, OpenDTLayerList reDTLayers,
			Envelope sfcEnvel, int enSize) {

		ErrorLayer errorLayer = new ErrorLayer();
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();

		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		DefaultFeatureCollection reDfc = new DefaultFeatureCollection();
		for (OpenDTLayer reDtLayer : reDTLayers) {
			SimpleFeatureCollection reSfc = reDtLayer.getSimpleFeatureCollection();
			OptionFilter reAttrFilter = reDtLayer.getFilter();
			List<AttributeFilter> reAttrConditions = null;
			if (reAttrFilter != null) {
				reAttrConditions = reAttrFilter.getFilter();
			}
			SimpleFeatureIterator iterator = reSfc.features();
			while (iterator.hasNext()) {
				SimpleFeature relationSf = iterator.next();
				if (FeatureFilter.filter(relationSf, reAttrConditions)) {
					reDfc.add(relationSf);
				}
			}
			iterator.close();
		}
		// tar
		Quadtree tarQuad = Quadtree.buildQuadTree(sfc);
		// re
		Quadtree reQuad = Quadtree.buildQuadTree(reDfc);

		List<Envelope> envelopes = new ArrayList<Envelope>();
		envelopes.add(sfcEnvel);
		Map<String, Object> validMap = new HashMap<>();
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
		for (int en = 0; en < envelopes.size(); en++) {
			Envelope envelope = envelopes.get(en);
			if (en == 0) {
				if (sfc.size() < enSize && reDfc.size() < enSize) {
					List<SimpleFeatureCollection> sfcList = new ArrayList<>();
					sfcList.add(sfc);
					sfcList.add(reDfc);
					validMap.put(envelope.toString(), sfcList);
					break;
				}
			}
			List<Envelope> halfEnvels = getGrids(envelope, envelope.getWidth() / 2);
			for (Envelope halfEnvel : halfEnvels) {
				List halfTarItems = tarQuad.query(halfEnvel);
				int tmpSize = halfTarItems.size();
				SimpleFeatureCollection tarTmp = new DefaultFeatureCollection();
				for (int i = 0; i < tmpSize; i++) {
					SimpleFeature sf = (SimpleFeature) halfTarItems.get(i);
					if (sf != null) {
						((DefaultFeatureCollection) tarTmp).add(sf);
					}
				}
				Filter tarGeomFilter = ff.intersects(ff.property("the_geom"), ff.literal(halfEnvel));
				tarTmp = (SimpleFeatureCollection) tarTmp.subCollection(tarGeomFilter);
				DefaultFeatureCollection tarDfc = new DefaultFeatureCollection();
				SimpleFeatureIterator iter = tarTmp.features();
				while (iter.hasNext()) {
					SimpleFeature isf = getIntersection(halfEnvel, (SimpleFeature) iter.next());
					if (isf != null) {
						tarDfc.add(isf);
					}
				}
				iter.close();
				int tarDfcSize = tarDfc.size();
				if (tarDfcSize == 0) {
					continue;
				}
				List halfReItems = reQuad.query(halfEnvel);
				int tmpReSize = halfReItems.size();
				SimpleFeatureCollection reTmp = new DefaultFeatureCollection();
				for (int i = 0; i < tmpReSize; i++) {
					SimpleFeature sf = (SimpleFeature) halfReItems.get(i);
					if (sf != null) {
						((DefaultFeatureCollection) reTmp).add(sf);
					}
				}
				SimpleFeatureCollection reTarDfc = new DefaultFeatureCollection();
				Filter reGeomFilter = ff.intersects(ff.property("the_geom"), ff.literal(halfEnvel));
				SimpleFeatureCollection tmpReDfc = (SimpleFeatureCollection) reTmp.subCollection(reGeomFilter);
				SimpleFeatureIterator reiter = tmpReDfc.features();
				while (reiter.hasNext()) {
					SimpleFeature isf = getIntersection(halfEnvel, (SimpleFeature) reiter.next());
					if (isf != null) {
						((DefaultFeatureCollection) reTarDfc).add(isf);
					}
				}
				reiter.close();
				int reTarDfcSize = reTarDfc.size();
				if (reTarDfcSize == 0) {
					continue;
				}
				if (tarDfcSize > enSize || reTarDfcSize > enSize) {
					envelopes.add(halfEnvel);
				} else {
					if (tarDfcSize > 0 && reTarDfcSize > 0) {
						List<SimpleFeatureCollection> sfcList = new ArrayList<>();
						sfcList.add(tarDfc);
						sfcList.add(reTarDfc);
						validMap.put(halfEnvel.toString(), sfcList);
					}
				}
			}
		}

		Iterator iter = validMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			List<SimpleFeatureCollection> sfcList = (List<SimpleFeatureCollection>) validMap.get(key);

			SimpleFeatureCollection tarValSfc = sfcList.get(0);
			SimpleFeatureCollection reValSfc = sfcList.get(1);

			SimpleFeatureIterator sfIter = tarValSfc.features();
			while (sfIter.hasNext()) {
				SimpleFeature simpleFeature = sfIter.next();
				String layerID = simpleFeature.getFeatureType().getName().toString();
				DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
				ErrorFeature errFeature = grapValidator.validateOutBoundary(feature, tolerance, reValSfc);
				if (errFeature != null) {
					errFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(errFeature);
				}
			}
			sfIter.close();
			tarValSfc = null;
			reValSfc = null;
			sfIter = null;
		}
		validMap = null;
		envelopes = null;
		tarQuad = null;
		reQuad = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}

	}

	// 노드오류 (NodeMiss)
	public ErrorLayer validateNodeMiss(OptionFilter filter, OptionTolerance tolerance, OptionFigure figure,
			OpenDTLayer reLayer, Geometry envelop) {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator sfIter = sfc.features();
		Geometry envelopBdr = envelop.getBoundary();
		while (sfIter.hasNext()) {
			SimpleFeature sf = sfIter.next();
			String layerID = sf.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, sf, attrConditions);
			List<ErrorFeature> errFeatures = grapValidator.validateNodeMiss(feature, sfc, reLayer, tolerance, figure,
					envelopBdr);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setFeatureID(sf.getID());
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		sfIter.close();
		sfIter = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {

			return null;
		}
	}

	// 계층오류 (LayerMiss)
	public ErrorLayer validateLayerFixMiss(String geometry) {

		ErrorLayer errorLayer = new ErrorLayer();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, null);
			// geom
			ErrorFeature graphicErrFeature = grapValidator.validateLayerFixMiss(feature, geometry);
			if (graphicErrFeature != null) {
				graphicErrFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(graphicErrFeature);
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 기준점 초과오류 (OverShoot)
	public ErrorLayer validateOverShoot(OptionFilter filter, OptionTolerance tolerance) {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator sfIter = sfc.features();
		while (sfIter.hasNext()) {
			SimpleFeature sf = sfIter.next();
			String layerID = sf.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, sf, attrConditions);
			List<ErrorFeature> errFeatures = grapValidator.validateOverShoot(feature, sfc, tolerance);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setFeatureID(sf.getID());
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		sfIter.close();
		sfIter = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {

			return null;
		}
	}

	// 폴리곤 꼬임 오류 (InvalidPolygon)
	public ErrorLayer validateEntityTwisted(OptionFilter filter) {

		ErrorLayer errorLayer = new ErrorLayer();

		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errorFeatures = grapValidator.validateEntityTwisted(feature);
			if (errorFeatures != null) {
				for (ErrorFeature errorFeature : errorFeatures) {
					errorFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(errorFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 속성오류 (AttributeMiss)
	public ErrorLayer validateAttributeMiss(List<FixedValue> fixedValue) {

		ErrorLayer errorLayer = new ErrorLayer();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			String layerID = simpleFeature.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, simpleFeature, null);
			// attr
			if (fixedValue != null) {
				ErrorFeature attrErrFeature = attrValidator.validateAttributeMiss(feature, fixedValue);
				if (attrErrFeature != null) {
					attrErrFeature.setFeatureID(simpleFeature.getID());
					errorLayer.addErrorFeature(attrErrFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		simpleFeatureIterator = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	// 인접요소속성오류 (RefAttributeMiss)
	public ErrorLayer validateRefAttributeMiss(OptionFilter filter, OptionFigure figure, OptionTolerance tolerance,
			OpenDTLayer retargetLayer) {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator sfIter = sfc.features();
		while (sfIter.hasNext()) {
			SimpleFeature sf = sfIter.next();
			String layerID = sf.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, sf, attrConditions);
			List<ErrorFeature> errFeatures = attrValidator.validateRefAttributeMiss(feature, figure, tolerance, sfc,
					retargetLayer);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setFeatureID(sf.getID());
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		sfIter.close();
		sfIter = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}

	}

	// 인접요소부재오류 (RefEntityNone)
	public ErrorLayer validateRefEntityNone(OptionFilter filter, OptionTolerance tolerance, Geometry envelop) {

		ErrorLayer errorLayer = new ErrorLayer();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator sfIter = sfc.features();
		while (sfIter.hasNext()) {
			SimpleFeature sf = sfIter.next();
			String layerID = sf.getFeatureType().getName().toString();
			DTFeature feature = new DTFeature(typeName, layerID, sf, attrConditions);
			List<ErrorFeature> errFeatures = grapValidator.validateRefEntityNone(feature, sfc, tolerance, envelop);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setFeatureID(sf.getID());
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		sfIter.close();
		sfIter = null;
		sfc = null;
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {

			return null;
		}

	}

	private List<Envelope> getGrids(Envelope envelope, double quadIndexWidth) {

		List<Envelope> resultRefEnl = new ArrayList<Envelope>();
		for (double y = envelope.getMinY(); y < envelope.getMaxY(); y += quadIndexWidth) {
			for (double x = envelope.getMinX(); x < envelope.getMaxX(); x += quadIndexWidth) {
				Envelope newEnvelope = new Envelope(x, x + quadIndexWidth, y, y + quadIndexWidth);
				resultRefEnl.add(newEnvelope);
			}
		}
		return resultRefEnl;
	}

	private SimpleFeature getIntersection(Envelope envelope, SimpleFeature sf) {

		GeometryFactory f = new GeometryFactory();
		SimpleFeature resultSF = SimpleFeatureBuilder.copy(sf);
		Geometry envelpoeGeom = f.toGeometry(envelope);
		Geometry sfGeom = (Geometry) sf.getDefaultGeometry();
		Geometry interGeom = null;
		try {
			interGeom = envelpoeGeom.intersection(sfGeom);
			if (interGeom != null) {
				resultSF.setDefaultGeometry(interGeom);
			}
		} catch (TopologyException e) {
			return resultSF;
		} finally {
			return resultSF;
		}
	}

}
