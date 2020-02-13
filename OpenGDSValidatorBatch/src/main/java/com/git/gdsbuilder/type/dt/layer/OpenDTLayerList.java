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
package com.git.gdsbuilder.type.dt.layer;

import java.util.ArrayList;

/**
 * {@link com.git.gdsbuilder.type.dt.layer.DTLayerList}정보를 저장하는 클래스.
 * <p>
 * 다수의 {@link com.git.gdsbuilder.type.dt.layer.DTLayer}을
 * {@link java.util.ArrayList} 형태로 저장
 * 
 * @author DY.Oh
 */
public class OpenDTLayerList extends ArrayList<OpenDTLayer> {

	/**
	 * {@link com.git.gdsbuilder.type.dt.layer.DTLayerList} 중 layerID에 해당하는
	 * {@link com.git.gdsbuilder.type.dt.layer.DTLayer}를 검색하여 반환
	 * 
	 * @param layerID 검색하고자 하는 {@link com.git.gdsbuilder.type.dt.layer.DTLayer}의
	 *                layerID
	 * @return DTLayer layerID에 해당하는
	 *         {@link com.git.gdsbuilder.type.dt.layer.DTLayer}
	 * 
	 * @author DY.Oh
	 */
	public OpenDTLayer getDTLayer(String layerID) {

		for (int i = 0; i < this.size(); i++) {
			OpenDTLayer layer = this.get(i);
			if (layerID.equals(layer.getLayerID())) {
				return layer;
			}
		}
		return null;
	}

	/**
	 * {@link com.git.gdsbuilder.type.dt.layer.DTLayerList} 중 id와 Geometry type이 동일한
	 * {@link com.git.gdsbuilder.type.dt.layer.DTLayer}의 존재 여부를 확인
	 * 
	 * @param id   {@link com.git.gdsbuilder.type.dt.layer.DTLayer} id
	 * @param type {@link com.git.gdsbuilder.type.dt.layer.DTLayer} Geometry type
	 * @return boolean {@link com.git.gdsbuilder.type.dt.layer.DTLayerList} 중 id와
	 *         Geometry type이 동일한 {@link com.git.gdsbuilder.type.dt.layer.DTLayer}의
	 *         존재 여부
	 * @author DY.Oh
	 */
	public boolean isEqualsLayer(String id, String type) {

		for (int i = 0; i < this.size(); i++) {
			OpenDTLayer layer = this.get(i);
			String layerID = layer.getLayerID();
			if (layerID.equals(id + "_" + type)) {
				return true;
			}
		}
		return false;
	}
}
