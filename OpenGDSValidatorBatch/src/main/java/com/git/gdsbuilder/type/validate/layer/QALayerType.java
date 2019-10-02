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

import java.util.List;

import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.validate.option.QAOption;

/**
 * ValidateLayerType 정보를 담고 있는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 3. 11. 오후 3:02:56
 */

public class QALayerType {

	String name;
	List<String> layerIDList;
	QAOption option;

	/**
	 * @author DY.Oh
	 * @since 2018. 3. 30. 오후 2:22:05
	 * @param string
	 * @param colleciton
	 * @return DTLayer
	 * @decription
	 */
	
	
	public DTLayer getTypeLayer(String layerID, DTLayerCollection colleciton) {

		DTLayer dtLayer = null;
		for (int i = 0; i < this.layerIDList.size(); i++) {
			String id = this.layerIDList.get(i);
			if (id.equals(layerID)) {
				dtLayer = colleciton.getLayer(id);
			}
		}
		if (dtLayer != null) {
			return dtLayer;
		} else {
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getLayerIDList() {
		return layerIDList;
	}

	public void setLayerIDList(List<String> layerIDList) {
		this.layerIDList = layerIDList;
	}

	public QAOption getOption() {
		return option;
	}

	public void setOption(QAOption option) {
		this.option = option;
	}
	
	
}
