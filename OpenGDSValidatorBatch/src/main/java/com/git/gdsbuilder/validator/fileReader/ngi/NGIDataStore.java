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

package com.git.gdsbuilder.validator.fileReader.ngi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

import org.geotools.data.ngi.NGISchemaReader;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * ngi/nda 파일을 각각 BufferedReader 객체로 변환하는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 3. 11. 오전 11:35:53
 */
public class NGIDataStore {

	private BufferedReader ngiReader;
	private BufferedReader ndaReader;
	private boolean isNDA = false;

	Map<String, SimpleFeatureType> schemas;

	/**
	 * constructors
	 */
	public NGIDataStore() {
		super();
	}

	/**
	 * constructors
	 * 
	 * @param ngiFile
	 * @param charset
	 * @param crs
	 * @throws IOException
	 */
	public NGIDataStore(File ngiFile, Charset charset, CoordinateReferenceSystem crs) throws IOException {

		// Linux
		final int endIndex = ngiFile.getPath().length() - 4;
		File ndaFile = new File(ngiFile.getPath().substring(0, endIndex) + ".nda");
		if (!ndaFile.exists()) {
			ndaFile = new File(ngiFile.getPath().substring(0, endIndex) + ".NDA");
		}
		if (!ndaFile.exists()) {
			this.ngiReader = new BufferedReader(new InputStreamReader(new FileInputStream(ngiFile), charset));
		} else {
			this.ngiReader = new BufferedReader(new InputStreamReader(new FileInputStream(ngiFile), charset));
			this.ndaReader = new BufferedReader(new InputStreamReader(new FileInputStream(ndaFile), charset));
			setNDA(true);
		}
		// create ngi schema
		NGISchemaReader schemaReader = new NGISchemaReader(ngiFile, ndaFile, charset, crs);
		this.schemas = schemaReader.getSchemas();
	}

	public Map<String, SimpleFeatureType> getSchemas() {
		return schemas;
	}

	public void setSchemas(Map<String, SimpleFeatureType> schemas) {
		this.schemas = schemas;
	}

	public BufferedReader getNgiReader() {
		return ngiReader;
	}

	public void setNgiReader(BufferedReader ngiReader) {
		this.ngiReader = ngiReader;
	}

	public BufferedReader getNdaReader() {
		return ndaReader;
	}

	public void setNdaReader(BufferedReader ndaReader) {
		this.ndaReader = ndaReader;
	}

	public boolean isNDA() {
		return isNDA;
	}

	public void setNDA(boolean isNDA) {
		this.isNDA = isNDA;
	}

}
