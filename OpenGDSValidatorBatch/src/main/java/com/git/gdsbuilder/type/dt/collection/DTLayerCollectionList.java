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

package com.git.gdsbuilder.type.dt.collection;

import java.util.ArrayList;
import java.util.List;

import com.git.gdsbuilder.type.dt.layer.DTLayer;

/**
 * LayerCollectionList 정보를 저장하는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 3. 11. 오전 11:45:30
 */
@SuppressWarnings("serial")
public class DTLayerCollectionList extends ArrayList<DTLayerCollection> {

	public long getAllLayerSize() {

		List<DTLayerCollection> collections = this;
		long size = 0;
		for (DTLayerCollection collection : collections) {
			size += collection.layers.size();
		}
		return size;
	}

	public DTLayerCollection getLayerCollection(String collectionName) {

		DTLayerCollection layerCollection = null;
		for (DTLayerCollection collection : this) {
			if (collection.getCollectionName().equals(collectionName)) {
				layerCollection = collection;
				break;
			}
		}
		return layerCollection;
	}

	public DTLayerCollectionList getCloseLayerCollections(MapSystemRule mapSystemRule) {

		DTLayerCollectionList closeList = new DTLayerCollectionList();

		if (mapSystemRule == null) {
			return null;
		}

		Integer top = mapSystemRule.getTop();
		Integer bottom = mapSystemRule.getBottom();
		Integer right = mapSystemRule.getRight();
		Integer left = mapSystemRule.getLeft();

		for (DTLayerCollection collection : this) {
			if (top != null && collection.getCollectionName().equals(String.valueOf(top))) {
				closeList.add(collection);
			}
			if (bottom != null && collection.getCollectionName().equals(String.valueOf(bottom))) {
				closeList.add(collection);
			}
			if (right != null && collection.getCollectionName().equals(String.valueOf(right))) {
				closeList.add(collection);
			}
			if (left != null && collection.getCollectionName().equals(String.valueOf(left))) {
				closeList.add(collection);
			}
		}
		if (closeList.size() > 0) {
			return closeList;
		} else {
			return null;
		}
	}

	public DTLayer getLayer(String name, DTLayerCollectionList layerCollection) {
		// TODO Auto-generated method stub
		return null;
	}
}
