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

package com.git.gdsbuilder.validator.fileReader.ngi.reader;

import java.util.logging.Level;

import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.DTLayerList;
import com.git.gdsbuilder.validator.fileReader.ngi.NGIDataStore;
import com.git.gdsbuilder.validator.fileReader.ngi.parser.NGIFileLayerParser;
import com.git.gdsbuilder.validator.fileReader.ngi.parser.NGIFileParser;

/**
 * ngi/nda 파일을 QA20LayerCollection 객체로 파싱하는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 5. 11. 오전 10:36:07
 */
public class NGIFileReader {

	public DTLayerCollection read(String epsg, String filePath, String fileName, String neatLine)
			throws Exception {
		org.geotools.util.logging.Logging.getLogger("org").setLevel(Level.OFF);
		NGIFileParser parser = new NGIFileParser();
		NGIDataStore dataStore = parser.parse(filePath, epsg, "EUC-KR");
		DTLayerCollection collection = new DTLayerCollection();

		DTLayerList list;
		if (dataStore.isNDA()) {
			NGIFileLayerParser dtlayers = new NGIFileLayerParser(epsg, dataStore.getNgiReader(), dataStore.getNdaReader());
			list = dtlayers.parseDTLayersWithAtt();
		} else {
			NGIFileLayerParser dtlayers = new NGIFileLayerParser(epsg, dataStore.getNgiReader());
			list = dtlayers.parseDTLayers();
		}
		for (int i = 0; i < list.size(); i++) {
			DTLayer layer = list.get(i);
			String layerName = layer.getLayerID();
			int sfcSize = layer.getSimpleFeatureCollection().size();
			if (sfcSize != 0) {
				if (neatLine != null) {
					if (layerName.equals(neatLine)) {
						collection.setNeatLine(layer);
					}
				}
			}
		}
		collection.setLayers(list);
		collection.setCollectionName(fileName);
		return collection;
	}
}
