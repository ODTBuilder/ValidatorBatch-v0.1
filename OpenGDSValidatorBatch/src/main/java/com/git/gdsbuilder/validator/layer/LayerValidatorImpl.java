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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.git.gdsbuilder.type.dt.feature.DTFeature;
import com.git.gdsbuilder.type.dt.feature.DTFeatureList;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.DTLayerList;
import com.git.gdsbuilder.type.validate.error.ErrorFeature;
import com.git.gdsbuilder.type.validate.error.ErrorLayer;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFigure;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.git.gdsbuilder.type.validate.option.standard.FixedValue;
import com.git.gdsbuilder.validator.feature.FeatureAttributeValidator;
import com.git.gdsbuilder.validator.feature.FeatureAttributeValidatorImpl;
import com.git.gdsbuilder.validator.feature.FeatureCloseCollectionValidator;
import com.git.gdsbuilder.validator.feature.FeatureCloseCollectionValidatorImpl;
import com.git.gdsbuilder.validator.feature.FeatureGraphicValidator;
import com.git.gdsbuilder.validator.feature.FeatureGraphicValidatorImpl;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vividsolutions.jts.geom.Geometry;

public class LayerValidatorImpl implements LayerValidator {

	DTLayer validatorLayer;
	FeatureGraphicValidator graphicValidator = new FeatureGraphicValidatorImpl();
	FeatureAttributeValidator attributeValidator = new FeatureAttributeValidatorImpl();
	FeatureCloseCollectionValidator closeCollectionValidator = new FeatureCloseCollectionValidatorImpl();

	public LayerValidatorImpl(DTLayer validatorLayer) {
		super();
		this.validatorLayer = validatorLayer;

	}

	public LayerValidatorImpl() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the validatorLayer
	 */
	public DTLayer getValidatorLayer() {
		return validatorLayer;
	}

	/**
	 * @param validatorLayer the validatorLayer to set
	 */
	public void setValidatorLayer(DTLayer validatorLayer) {
		this.validatorLayer = validatorLayer;
	}

