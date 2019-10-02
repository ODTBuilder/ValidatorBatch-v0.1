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

package com.git.gdsbuilder.validator.fileReader;

import java.io.IOException;

import org.geotools.feature.SchemaException;

import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.validator.fileReader.dxf.reader.DXFFileReader;
import com.git.gdsbuilder.validator.fileReader.ngi.reader.NGIFileReader;
import com.git.gdsbuilder.validator.fileReader.shp.reader.SHPFileReader;

/**
 * @className FileDTLayerCollectionReader.java
 * @description 검수 대상 파일을 읽어 DTLayerCollection로 파싱하는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 3:19:33
 */
public class FileDTLayerCollectionReader {

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 4:29:53
	 * @param filePath
	 * @param fileName
	 * @param neatLineName
	 * @param neatLine
	 * @return DTLayerCollection
	 * @throws Throwable
	 * @decription shp 파일을 읽어 DTLayerCollection 객체로 파싱
	 */
	public DTLayerCollection shpLayerParse(String epsg, String filePath, String fileName, String neatLineName)
			throws Throwable {

		SHPFileReader fileReader = new SHPFileReader();
		DTLayerCollection dtCollection = fileReader.read(epsg, filePath, fileName, neatLineName);
		return dtCollection;
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 4:30:21
	 * @param epsg
	 * @param filePath
	 * @param fileName
	 * @param neatLineName
	 * @return DTLayerCollection
	 * @throws Exception
	 * @decription dxf 파일을 읽어 DTLayerCollection 객체로 파싱
	 */
	public DTLayerCollection dxfLayerParse(String epsg, String filePath, String fileName, String neatLineName)
			throws Exception {

		DXFFileReader fileReader = new DXFFileReader();
		DTLayerCollection dtCollection = fileReader.read(epsg, filePath, fileName, neatLineName);
		return dtCollection;
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 4:30:35
	 * @param filePath
	 * @param fileName
	 * @param neatLineName
	 * @param neatLine
	 * @return DTLayerCollection
	 * @throws IOException
	 * @throws SchemaException
	 * @decription ngi/nda 파일을 읽어 DTLayerCollection 객체로 파싱
	 */
	public DTLayerCollection ngiLayerParse(String epsg, String filePath, String fileName, String neatLine)
			throws Exception {

		NGIFileReader fileReader = new NGIFileReader();
		DTLayerCollection dtCollection = fileReader.read(epsg, filePath, fileName, neatLine);
		return dtCollection;
	}

}
