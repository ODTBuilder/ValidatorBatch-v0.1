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

package com.git.gdsbuilder.type.validate.layer;

import java.util.ArrayList;
import java.util.List;

import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.DTLayerList;
import com.git.gdsbuilder.type.validate.option.specific.OptionRelation;

/**
 * ValidateLayerTypeList 정보를 담고 있는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 3. 11. 오후 3:02:56
 */
@SuppressWarnings("serial")
public class QALayerTypeList extends ArrayList<QALayerType> {

	int category;
	List<String> layerIDList = new ArrayList<String>();

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public List<String> getLayerIDList() {
		return layerIDList;
	}

	public void setLayerIDList(List<String> layerIDList) {
		this.layerIDList = layerIDList;
	}

	public void addAllLayerIdList(List<String> list) {
		this.layerIDList.addAll(list);
	}

	public void addLayerId(String layerID) {
		this.layerIDList.add(layerID);
	}

	public DTLayerList getTypeLayers(String typeName, DTLayerCollection layerCollection) {
		DTLayerList layers = new DTLayerList();
		for (int j = 0; j < this.size(); j++) {
			QALayerType type = this.get(j);
			if (type.getName().equals(typeName)) {
				List<String> names = type.getLayerIDList();
				for (int i = 0; i < names.size(); i++) {
					String name = names.get(i);
					DTLayer geoLayer = layerCollection.getLayer(name);
					if (geoLayer != null) {
						layers.add(geoLayer);
					} else {
						continue;
					}
				}
			}
		}
		return layers;
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 6. 오후 7:16:29
	 * @param code
	 * @param layerCollection
	 * @decription
	 */
	public DTLayer getTypeLayer(String typeName, String code, DTLayerCollection layerCollection) {

		DTLayer layer = null;
		for (int j = 0; j < this.size(); j++) {
			QALayerType type = this.get(j);
			if (type.getName().equals(typeName)) {
				List<String> names = type.getLayerIDList();
				for (int i = 0; i < names.size(); i++) {
					String name = names.get(i);
					if (name.equals(code)) {
						layer = layerCollection.getLayer(name);
					}
				}
			}
		}
		return layer;
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 16. 오후 5:42:03
	 * @param relation
	 * @param layerCollection
	 * @return DTLayerList
	 * @decription
	 */
	public DTLayerList getTypeLayers(OptionRelation relation, DTLayerCollection layerCollection) {

		String typeName = relation.getName();
		DTLayerList layers = new DTLayerList();
		for (int j = 0; j < this.size(); j++) {
			QALayerType type = this.get(j);
			if (type.getName().equals(typeName)) {
				List<String> names = type.getLayerIDList();
				for (int i = 0; i < names.size(); i++) {
					String name = names.get(i);
					DTLayer dtLayer = layerCollection.getLayer(name);
					if (dtLayer != null) {
						// dtLayer.setFigure(relation.getFigure(name));
						dtLayer.setFilter(relation.getFilter(name));
						layers.add(dtLayer);
					} else {
						continue;
					}
				}
			}
		}
		return layers;

	}
	
}