	// 수치지도 검수
	@Override
	public ErrorLayer validateConBreak(DTLayerList relationLayers, OptionTolerance tole) throws SchemaException {

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();

		while (simpleFeatureIterator.hasNext()) {

			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);

			for (DTLayer relationLayer : relationLayers) {

				List<ErrorFeature> errFeatures = graphicValidator.validateConBreak(feature, relationLayer, tole);
				if (errFeatures != null) {
					for (ErrorFeature errFeature : errFeatures) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateConIntersected() throws SchemaException {

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		List<SimpleFeature> tmpsSimpleFeatures = new ArrayList<SimpleFeature>();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			tmpsSimpleFeatures.add(simpleFeature);
		}
		simpleFeatureIterator.close();
		int tmpsSimpleFeaturesSize = tmpsSimpleFeatures.size();
		for (int i = 0; i < tmpsSimpleFeaturesSize - 1; i++) {
			SimpleFeature tmpSimpleFeatureI = tmpsSimpleFeatures.get(i);
			DTFeature featureI = new DTFeature(layerID, tmpSimpleFeatureI, attrConditions);
			List<ErrorFeature> selfErrFeature = graphicValidator.validateConIntersected(featureI);
			if (selfErrFeature != null) {
				for (ErrorFeature errFeature : selfErrFeature) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			}
			for (int j = i + 1; j < tmpsSimpleFeaturesSize; j++) {
				SimpleFeature tmpSimpleFeatureJ = tmpsSimpleFeatures.get(j);
				DTFeature featureJ = new DTFeature(layerID, tmpSimpleFeatureJ, attrConditions);
				List<ErrorFeature> errFeatures = graphicValidator.validateConIntersected(featureI, featureJ);
				if (errFeatures != null) {
					for (ErrorFeature errFeature : errFeatures) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				} else {
					continue;
				}
			}
		}
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateConOverDegree(OptionTolerance tole) throws SchemaException {

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errFeatures = graphicValidator.validateConOverDegree(feature, tole);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateZValueAmbiguous(OptionFigure figure) throws SchemaException {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errFeature = attributeValidator.validateZvalueAmbiguous(feature, figure);
			if (errFeature != null) {
				errFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errFeature);
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateUselessPoint()
			throws NoSuchAuthorityCodeException, SchemaException, FactoryException, TransformException {

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errFeatures = graphicValidator.validateUselessPoint(feature);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateSmallArea(OptionTolerance tole) throws SchemaException {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		ErrorFeature errorFeature;
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();

		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature sf = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, sf, attrConditions);
			errorFeature = graphicValidator.validateSmallArea(feature, tole);
			if (errorFeature != null) {
				errorFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errorFeature);
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateSmallLength(OptionTolerance tole) throws SchemaException {

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errFeature = graphicValidator.validateSmallLength(feature, tole);
			if (errFeature != null) {
				errFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errFeature);
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateOverShoot(DTLayerList relationLayers, OptionTolerance tole) throws SchemaException {

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		// SimpleFeatureCollection neatLineSfc =
		// neatLayer.getSimpleFeatureCollection();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();

		while (simpleFeatureIterator.hasNext()) {

			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);

			for (DTLayer relationLayer : relationLayers) {

				List<ErrorFeature> errFeatures = graphicValidator.validateOverShoot(feature, relationLayer, tole);
				if (errFeatures != null) {
					for (ErrorFeature errFeature : errFeatures) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				} else {
					continue;
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateSelfEntity(OptionTolerance tolerance) throws SchemaException, IOException {
		ErrorLayer errorLayer = new ErrorLayer();
		String layerId = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		String layerID = validatorLayer.getLayerID();
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator sfcIter = sfc.features();
		while (sfcIter.hasNext()) {
			SimpleFeature sf = sfcIter.next();
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			DTFeature feature = new DTFeature(layerID, sf, attrConditions);
			// self
			SimpleFeatureCollection selfSfc = validatorLayer.getSimpleFeatureCollection();
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
			Filter sffilter = ff.intersects(ff.property("the_geom"), ff.literal(geom.getEnvelope()));
			SimpleFeatureSource source = DataUtilities.source(selfSfc);
			SimpleFeatureCollection filterSfc = source.getFeatures(sffilter);
			SimpleFeatureIterator selfSfcIter = filterSfc.features();
			while (selfSfcIter.hasNext()) {
				SimpleFeature selfSf = selfSfcIter.next();
				if (sf.getID().equals(selfSf.getID())) {
					continue;
				}
				DTFeature reFeature = new DTFeature(layerID, selfSf, attrConditions);
				List<ErrorFeature> errFeatures = graphicValidator.validateSelfEntity(feature, reFeature, tolerance);
				if (errFeatures != null) {
					for (ErrorFeature errFeature : errFeatures) {
						errFeature.setLayerID(layerId);
						errorLayer.addErrorFeature(errFeature);
					}
				}
			}
			selfSfcIter.close();
		}
		sfcIter.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	public ErrorLayer validateSelfEntity(DTLayerList relationLayers, OptionTolerance tolerance)
			throws SchemaException, IOException {

		ErrorLayer errorLayer = new ErrorLayer();
		OptionFilter optionFilter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (optionFilter != null) {
			attrConditions = optionFilter.getFilter();
		}
		String layerID = validatorLayer.getLayerID();
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator sfcIter = sfc.features();

		class Task implements Runnable {
			ErrorLayer errorLayer;
			DTFeature dtFeature;
			List<DTFeature> reDtFeatureList;
			List<ErrorFeature> errFeatures;
			List<AttributeFilter> reAttrConditions;
			String relationLayerId;
			int index;

			public Task(ErrorLayer errorLayer, DTFeature dtFeature, List<DTFeature> reDtFeatureList,
					List<AttributeFilter> reAttrConditions, String relationLayerId, int index) {
				super();
				this.errorLayer = errorLayer;
				this.dtFeature = dtFeature;
				this.reDtFeatureList = reDtFeatureList;
				this.reAttrConditions = reAttrConditions;
				this.relationLayerId = relationLayerId;
				this.index = index;
			}

			@Override
			public void run() {
				System.out.println(dtFeature.getSimefeature().getAttribute("osm_id"));
				for (int j = this.index; j < reDtFeatureList.size(); j++) {
					DTFeature reDtFeature = reDtFeatureList.get(j);
					SimpleFeature reSf = reDtFeature.getSimefeature();
					if (dtFeature.equals(reDtFeature))
						continue;
					DTFeature reFeature = new DTFeature(relationLayerId, reSf, reAttrConditions);
					this.errFeatures = graphicValidator.validateSelfEntity(dtFeature, reFeature, tolerance);
					if (errFeatures != null) {
						for (ErrorFeature errFeature : errFeatures) {
							errFeature.setLayerID(layerID);
						}
						errorLayer.addErrorFeatureCollection(errFeatures);
					}
				}

			}
		}
		SimpleFeatureIterator reSfcIter = null;
		// relation
		if (relationLayers != null) {
			for (int i = 0; i < relationLayers.size(); i++) {
				DTLayer relationLayer = relationLayers.get(i);
				String relationLayerId = relationLayer.getLayerID();
				OptionFilter reFilter = relationLayer.getFilter();
				List<AttributeFilter> reAttrConditions = null;
				if (reFilter != null) {
					reAttrConditions = optionFilter.getFilter();
				}
				SimpleFeatureCollection reSfc = relationLayer.getSimpleFeatureCollection();
				reSfcIter = reSfc.features();

				// 자기 자신 처리 - 멀티스레딩
				if (
				// relationLayers.size() == 1
				layerID.equals(relationLayerId)) {
//					int core = Runtime.getRuntime().availableProcessors();
					int core = 2;
					ExecutorService executorService = Executors.newFixedThreadPool(core,
							new ThreadFactoryBuilder().setNameFormat("self 검수-%d").build());
					DTFeatureList dtFeatureListJ = new DTFeatureList();
					List<Future<List<ErrorFeature>>> futures = new ArrayList<>();
					int index = 0;
					while (sfcIter.hasNext()) {
						SimpleFeature sf = sfcIter.next(); // 행
						// Geometry geom = (Geometry) sf.getDefaultGeometry();
						DTFeature feature = new DTFeature(layerID, sf, attrConditions);

						if (dtFeatureListJ.isEmpty()) {
							while (reSfcIter.hasNext()) {
								dtFeatureListJ.add(new DTFeature(layerID, reSfcIter.next(), attrConditions));
							}
						}

						for (int j = 1; j <= core; j++) {
							int listSize = dtFeatureListJ.size();
							int front = (listSize / core) * (j - 1);
							int end = (listSize / core) * j;
							if (j == core) {
								end = listSize;
							}
							List<ErrorFeature> errFeatures = new ArrayList<>();
							Runnable task = new Task(errorLayer, feature, dtFeatureListJ.subList(front, end),
									reAttrConditions, relationLayerId, index);
							Future<List<ErrorFeature>> futureList = executorService.submit(task, errFeatures);
							futures.add(futureList);
						}
						index++;
					}
					for (Future<List<ErrorFeature>> future : futures) {
						try {
							future.get();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					executorService.shutdown();
					// 복수 레이어 처리
				} else {
					FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
					SimpleFeatureSource source = DataUtilities.source(reSfc);
					SimpleFeatureCollection refilterSfc = null;
					while (sfcIter.hasNext()) {
						SimpleFeature sf = sfcIter.next();
						Geometry geom = (Geometry) sf.getDefaultGeometry();
						Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(geom.getEnvelope()));
						try {
							refilterSfc = source.getFeatures(filter);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						DTFeature feature = new DTFeature(layerID, sf, attrConditions);
						reSfcIter = refilterSfc.features();

						while (reSfcIter.hasNext()) {
							SimpleFeature reSf = reSfcIter.next();
							if (sf.equals(reSf))
								continue;
							DTFeature reFeature = new DTFeature(relationLayerId, reSf, reAttrConditions);
							List<ErrorFeature> errFeatures = graphicValidator.validateSelfEntity(feature, reFeature,
									tolerance);
							if (errFeatures != null) {
								for (ErrorFeature errFeature : errFeatures) {
									errFeature.setLayerID(layerID);
									errorLayer.addErrorFeature(errFeature);
								}
							}
						}
					}
				}
			}
		}
		reSfcIter.close();
		sfcIter.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	public ErrorLayer validateSelfEntity3(DTLayerList relationLayers, OptionTolerance tolerance)
			throws SchemaException, IOException {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerId = validatorLayer.getLayerID();
		OptionFilter optionFilter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (optionFilter != null) {
			attrConditions = optionFilter.getFilter();
		}
		String layerID = validatorLayer.getLayerID();
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator sfcIter = sfc.features();
		DTFeatureList dtFeatureListI = new DTFeatureList();

		while (sfcIter.hasNext()) {
			dtFeatureListI.add(new DTFeature(layerID, sfcIter.next(), attrConditions));
		}

		for (int i = 0; i < dtFeatureListI.size(); i++) {
			SimpleFeature sf = dtFeatureListI.get(i).getSimefeature();
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			DTFeature feature = new DTFeature(layerID, sf, attrConditions);

			// relation
			if (relationLayers != null) { // 1개
				for (int k = 0; k < relationLayers.size(); k++) {

					DTLayer relationLayer = relationLayers.get(k);
					String relationLayerId = relationLayer.getLayerID();
					OptionFilter reFilter = relationLayer.getFilter();
					List<AttributeFilter> reAttrConditions = null;
					if (reFilter != null) {
						reAttrConditions = optionFilter.getFilter();
					}
					SimpleFeatureCollection reSfc = relationLayer.getSimpleFeatureCollection();
					SimpleFeatureIterator reSfcIter = reSfc.features();

					while (reSfcIter.hasNext()) {
						SimpleFeature reSf = reSfcIter.next();
						if (sf.equals(reSf))
							continue;
						DTFeature reFeature = new DTFeature(relationLayerId, reSf, reAttrConditions);
						List<ErrorFeature> errFeatures = graphicValidator.validateSelfEntity(feature, reFeature,
								tolerance);
						if (errFeatures != null) {
							for (ErrorFeature errFeature : errFeatures) {
								errFeature.setLayerID(layerId);
								errorLayer.addErrorFeature(errFeature);
							}
						}
					}
					reSfcIter.close();
				}
			}

		}
		sfcIter.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateOutBoundary(DTLayerList relationLayers, OptionTolerance tole) throws SchemaException {

		// relationlayer ; 도로경계
		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator iter = sfc.features();
		while (iter.hasNext()) {
			SimpleFeature sf = iter.next();
			DTFeature feature = new DTFeature(layerID, sf, attrConditions);
			for (int i = 0; i < relationLayers.size(); i++) {
				DTLayer relationLayer = relationLayers.get(i);
				ErrorFeature errFeature = graphicValidator.validateOutBoundary(feature, relationLayer, tole);
				if (errFeature != null) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				} else {
					continue;
				}
			}
		}
		iter.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateEntityDuplicated() throws SchemaException {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		List<DTFeature> tmpsSimpleFeatures = new ArrayList<DTFeature>();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			tmpsSimpleFeatures.add(feature);
		}
		simpleFeatureIterator.close();
		int tmpSize = tmpsSimpleFeatures.size();
		for (int i = 0; i < tmpSize - 1; i++) {
			DTFeature tmpSimpleFeatureI = tmpsSimpleFeatures.get(i);
			for (int j = i + 1; j < tmpSize; j++) {
				DTFeature tmpSimpleFeatureJ = tmpsSimpleFeatures.get(j);
				ErrorFeature errFeature = graphicValidator.validateEntityDuplicated(tmpSimpleFeatureI,
						tmpSimpleFeatureJ);
				if (errFeature != null) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				} else {
					continue;
				}
			}
		}
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateEntityOpenMiss(DTLayerList relationLayers, OptionTolerance tole) throws SchemaException {

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();

		while (simpleFeatureIterator.hasNext()) {

			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);

			for (DTLayer relationLayer : relationLayers) {

				ErrorFeature errFeature = graphicValidator.validateEntityOpenMiss(feature, relationLayer, tole);
				if (errFeature != null) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {

			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {

			return null;
		}
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 6. 오후 2:02:50
	 * @param fixedValue
	 * @return ErrorLayer
	 * @decription
	 */

	@Override
	public ErrorLayer validateLayerFixMiss(String geometry, List<FixedValue> fixedValue) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, null);
			// geom
			ErrorFeature graphicErrFeature = graphicValidator.validateLayerFixMiss(simpleFeature, geometry);
			if (graphicErrFeature != null) {
				graphicErrFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(graphicErrFeature);
			}
			// attr
			if (fixedValue != null) {
				ErrorFeature attrErrFeature = attributeValidator.validateLayerFixMiss(feature, fixedValue);
				if (attrErrFeature != null) {
					attrErrFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(attrErrFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateBridgeNameMiss(OptionFigure figure, DTLayerList relationLayers,
			List<OptionFigure> reFigures) throws SchemaException {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			for (int i = 0; i < relationLayers.size(); i++) {
				DTLayer relationLayer = relationLayers.get(i);
				OptionFilter relationFilter = relationLayer.getFilter();
				List<AttributeFilter> relationAttrConditions = null;
				if (relationFilter != null) {
					relationAttrConditions = relationFilter.getFilter();
				}
				SimpleFeatureCollection relationSfc = relationLayer.getSimpleFeatureCollection();
				SimpleFeatureIterator relationSimpleFeatureIterator = relationSfc.features();
				while (relationSimpleFeatureIterator.hasNext()) {
					SimpleFeature relationSimpleFeature = relationSimpleFeatureIterator.next();
					DTFeature relationFeature = new DTFeature(layerID, relationSimpleFeature, relationAttrConditions);
					for (OptionFigure reFigure : reFigures) {
						ErrorFeature errorFeature = attributeValidator.validateBridgeNameMiss(feature, relationFeature,
								figure, reFigure);
						if (errorFeature != null) {
							errorFeature.setLayerID(layerID);
							errorLayer.addErrorFeature(errorFeature);
						} else {
							continue;
						}
					}
				}
				relationSimpleFeatureIterator.close();
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateAdminBoundaryMiss(OptionFigure figure) throws SchemaException {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errorFeature = attributeValidator.validateAdminBoundaryMiss(feature, figure);
			if (errorFeature != null) {
				errorFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errorFeature);
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateTwistedPolygon() throws SchemaException {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {

			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();

		while (simpleFeatureIterator.hasNext()) {

			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errorFeature = graphicValidator.validateTwistedPolygon(feature);

			if (errorFeature != null) {

				errorFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errorFeature);
			} else {

				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateNodeMiss(DTLayerList relationLayers, String geomColumn, OptionTolerance tole)
			throws SchemaException {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {

			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();

		while (simpleFeatureIterator.hasNext()) {

			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			for (int i = 0; i < relationLayers.size(); i++) {

				DTLayer relationLayer = relationLayers.get(i);
				List<ErrorFeature> errFeatures = graphicValidator.validateNodeMiss(feature, sfc, relationLayer, tole);

				if (errFeatures != null) {

					for (ErrorFeature errFeature : errFeatures) {

						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				} else {
					continue;
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {

			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {

			return null;
		}
	}

	@Override
	public ErrorLayer validatePointDuplicated() {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {

			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errFeatures = graphicValidator.validatePointDuplicated(feature);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateOneAcre(DTLayerList relationLayers, double spatialAccuracyTolorence) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		List<SimpleFeature> simpleFeatures = new ArrayList<SimpleFeature>();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();

		while (simpleFeatureIterator.hasNext()) {

			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			simpleFeatures.add(simpleFeature);
		}
		simpleFeatureIterator.close();
		for (int i = 0; i < relationLayers.size(); i++) {
			DTLayer relationLayer = relationLayers.get(i);
			SimpleFeatureCollection relationSfc = relationLayer.getSimpleFeatureCollection();
			for (int j = 0; j < simpleFeatures.size(); j++) {
				SimpleFeature simpleFeature = simpleFeatures.get(j);
				DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
				// 단독지류계 검수
				ErrorFeature errFeature = graphicValidator.validateOneAcre(feature, relationSfc);
				if (errFeature != null) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				} else {
					continue;
				}
			}
		}

		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateOneStage(DTLayerList relationLayers) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			for (int i = 0; i < relationLayers.size(); i++) {
				DTLayer relationLayer = relationLayers.get(i);
				List<ErrorFeature> errFeatures = graphicValidator.validateOneStage(feature, relationLayer);
				if (errFeatures != null) {
					for (ErrorFeature errFeature : errFeatures) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				} else {
					continue;
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateUFIDMiss(String collectionName, String layerName, OptionFigure figure,
			DTLayerList relationLayers) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			if (relationLayers != null) {
				for (DTLayer relationLayer : relationLayers) {
					ErrorFeature errorFeature = attributeValidator.validateUFIDMiss(feature, collectionName, layerName,
							figure, relationLayer);
					if (errorFeature != null) {
						errorFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errorFeature);
					} else {
						continue;
					}
				}
			} else {
				ErrorFeature errorFeature = attributeValidator.validateUFIDMiss(feature, collectionName, layerName,
						figure, null);
				if (errorFeature != null) {
					errorFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errorFeature);
				} else {
					continue;
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateBuildingSiteMiss(DTLayerList relationLayers) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errorFeature = graphicValidator.validateBuildingSiteMiss(feature, relationLayers);
			if (errorFeature != null) {
				errorFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errorFeature);
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateNumericalValue(OptionFigure figure) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errorFeature = attributeValidator.validateNumericalValue(feature, figure);
			if (errorFeature != null) {
				errorFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errorFeature);
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateBoundaryMiss(DTLayerList relationLayers) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		for (int i = 0; i < relationLayers.size(); i++) {
			DTLayer relationLayer = relationLayers.get(i);
			while (simpleFeatureIterator.hasNext()) {
				SimpleFeature simpleFeature = simpleFeatureIterator.next();
				DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
				ErrorFeature errorFeature = graphicValidator.validateBoundaryMiss(feature, relationLayer);
				if (errorFeature != null) {
					errorFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errorFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateCenterLineMiss(DTLayerList relationLayers) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		for (int i = 0; i < relationLayers.size(); i++) {
			DTLayer relationLayer = relationLayers.get(i);
			while (simpleFeatureIterator.hasNext()) {
				SimpleFeature simpleFeature = simpleFeatureIterator.next();
				DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
				ErrorFeature errFeature = graphicValidator.validateCenterLineMiss(feature, relationLayer);
				if (errFeature != null) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				} else {
					continue;
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateHoleMissplacement() {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errorFeatures = graphicValidator.validateHoleMissplacement(feature);
			if (errorFeatures != null) {
				for (ErrorFeature errFeature : errorFeatures) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateEntityInHole(DTLayerList relationLayers) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		String layerNameTg = validatorLayer.getLayerID();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		for (int i = 0; i < relationLayers.size(); i++) {
			DTLayer relationLayer = relationLayers.get(i);
			String layerNameRl = relationLayer.getLayerID();
			boolean isEquals = false;
			if (layerNameTg.equals(layerNameRl)) {
				isEquals = true;
			}
			while (simpleFeatureIterator.hasNext()) {
				SimpleFeature simpleFeature = simpleFeatureIterator.next();
				DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
				List<ErrorFeature> errorFeatures = graphicValidator.validateEntityInHole(feature, relationLayer,
						isEquals);
				if (errorFeatures != null) {
					for (ErrorFeature errFeature : errorFeatures) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				} else {
					continue;
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer valildateLinearDisconnection(DTLayerList relationLayers, OptionTolerance tole)
			throws SchemaException {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();

		for (int i = 0; i < relationLayers.size(); i++) {
			DTLayer relationlayer = relationLayers.get(i);
			while (simpleFeatureIterator.hasNext()) {
				SimpleFeature simpleFeature = simpleFeatureIterator.next();
				DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
				List<ErrorFeature> errorFeatures = graphicValidator.validateLinearDisconnection(feature, relationlayer,
						tole);
				if (errorFeatures != null) {
					for (ErrorFeature errFeature : errorFeatures) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				} else {
					continue;
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateMultiPart() {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errFeatures = graphicValidator.validateMultiPart(feature);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateFRefEntityNone(DTLayer closeLayer, Geometry closeBoundary, OptionTolerance tolerance) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errFeature = closeCollectionValidator.validateDRefEntityNone(feature, closeLayer,
					closeBoundary, tolerance);
			if (errFeature != null) {
				errFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errFeature);
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateDRefEntityNone(DTLayer closeLayer, Geometry closeBoundary, OptionTolerance tolerance) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errFeature = closeCollectionValidator.validateDRefEntityNone(feature, closeLayer,
					closeBoundary, tolerance);
			if (errFeature != null) {
				errFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errFeature);
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateRefAttributeMiss(DTLayer closeLayer, Geometry closeBoundary, OptionFigure figure,
			OptionTolerance tolerance) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errFeature = closeCollectionValidator.validateRefAttributeMiss(feature, closeLayer,
					closeBoundary, tolerance, figure);
			if (errFeature != null) {
				errFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errFeature);
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateRefZValueMiss(DTLayer closeLayer, Geometry closeBoundary, OptionFigure figure,
			OptionTolerance tolerance) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			ErrorFeature errFeature = closeCollectionValidator.validateRefZValueMiss(feature, closeLayer, closeBoundary,
					tolerance, figure);
			if (errFeature != null) {
				errFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errFeature);
			} else {
				continue;
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	// 지하시설물 검수
	@Override
	public ErrorLayer validateUNodeMiss(DTLayerList relationLayers) {

		/**
		 * 쓰레드 ValidateUNodeMiss 결과클래스
		 * 
		 * @author SG.Lee
		 * @since 2017. 9. 6. 오후 3:09:38
		 */
		class ValidateUNodeMissResult {
			ErrorLayer treadErrorLayer = new ErrorLayer();

			synchronized void mergeErrorLayer(ErrorLayer typeErrorLayer) {

				if (typeErrorLayer != null) {
					treadErrorLayer.mergeErrorLayer(typeErrorLayer);
				}
			}
		}

		final String layerID = validatorLayer.getLayerID();
		final OptionFilter filter = validatorLayer.getFilter();

		// 관로
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
				new ThreadFactoryBuilder().setNameFormat("관로-%d").build());
		ValidateUNodeMissResult mergeValidateUNodeMissResult = new ValidateUNodeMissResult();

		class Task implements Runnable {
			DTLayer relationLayer;
			ValidateUNodeMissResult validateUNodeMissResult;
			List<AttributeFilter> attrConditions;

			Task(ValidateUNodeMissResult validateUNodeMissResult, DTLayer relationLayer,
					List<AttributeFilter> attrConditions) {
				this.relationLayer = relationLayer;
				this.validateUNodeMissResult = validateUNodeMissResult;
				this.attrConditions = attrConditions;
			}

			@Override
			public void run() {
				ErrorLayer errorLayer = new ErrorLayer();
				OptionFilter reFilter = relationLayer.getFilter();
				SimpleFeatureCollection relationSfc;
				List<AttributeFilter> reAttrFilters = null;
				if (reFilter != null) {
					reAttrFilters = reFilter.getFilter();
				}
				relationSfc = relationLayer.getSimpleFeatureCollection();
				SimpleFeatureIterator relationFeatureIterator = relationSfc.features();
				while (relationFeatureIterator.hasNext()) {
					SimpleFeature relationSf = relationFeatureIterator.next();
					DTFeature reFeature = new DTFeature(layerID, relationSf, reAttrFilters);
					SimpleFeatureIterator simpleFeatureIterator = sfc.features();
					while (simpleFeatureIterator.hasNext()) {
						SimpleFeature sf = simpleFeatureIterator.next();
						DTFeature feature = new DTFeature(layerID, sf, attrConditions);
						ErrorFeature errFeature = graphicValidator.validateUNodeMiss(feature, reFeature);
						if (errFeature != null) {
							errFeature.setLayerID(layerID);
							errorLayer.addErrorFeature(errFeature);
						}
					}
					simpleFeatureIterator.close();
				}
				relationFeatureIterator.close();

				if (errorLayer.getErrFeatureList().size() > 0) {
					errorLayer.setLayerName(validatorLayer.getLayerID());
					validateUNodeMissResult.mergeErrorLayer(errorLayer);
					errorLayer = null; // 에러레이어 초기화
				}
			}
		}
		List<Future<ValidateUNodeMissResult>> futures = new ArrayList<Future<ValidateUNodeMissResult>>();

		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		for (int i = 0; i < relationLayers.size(); i++) {
			DTLayer relationLayer = relationLayers.get(i);
			if (relationLayer != null) {
				Runnable task = new Task(mergeValidateUNodeMissResult, relationLayer, attrConditions);
				Future<ValidateUNodeMissResult> future = executorService.submit(task, mergeValidateUNodeMissResult);
				if (future != null) {
					futures.add(future);
				}
			}
		}
		for (Future<ValidateUNodeMissResult> future : futures) {
			try {
				if (future != null) {
					future.get();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		executorService.shutdown();
		int size = mergeValidateUNodeMissResult.treadErrorLayer.getErrFeatureList().size();

		if (size > 0) {
			return mergeValidateUNodeMissResult.treadErrorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateUAvrgDPH20(List<OptionFigure> figures, DTLayerList relationLayers,
			List<OptionFigure> reFigures) {

		/**
		 * 쓰레드 validateUAvrgDPH20 결과클래스
		 * 
		 * @author SG.Lee
		 * @since 2017. 9. 6. 오후 3:09:38
		 */
		class ValidateUAvrgDPH20Result {
			ErrorLayer treadErrorLayer = new ErrorLayer();

			synchronized void mergeErrorLayer(ErrorLayer typeErrorLayer) {

				if (typeErrorLayer != null) {
					treadErrorLayer.mergeErrorLayer(typeErrorLayer);
				}
			}
		}

		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
				new ThreadFactoryBuilder().setNameFormat("validateUAvrgDPH20-%d").build());
		ValidateUAvrgDPH20Result mergeValidateUAvrgDPH20Result = new ValidateUAvrgDPH20Result();

		class Task implements Runnable {
			DTLayer relationLayer;
			List<AttributeFigure> attributeFigures;
			DTFeature dtFeature;
			ValidateUAvrgDPH20Result validateUAvrgDPH20Result;

			Task(ValidateUAvrgDPH20Result validateUAvrgDPH20Result, DTLayer relationLayer,
					List<AttributeFigure> attributeFigures, DTFeature dtFeature) {
				this.relationLayer = relationLayer;
				this.attributeFigures = attributeFigures;
				this.dtFeature = dtFeature;
				this.validateUAvrgDPH20Result = validateUAvrgDPH20Result;
			}

			@Override
			public void run() {
				ErrorLayer errorLayer = new ErrorLayer();
				for (OptionFigure reFigure : reFigures) {
					String relayerID = relationLayer.getLayerID();
					String reCode = reFigure.getCode();
					if (!relayerID.equals(reCode)) {
						continue;
					}
					List<AttributeFigure> reAttrFigures = reFigure.getFigure();
					ErrorFeature errFeature = attributeValidator.validateUAvrgDPH20(dtFeature, attributeFigures,
							relationLayer, reAttrFigures);
					if (errFeature != null) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				}
				if (errorLayer.getErrFeatureList().size() > 0) {
					errorLayer.setLayerName(validatorLayer.getLayerID());
					validateUAvrgDPH20Result.mergeErrorLayer(errorLayer);
					errorLayer = null; // 에러레이어 초기화
				}
			}
		}

		String code;
		List<AttributeFigure> tempAttrFigures = new ArrayList<AttributeFigure>();

		for (OptionFigure figure : figures) {
			code = figure.getCode();
			if (layerID.equals(code) || code == null) {
				// 관로
				List<AttributeFigure> attrFigs = figure.getFigure();
				for (AttributeFigure attrFig : attrFigs) {
					tempAttrFigures.add(attrFig);
				}
			}
		}

		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			List<Future<ValidateUAvrgDPH20Result>> futures = new ArrayList<Future<ValidateUAvrgDPH20Result>>();

			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);

			// 심도
			for (DTLayer relationLayer : relationLayers) {
				if (relationLayer != null) {
					Runnable task = new Task(mergeValidateUAvrgDPH20Result, relationLayer, tempAttrFigures, feature);
					Future<ValidateUAvrgDPH20Result> future = executorService.submit(task,
							mergeValidateUAvrgDPH20Result);
					if (future != null) {
						futures.add(future);
					}
				}
			}
			for (Future<ValidateUAvrgDPH20Result> future : futures) {
				try {
					if (future != null) {
						future.get();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		simpleFeatureIterator.close();
		executorService.shutdown();

		int size = mergeValidateUAvrgDPH20Result.treadErrorLayer.getErrFeatureList().size();

		if (size > 0) {
			return mergeValidateUAvrgDPH20Result.treadErrorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateULeaderline(DTLayerList relationLayers) throws SchemaException {
		/**
		 * 쓰레드 ValidateULeaderline 결과클래스
		 * 
		 * @author SG.Lee
		 * @since 2017. 9. 6. 오후 3:09:38
		 */
		class ValidateULeaderlineResult {
			ErrorLayer treadErrorLayer = new ErrorLayer();

			synchronized void mergeErrorLayer(ErrorLayer typeErrorLayer) {

				if (typeErrorLayer != null) {
					treadErrorLayer.mergeErrorLayer(typeErrorLayer);
				}
			}
		}

		final String layerID = validatorLayer.getLayerID();
		final OptionFilter filter = validatorLayer.getFilter();
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
				new ThreadFactoryBuilder().setNameFormat("validateULeaderline-%d").build());
		ValidateULeaderlineResult mergeValidateULeaderlineResult = new ValidateULeaderlineResult();

		class Task implements Runnable {
			DTLayer relationLayer;
			ValidateULeaderlineResult validateULeaderlineResult;
			DTFeature feature;
			List<AttributeFilter> attrConditions;

			Task(ValidateULeaderlineResult validateULeaderlineResult, DTFeature feature, DTLayer relationLayer,
					List<AttributeFilter> attrConditions) {
				this.relationLayer = relationLayer;
				this.feature = feature;
				this.validateULeaderlineResult = validateULeaderlineResult;
				this.attrConditions = attrConditions;
			}

			@Override
			public void run() {
				ErrorLayer errorLayer = new ErrorLayer();
				SimpleFeatureCollection relationSfc = relationLayer.getSimpleFeatureCollection();
				SimpleFeatureIterator relationIter = relationSfc.features();
				while (relationIter.hasNext()) {
					SimpleFeature relationSf = relationIter.next();
					Object originType = relationSf.getAttribute("originType");
					if (originType != null) {
						OptionFilter reFilter = relationLayer.getFilter();
						List<AttributeFilter> reAttrFilters = null;
						if (reFilter != null) {
							reAttrFilters = reFilter.getFilter();
						}
						DTFeature reFeature = new DTFeature(layerID, relationSf, reAttrFilters);
						List<ErrorFeature> errFeatures = null;
						if (originType.equals("TEXT")) {
							errFeatures = graphicValidator.validateULeaderLine(feature, reFeature);
						} else if (originType.equals("LWPOLYLINE") || originType.equals("LINESTRING")
								|| originType.equals("POLYLINE")) {
							errFeatures = graphicValidator.validateSelfEntity(feature, reFeature, null);
						}
						if (errFeatures != null) {
							for (ErrorFeature errFeature : errFeatures) {
								errFeature.setLayerID(layerID);
								errorLayer.addErrorFeature(errFeature);
							}
						}
					}
				}
				relationIter.close();

				if (errorLayer.getErrFeatureList().size() > 0) {
					errorLayer.setLayerName(validatorLayer.getLayerID());
					validateULeaderlineResult.mergeErrorLayer(errorLayer);
					errorLayer = null; // 에러레이어 초기화
				}
			}
		}

		List<AttributeFilter> attrConditions = null;

		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		// 지시선 or 관로이력
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature sf = simpleFeatureIterator.next();

			// 쓰레드 생성
			List<Future<ValidateULeaderlineResult>> futures = new ArrayList<Future<ValidateULeaderlineResult>>();
			DTFeature feature = new DTFeature(layerID, sf, attrConditions);
			for (int i = 0; i < relationLayers.size(); i++) {
				DTLayer relationLayer = relationLayers.get(i);
				// 관로
				if (relationLayer != null) {
					Runnable task = new Task(mergeValidateULeaderlineResult, feature, relationLayer, attrConditions);
					Future<ValidateULeaderlineResult> future = executorService.submit(task,
							mergeValidateULeaderlineResult);
					if (future != null) {
						futures.add(future);
					}
				}
			}
			for (Future<ValidateULeaderlineResult> future : futures) {
				try {
					if (future != null) {
						future.get();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		simpleFeatureIterator.close();
		executorService.shutdown();

		int size = mergeValidateULeaderlineResult.treadErrorLayer.getErrFeatureList().size();

		if (size > 0) {
			return mergeValidateULeaderlineResult.treadErrorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateUAvrgDPH10(DTLayerList relationLayers, List<OptionTolerance> reTolerances) {
		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		DTLayer lineLayer = null; // 관로
		DTLayer textLayer = null; // 심도T
		DTLayer readerLayer = null; // 지시선

		OptionTolerance readerTolerance = reTolerances.get(0);
		String readerCode = readerTolerance.getCode();
		OptionTolerance lineTolerance = reTolerances.get(1);
		String lineCode = lineTolerance.getCode();
		OptionTolerance textTolerance = reTolerances.get(2);
		String textCode = textTolerance.getCode();

		for (DTLayer relationLayer : relationLayers) {
			String relationLayerId = relationLayer.getLayerID();
			if (relationLayerId.equals(lineCode)) {
				lineLayer = relationLayer;
			} else if (relationLayerId.equals(textCode)) {
				textLayer = relationLayer;
			} else if (relationLayerId.equals(readerCode)) {
				readerLayer = relationLayer;
			}
		}

		if (lineLayer != null && textLayer != null && readerLayer != null) {
			SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
			SimpleFeatureIterator sfcIter = sfc.features();
			while (sfcIter.hasNext()) {
				SimpleFeature sf = sfcIter.next();
				DTFeature feature = new DTFeature(layerID, sf, attrConditions);
				ErrorFeature errFeature = graphicValidator.validateUAvrgDPH10(feature, readerLayer, lineLayer,
						textLayer);
				if (errFeature != null) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			}
			sfcIter.close();
		}
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateUSymbolDirection(List<OptionFigure> figures, DTLayerList relationLayers,
			List<OptionFigure> reFigures) {

		/**
		 * 쓰레드 ValidateUSymbolDirection 결과클래스
		 * 
		 * @author SG.Lee
		 * @since 2017. 9. 6. 오후 3:09:38
		 */
		class ValidateUSymbolDirectionResult {
			ErrorLayer treadErrorLayer = new ErrorLayer();

			synchronized void mergeErrorLayer(ErrorLayer typeErrorLayer) {

				if (typeErrorLayer != null) {
					treadErrorLayer.mergeErrorLayer(typeErrorLayer);
				}
			}
		}

		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
				new ThreadFactoryBuilder().setNameFormat("ValidateUSymbolDirectionResult-%d").build());
		ValidateUSymbolDirectionResult mergeValidateUSymbolDirectionResult = new ValidateUSymbolDirectionResult();

		class Task implements Runnable {
			DTLayer relationLayer;
			List<AttributeFigure> attributeFigures;
			DTFeature dtFeature;
			ValidateUSymbolDirectionResult validateUSymbolDirectionResult;

			Task(ValidateUSymbolDirectionResult validateUSymbolDirectionResult, DTLayer relationLayer,
					List<AttributeFigure> attributeFigures, DTFeature dtFeature) {
				this.relationLayer = relationLayer;
				this.attributeFigures = attributeFigures;
				this.dtFeature = dtFeature;
				this.validateUSymbolDirectionResult = validateUSymbolDirectionResult;
			}

			@Override
			public void run() {
				ErrorLayer errorLayer = new ErrorLayer();
				for (OptionFigure reFigure : reFigures) {
					String relayerID = relationLayer.getLayerID();
					String reCode = reFigure.getCode();
					if (!relayerID.equals(reCode)) {
						continue;
					}
					OptionFilter reFilter = relationLayer.getFilter();
					List<AttributeFilter> reAttrFilters = null;
					if (reFilter != null) {
						reAttrFilters = reFilter.getFilter();
					}
					SimpleFeatureCollection reSfc = relationLayer.getSimpleFeatureCollection();
					SimpleFeatureIterator reSimpleFeatureIter = reSfc.features();
					while (reSimpleFeatureIter.hasNext()) {
						SimpleFeature reSf = reSimpleFeatureIter.next();
						DTFeature reFeature = new DTFeature(layerID, reSf, reAttrFilters);
						ErrorFeature errFeature = attributeValidator.validateUSymbolDirection(dtFeature,
								attributeFigures, reFeature);
						if (errFeature != null) {
							errFeature.setLayerID(layerID);
							errorLayer.addErrorFeature(errFeature);
						}
					}
					reSimpleFeatureIter.close();
					if (errorLayer.getErrFeatureList().size() > 0) {
						errorLayer.setLayerName(validatorLayer.getLayerID());
						validateUSymbolDirectionResult.mergeErrorLayer(errorLayer);
						errorLayer = null; // 에러레이어 초기화
					}
				}
			}
		}

		String code;
		List<AttributeFigure> tempAttrFigures = new ArrayList<AttributeFigure>();

		for (OptionFigure figure : figures) {
			code = figure.getCode();
			if (layerID.equals(code) || code == null) {
				// 관로
				List<AttributeFigure> attrFigs = figure.getFigure();
				for (AttributeFigure attrFig : attrFigs) {
					tempAttrFigures.add(attrFig);
				}
			}
		}

		List<Future<ValidateUSymbolDirectionResult>> futures = new ArrayList<Future<ValidateUSymbolDirectionResult>>();

		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			for (DTLayer relationLayer : relationLayers) {
				if (relationLayer != null) {
					Runnable task = new Task(mergeValidateUSymbolDirectionResult, relationLayer, tempAttrFigures,
							feature);
					Future<ValidateUSymbolDirectionResult> future = executorService.submit(task,
							mergeValidateUSymbolDirectionResult);
					if (future != null) {
						futures.add(future);
					}
				}
			}
			for (Future<ValidateUSymbolDirectionResult> future : futures) {
				try {
					if (future != null) {
						future.get();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			executorService.shutdown();
		}
		simpleFeatureIterator.close();

		int size = mergeValidateUSymbolDirectionResult.treadErrorLayer.getErrFeatureList().size();

		if (size > 0) {
			return mergeValidateUSymbolDirectionResult.treadErrorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateUSymbolOut(DTLayerList lines) {
		/**
		 * 쓰레드 ValidateUSymbolOut 결과클래스
		 * 
		 * @author SG.Lee
		 * @since 2017. 9. 6. 오후 3:09:38
		 */
		class ValidateUSymbolOutResult {
			ErrorLayer treadErrorLayer = new ErrorLayer();

			synchronized void mergeErrorLayer(ErrorLayer typeErrorLayer) {

				if (typeErrorLayer != null) {
					treadErrorLayer.mergeErrorLayer(typeErrorLayer);
				}
			}
		}

		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();

		// 시설물, 심도
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
				new ThreadFactoryBuilder().setNameFormat("validateUSymbolOut-%d").build());
		ValidateUSymbolOutResult mergeValidateUSymbolOutResult = new ValidateUSymbolOutResult();

		class Task implements Runnable {
			DTLayer relationLayer;
			ValidateUSymbolOutResult validateUSymbolOutResult;
			List<AttributeFilter> attrConditions;

			Task(ValidateUSymbolOutResult validateUSymbolOutResult, DTLayer relationLayer,
					List<AttributeFilter> attrConditions) {
				this.relationLayer = relationLayer;
				this.validateUSymbolOutResult = validateUSymbolOutResult;
				this.attrConditions = attrConditions;
			}

			@Override
			public void run() {
				ErrorLayer errorLayer = new ErrorLayer();

				SimpleFeatureIterator simpleFeatureIterator = sfc.features();
				while (simpleFeatureIterator.hasNext()) {
					SimpleFeature sf = simpleFeatureIterator.next();
					DTFeature feature = new DTFeature(layerID, sf, attrConditions);
					ErrorFeature errFeature = graphicValidator.validateUSymbolOut(feature, relationLayer);
					if (errFeature != null) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				}
				simpleFeatureIterator.close();
				if (errorLayer.getErrFeatureList().size() > 0) {
					errorLayer.setLayerName(validatorLayer.getLayerID());
					validateUSymbolOutResult.mergeErrorLayer(errorLayer);
					errorLayer = null; // 에러레이어 초기화
				}
			}
		}

		List<Future<ValidateUSymbolOutResult>> futures = new ArrayList<Future<ValidateUSymbolOutResult>>();

		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		for (int i = 0; i < lines.size(); i++) {
			// 관로
			DTLayer lineLayer = lines.get(i);
			if (lineLayer != null) {
				Runnable task = new Task(mergeValidateUSymbolOutResult, lineLayer, attrConditions);
				Future<ValidateUSymbolOutResult> future = executorService.submit(task, mergeValidateUSymbolOutResult);
				if (future != null) {
					futures.add(future);
				}
			}
		}
		for (Future<ValidateUSymbolOutResult> future : futures) {
			try {
				if (future != null) {
					future.get();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		executorService.shutdown();

		int size = mergeValidateUSymbolOutResult.treadErrorLayer.getErrFeatureList().size();

		if (size > 0) {
			return mergeValidateUSymbolOutResult.treadErrorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateSymbolInLine(DTLayerList relationLayers, List<OptionTolerance> reTolerances) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		// 관로
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			// 심도
			List<ErrorFeature> errFeatures = graphicValidator.validateSymbolInLine(feature, relationLayers,
					reTolerances);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateSymbolsDistance() {
		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		// 관로
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			// 심도
			List<ErrorFeature> errFeatures = graphicValidator.validateSymbolsDistance(feature);
			if (errFeatures != null) {
				for (ErrorFeature errFeature : errFeatures) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}

	}

	@Override
	public ErrorLayer validateLineCross() {
		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		List<ErrorFeature> errorFeatures;
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		SimpleFeature sf;

		while (simpleFeatureIterator.hasNext()) {
			sf = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, sf, attrConditions);
			errorFeatures = graphicValidator.validateLineCross(feature, validatorLayer);
			if (errorFeatures != null) {
				for (ErrorFeature errorFeature : errorFeatures) {
					errorFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errorFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {

			return null;
		}
	}

	// 임상도 검수

	/**
	 * 
	 * @since 2018. 3. 21.
	 * @author DY.Oh
	 * @param figures
	 * @return
	 */
	@Override
	public ErrorLayer validateFFcodeLogicalAttribute(List<OptionFigure> figures) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		SimpleFeature sf;

		while (simpleFeatureIterator.hasNext()) {
			sf = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, sf, attrConditions);
			for (OptionFigure figure : figures) {
				String fCode = figure.getCode();
				ErrorFeature errorFeature = null;
				if (fCode == null) {
					errorFeature = attributeValidator.validateFcodeLogicalAttribute(feature, figure);
				} else {
					if (fCode.equals(layerID)) {
						errorFeature = attributeValidator.validateFcodeLogicalAttribute(feature, figure);
					}
				}
				if (errorFeature != null) {
					errorFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errorFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {

			return null;
		}
	}

	/**
	 * @since 2018. 3. 21.
	 * @author DY.Oh
	 * @param figures
	 * @return ErrorLayer
	 */
	@Override
	public ErrorLayer validateFLabelLogicalAttribute(List<OptionFigure> figures) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		SimpleFeature sf;
		while (simpleFeatureIterator.hasNext()) {
			sf = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, sf, attrConditions);
			for (OptionFigure figure : figures) {

				String fCode = figure.getCode();
				ErrorFeature errorFeature = null;
				if (fCode == null) {
					errorFeature = attributeValidator.validateFLabelLogicalAttribute(feature, figure);
				} else {
					if (fCode.equals(layerID)) {
						errorFeature = attributeValidator.validateFLabelLogicalAttribute(feature, figure);
					}
				}
				if (errorFeature != null) {
					errorFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errorFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	/**
	 * @since 2018. 3. 23.
	 * @author DY.Oh
	 * @param tole
	 * @return ErrorLayer
	 */
	@Override
	public ErrorLayer validateFEntityInHole() {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature simpleFeature = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, simpleFeature, attrConditions);
			List<ErrorFeature> errorFeatures = graphicValidator.validateFEntityInHole(feature, validatorLayer);
			if (errorFeatures != null) {
				for (ErrorFeature errFeature : errorFeatures) {
					errFeature.setLayerID(layerID);
					errorLayer.addErrorFeature(errFeature);
				}
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	@Override
	public ErrorLayer validateDissolve(List<OptionFigure> figures) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator iter = sfc.features();
		SimpleFeature sf;
		DTFeature feature;

		while (iter.hasNext()) {

			sf = iter.next();
			feature = new DTFeature(layerID, sf, attrConditions);
			for (OptionFigure figure : figures) {
				String fCode = figure.getCode();
				List<ErrorFeature> errorFeatures = new ArrayList<>();
				if (fCode == null) {
					errorFeatures = attributeValidator.validateDissolve(feature, sfc, figure);
				} else {
					if (fCode.equals(layerID)) {
						errorFeatures = attributeValidator.validateDissolve(feature, sfc, figure);
					}
				}
				if (errorFeatures != null) {
					for (ErrorFeature errFeature : errorFeatures) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				}
			}
		}
		iter.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	/**
	 * @since 2018. 3. 30.
	 * @author DY.Oh
	 * @param figures
	 * @param closeLayer
	 * @return ErrorLayer
	 */
	@Override
	public ErrorLayer validateDissolve(List<OptionFigure> figures, DTLayer closeLayer) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();

		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator iter = sfc.features();
		SimpleFeatureCollection closeSfc = closeLayer.getSimpleFeatureCollection();

		SimpleFeature sf;
		DTFeature feature;

		while (iter.hasNext()) {
			sf = iter.next();
			feature = new DTFeature(layerID, sf, attrConditions);
			for (OptionFigure figure : figures) {
				String fCode = figure.getCode();
				List<ErrorFeature> errorFeatures = new ArrayList<>();
				if (fCode == null) {
					errorFeatures = attributeValidator.validateDissolve(feature, closeSfc, figure);
				} else {
					if (fCode.equals(layerID)) {
						errorFeatures = attributeValidator.validateDissolve(feature, closeSfc, figure);
					}
				}
				if (errorFeatures != null) {
					for (ErrorFeature errFeature : errorFeatures) {
						errFeature.setLayerID(layerID);
						errorLayer.addErrorFeature(errFeature);
					}
				}
			}
		}
		iter.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @since 2018. 4. 10.
	 * @author DY.Oh
	 * @param relationLayers
	 * @return
	 */
	@Override
	public ErrorLayer validateSymbolOut(DTLayerList relationLayers) {

		ErrorLayer errorLayer = new ErrorLayer();
		String layerID = validatorLayer.getLayerID();
		OptionFilter filter = validatorLayer.getFilter();
		List<AttributeFilter> attrConditions = null;
		if (filter != null) {
			attrConditions = filter.getFilter();
		}

		// 관로
		SimpleFeatureCollection sfc = validatorLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator simpleFeatureIterator = sfc.features();
		while (simpleFeatureIterator.hasNext()) {
			SimpleFeature sf = simpleFeatureIterator.next();
			DTFeature feature = new DTFeature(layerID, sf, attrConditions);
			ErrorFeature errFeature = graphicValidator.validateSymbolOut(feature, relationLayers);
			if (errFeature != null) {
				errFeature.setLayerID(layerID);
				errorLayer.addErrorFeature(errFeature);
			}
		}
		simpleFeatureIterator.close();
		if (errorLayer.getErrFeatureList().size() > 0) {
			errorLayer.setLayerName(validatorLayer.getLayerID());
			return errorLayer;
		} else {
			return null;
		}
	}
}
