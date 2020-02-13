/**
 * 
 */
package com.git.gdsbuilder.validator.feature.filter;

import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.DTLayerList;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;

/**
 * @className FeatureFilter.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 19. 오후 2:58:26
 */
public class FeatureFilter {

	public static boolean filter(SimpleFeature sf, AttributeFilter filter) {

		boolean isTrue = false;
		if (filter == null) {
			isTrue = true;
		} else {
			String key = filter.getKey();
			if (key != null) {
				// filter
				List<Object> values = filter.getValues();
				if (values != null) {
					Object attribute = sf.getAttribute(key);
					if (attribute != null) {
						for (Object value : values) {
							if (attribute.toString().equals(value)) {
								isTrue = true;
							}
						}
					}
				} else {
					isTrue = true;
				}
			}
		}
		return isTrue;
	}

	// attr filter
	public static boolean filter(SimpleFeature sf, List<AttributeFilter> filters) {

		boolean isTrue = false;
		if (filters == null) {
			isTrue = true;
		} else {
			for (AttributeFilter filter : filters) {
				String key = filter.getKey();
				if (key == null) {
					continue;
				}
				// filter
				List<Object> values = filter.getValues();
				if (values != null) {
					Object attribute = sf.getAttribute(key);
					for (Object value : values) {
						if (attribute.toString().equals(value)) {
							isTrue = true;
						}
					}
				} else {
					isTrue = true;
				}
			}
		}
		return isTrue;
	}
	// geom filter

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 19. 오후 5:36:39
	 * @param relationLayers
	 * @param reTolerances   void
	 * @decription
	 */
	public static DefaultFeatureCollection filter(DTLayerList relationLayers, List<OptionTolerance> reTolerances) {

		SimpleFeatureCollection tempSfc;
		SimpleFeatureIterator tempIterator = null;
		DefaultFeatureCollection relationSfc = new DefaultFeatureCollection();

		for (int i = 0; i < relationLayers.size(); i++) {
			DTLayer relationLayer = relationLayers.get(i);
			String reLayerCode = relationLayer.getLayerID();
			// tolerance
			if (reTolerances != null) {
				for (OptionTolerance reTolerance : reTolerances) {
					if (!reLayerCode.equals(reTolerance.getCode())) {
						continue;
					}
					// filter
					OptionFilter filters = relationLayer.getFilter();
					if (filters != null) {
						String code = filters.getCode();
						if (!reLayerCode.equals(code)) {
							continue;
						} else {
							List<AttributeFilter> attrFilters = filters.getFilter();
							tempSfc = relationLayer.getSimpleFeatureCollection();
							tempIterator = tempSfc.features();
							while (tempIterator.hasNext()) {
								SimpleFeature relationSf = tempIterator.next();

								if (attrFilters != null) {
									// filter
									if (FeatureFilter.filter(relationSf, attrFilters)) {
										relationSfc.add(relationSf);
									}
								}
							}
						}
					} else {
						tempSfc = relationLayer.getSimpleFeatureCollection();
						tempIterator = tempSfc.features();
						while (tempIterator.hasNext()) {
							SimpleFeature relationSf = tempIterator.next();
							relationSfc.add(relationSf);
						}
					}
				}
			} else {
				tempSfc = relationLayer.getSimpleFeatureCollection();
				tempIterator = tempSfc.features();
				while (tempIterator.hasNext()) {
					SimpleFeature relationSf = tempIterator.next();
					relationSfc.add(relationSf);
				}
			}
		}
		if (tempIterator != null) {
			tempIterator.close();
		}
		return relationSfc;
	}
}
