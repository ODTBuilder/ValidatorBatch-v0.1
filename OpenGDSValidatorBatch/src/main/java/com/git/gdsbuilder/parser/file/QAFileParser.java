/**
 * 
 */
package com.git.gdsbuilder.parser.file;

import java.util.Iterator;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

import com.git.gdsbuilder.file.FileMeta;
import com.git.gdsbuilder.file.FileMetaList;
import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.type.dt.collection.DTLayerCollectionList;
import com.git.gdsbuilder.type.dt.collection.MapSystemRule;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.DTLayerList;
import com.git.gdsbuilder.validator.fileReader.FileDTLayerCollectionReader;
import com.git.gdsbuilder.validator.fileReader.UnZipFile;
import com.git.gdsbuilder.validator.fileReader.shp.parser.SHPFileLayerParser;

/**
 * @className QAFileParser.java
 * @description
 * @author DY.Oh
 * @date 2018. 3. 29. 오후 3:05:10
 */
public class QAFileParser {

	private String fileState = "";
	private String brTag = "<br>";

	Integer cIdx;
	String support;
	UnZipFile unZipFile;
	String neatLine;
	String epsg;

	boolean isTrue;
	DTLayerCollectionList collectionList;

	public QAFileParser(String epsg, int cIdx, String support, UnZipFile unZipFile, String neatLine) {

		this.cIdx = cIdx;
		this.support = support;
		this.unZipFile = unZipFile;
		this.neatLine = neatLine;
		this.epsg = epsg;

		if (cIdx == 1 && support.equals("dxf")) {
			parseNumericalQA10File();
			isTrue = true;
		} else if (cIdx == 2 && support.equals("shp")) {
			parseNumericalQA20ShpFile();
			isTrue = true;
		} else if (cIdx == 2 && support.equals("ngi")) {
			parseNumericalQA20NgiFile();
			isTrue = true;
		} else if (cIdx == 3 && support.equals("dxf")) {
			parseUndergroundQA10File();
			isTrue = true;
		} else if (cIdx == 4 && support.equals("shp")) {
			parseUndergroundQA20File();
			isTrue = true;
		} else if (cIdx == 5 && support.equals("shp")) {
			parseForestQA20File();
			isTrue = true;
		} else {
			fileState += "지원하지않는 파일포맷" + brTag;
			isTrue = false;
		}
	}

	/**
	 * @author DY.Oh
	 * @Date 2018. 3. 29. 오후 6:13:52 void
	 * @decription
	 */
	private void parseUndergroundQA20File() {

		if (this.unZipFile.isFiles()) {
			FileMetaList metaList = this.unZipFile.getFileMetaList();
			String collectionName = this.unZipFile.getEntryName();
			DTLayerCollectionList collectionList = new DTLayerCollectionList();
			DTLayerList layerList = new DTLayerList();
			for (int i = 0; i < metaList.size(); i++) {
				FileMeta fileMeta = metaList.get(i);
				String filePath = fileMeta.getFilePath();
				String fileName = fileMeta.getFileName();
				int pos = fileName.lastIndexOf(".");
				String name = fileName.substring(0, pos);
				if (fileName.endsWith("shp")) {
					try {
						DTLayer layer = new SHPFileLayerParser().parseDTLayer(epsg, filePath, name);
						if (layer != null) {
							layerList.add(layer);
						}
					} catch (Exception e) {
						fileState += fileName + ":손상된 파일" + brTag;
					}
				}
			}
			DTLayerCollection collection = new DTLayerCollection();
			collection.setCollectionName(collectionName);
			if (layerList.size() > 0) {
				collection.setLayers(layerList);
				collectionList.add(collection);
			} else {
				fileState += collectionName + ":검수 대상 레이어 미존재" + brTag;
			}
			if (collectionList.size() > 0) {
				this.collectionList = collectionList;
			} else {
				this.collectionList = null;
				fileState += "검수 대상 파일 미존재" + brTag;
			}
		} else {
			this.collectionList = null;
			fileState += "검수 대상 파일 미존재" + brTag;
		}
	}

	/**
	 * @author DY.Oh
	 * @Date 2018. 3. 29. 오후 6:10:03 void
	 * @decription
	 */
	private void parseUndergroundQA10File() {

		if (this.unZipFile.isFiles()) {
			FileMetaList metaList = this.unZipFile.getFileMetaList();
			DTLayerCollectionList collectionList = new DTLayerCollectionList();
			FileDTLayerCollectionReader collectionReader = new FileDTLayerCollectionReader();
			for (int i = 0; i < metaList.size(); i++) {
				FileMeta fileMeta = metaList.get(i);
				String filePath = fileMeta.getFilePath();
				String fileName = fileMeta.getFileName();
				int pos = fileName.lastIndexOf(".");
				String name = fileName.substring(0, pos);
				if (fileName.endsWith("dxf")) {
					DTLayerCollection collection = null;
					try {
						collection = collectionReader.dxfLayerParse(this.epsg, filePath, name, null);
						if (collection == null) {
							fileState += fileName + ":검수 대상 레이어 미존재" + brTag;
							continue;
						} else {
							collectionList.add(collection);
						}
					} catch (Exception e) {
						fileState += fileName + ":손상된 파일" + brTag;
					}
				}
			}
			if (collectionList.size() > 0) {
				this.collectionList = collectionList;
			} else {
				this.collectionList = null;
				fileState += "검수 대상 파일 미존재" + brTag;
			}
		} else {
			this.collectionList = null;
			fileState += "검수 대상 파일 미존재" + brTag;
		}
	}

	/**
	 * @author DY.Oh
	 * @Date 2018. 3. 29. 오후 6:04:16 void
	 * @decription
	 */
	private void parseNumericalQA20NgiFile() {

		if (unZipFile.isFiles()) {
			FileDTLayerCollectionReader collectionReader = new FileDTLayerCollectionReader();
			FileMetaList metaList = unZipFile.getFileMetaList();
			DTLayerCollectionList collectionList = new DTLayerCollectionList();
			for (int i = 0; i < metaList.size(); i++) {
				FileMeta fileMeta = metaList.get(i);
				String filePath = fileMeta.getFilePath();
				String fileName = fileMeta.getFileName();
				int pos = fileName.lastIndexOf(".");
				String name = fileName.substring(0, pos);
				DTLayerCollection collection = null;
				try {
					if (fileName.endsWith("ngi")) {
						collection = collectionReader.ngiLayerParse(epsg, filePath, name, neatLine);
						
						
						
						if (collection == null) {
							fileState += fileName + ":검수 대상 레이어 미존재" + brTag;
							continue;
						}
					}
					if (this.neatLine != null && collection.getNeatLine() == null) {
						this.fileState += fileName + ":도곽미존재" + this.brTag;
						collection = null;
					}
					if (this.neatLine != null && collection.getNeatLine() != null) {
						collection.setMapRule(new MapSystemRule().setMapSystemRule(collection.getCollectionName()));
						collectionList.add(collection);
					}
					if (this.neatLine == null) {
						collection.setMapRule(new MapSystemRule().setMapSystemRule(collection.getCollectionName()));
						collectionList.add(collection);
					}
				} catch (Exception e) {
					fileState += fileName + ":손상된 파일" + brTag;
				}
			}
			if (collectionList.size() > 0) {
				this.collectionList = collectionList;
			} else {
				this.collectionList = null;
				fileState += "검수 대상 파일 미존재" + brTag;
			}
		} else {
			this.collectionList = null;
			fileState += "검수 대상 파일 미존재" + brTag;
		}
	}

	/**
	 * @author DY.Oh
	 * @Date 2018. 3. 29. 오후 5:49:52 void
	 * @decription
	 */
	private void parseNumericalQA20ShpFile() {

		if (this.unZipFile.isDir()) {
			Map<String, FileMetaList> dirMetaList = this.unZipFile.getDirMetaList();
			DTLayerCollectionList collectionList = new DTLayerCollectionList();
			Iterator dirIterator = dirMetaList.keySet().iterator();
			while (dirIterator.hasNext()) {
				String dirPath = (String) dirIterator.next();
				String dirName = getDirName(dirPath);
				DTLayerCollection collection = new DTLayerCollection();
				collection.setCollectionName(dirName);
				DTLayerList layerList = new DTLayerList();
				FileMetaList metaList = dirMetaList.get(dirPath);
				for (int i = 0; i < metaList.size(); i++) {
					DTLayer layer = new DTLayer();
					FileMeta fileMeta = metaList.get(i);
					String fileName = fileMeta.getFileName();
					int pos = fileName.lastIndexOf(".");
					String name = fileName.substring(0, pos);
					if (fileName.endsWith("shp")) {
						try {
							layer = new SHPFileLayerParser().parseDTLayer(epsg, dirPath, name);
							if (this.neatLine != null) {
								if (this.neatLine.equals(layer.getLayerID())) {
									collection.setNeatLine(layer);
								} else {
									layerList.add(layer);
								}
							} else {
								layerList.add(layer);
							}
						} catch (Exception e) {
							fileState += fileName + ":손상된 파일" + brTag;
						}
					}
				}
				if (layerList.size() > 0) {
					collection.setLayers(layerList);
				} else {
					fileState += dirName + ":검수 대상 레이어 미존재" + brTag;
					continue;
				}
				if (this.neatLine != null && collection.getNeatLine().getSimpleFeatureCollection() == null) {
					this.fileState += dirName + ":도곽미존재" + this.brTag;
					collection = null;
				}
				if (this.neatLine != null && collection.getNeatLine().getSimpleFeatureCollection() != null) {
					collection.setMapRule(new MapSystemRule().setMapSystemRule(collection.getCollectionName()));
					collectionList.add(collection);
				}
				if (this.neatLine == null) {
					collection.setMapRule(new MapSystemRule().setMapSystemRule(collection.getCollectionName()));
					collectionList.add(collection);
				}
			}
			if (collectionList.size() > 0) {
				this.collectionList = collectionList;
			} else {
				this.collectionList = null;
				fileState += "검수 대상 파일 미존재" + brTag;
			}
		} else {
			this.collectionList = null;
			fileState += "검수 대상 파일 미존재" + brTag;
		}
	}

	/**
	 * @author DY.Oh
	 * @Date 2018. 3. 29. 오후 3:10:06
	 * @decription
	 */
	private void parseNumericalQA10File() {

		if (this.unZipFile.isFiles()) {

			FileMetaList metaList = this.unZipFile.getFileMetaList();
			DTLayerCollectionList collectionList = new DTLayerCollectionList();
			FileDTLayerCollectionReader collectionReader = new FileDTLayerCollectionReader();
			for (int i = 0; i < metaList.size(); i++) {
				FileMeta fileMeta = metaList.get(i);
				String filePath = fileMeta.getFilePath();
				String fileName = fileMeta.getFileName();
				int pos = fileName.lastIndexOf(".");
				String name = fileName.substring(0, pos);
				if (fileName.endsWith("dxf")) {
					DTLayerCollection collection = null;
					try {
						collection = collectionReader.dxfLayerParse(this.epsg, filePath, name, this.neatLine);

				

						if (collection == null) {
							fileState += fileName + ":검수 대상 레이어 미존재" + brTag;
							continue;
						}
						DTLayer neatLineLayer = collection.getNeatLine();
						if (this.neatLine != null && neatLineLayer == null) {
							this.fileState += fileName + ":도곽미존재" + this.brTag;
							collection = null;
							continue;
						}
						if (this.neatLine != null && neatLineLayer != null) {
							collection.setMapRule(new MapSystemRule().setMapSystemRule(collection.getCollectionName()));
							collectionList.add(collection);
						}
						if (this.neatLine == null) {
							collectionList.add(collection);
						}
					} catch (Exception e) {
						fileState += fileName + ":손상된 파일" + brTag;
					}
				}
			}
			if (collectionList.size() > 0) {
				this.collectionList = collectionList;
			} else {
				this.collectionList = null;
				fileState += "검수 대상 파일 미존재" + brTag;
			}
		} else {
			this.collectionList = null;
			fileState += "검수 대상 파일 미존재" + brTag;
		}
	}

	private void parseForestQA20File() {

		if (this.unZipFile.isDir()) {
			Map<String, FileMetaList> dirMetaList = this.unZipFile.getDirMetaList();
			DTLayerCollectionList collectionList = new DTLayerCollectionList();
			Iterator dirIterator = dirMetaList.keySet().iterator();
			while (dirIterator.hasNext()) {
				String dirPath = (String) dirIterator.next();
				String dirName = getDirName(dirPath);
				DTLayerCollection collection = new DTLayerCollection();
				collection.setCollectionName(dirName);
				DTLayerList layerList = new DTLayerList();
				FileMetaList metaList = dirMetaList.get(dirPath);
				DTLayer neatlineLayer = new DTLayer();
				for (int i = 0; i < metaList.size(); i++) {
					DTLayer layer = new DTLayer();
					FileMeta fileMeta = metaList.get(i);
					String fileName = fileMeta.getFileName();
					int pos = fileName.lastIndexOf(".");
					String name = fileName.substring(0, pos);
					if (fileName.endsWith("shp")) {
						try {
							layer = new SHPFileLayerParser().parseDTLayer(epsg, dirPath, name);
							if (this.neatLine != null) {
								if (this.neatLine.equals(layer.getLayerID())) {
									neatlineLayer = layer;
								} else {
									layerList.add(layer);
								}
							} else {
								layerList.add(layer);
							}
						} catch (Exception e) {
							// e.printStackTrace();
							// System.out.println(e.);
							// fileState += fileName + ":손상된 파일" + brTag;
						}
					}
				}
				if (layerList.size() > 0) {
					if (this.neatLine != null && neatlineLayer.getSimpleFeatureCollection() == null) {
						this.fileState += dirName + ":도곽미존재" + this.brTag;
					}
					if (this.neatLine != null && neatlineLayer.getSimpleFeatureCollection() != null) {
						DTLayerCollection setDTCollection = setForestNeatLine(collection, neatlineLayer);
						setDTCollection.setCollectionName(collection.getCollectionName());
						setDTCollection.setLayers(layerList);
						setDTCollection
								.setMapRule(new MapSystemRule().setMapSystemRule(setDTCollection.getCollectionName()));
						if (setDTCollection != null) {
							collectionList.add(setDTCollection);
						} else {
							this.fileState += dirName + ":도곽미존재" + this.brTag;
						}
					}
				} else {
					if (!collection.getCollectionName().equals(this.neatLine)) {
						this.fileState += dirName + ":검수 대상 레이어 미존재" + brTag;
						continue;
					}
				}

			}
			if (collectionList.size() > 0) {
				this.collectionList = collectionList;
			} else {
				this.collectionList = null;
				fileState += "검수 대상 파일 미존재" + brTag;
			}
		} else {
			this.collectionList = null;
			fileState += "검수 대상 파일 미존재" + brTag;
		}
	}

	/**
	 * @author DY.Oh
	 * @Date 2018. 4. 17. 오전 11:20:17
	 * @param collectionList2
	 * @param neatlineLayer
	 * @decription
	 */
	private DTLayerCollection setForestNeatLine(DTLayerCollection collection, DTLayer neatlineLayer) {

		SimpleFeatureCollection neatSfc = neatlineLayer.getSimpleFeatureCollection();
		SimpleFeatureIterator iter = neatSfc.features();

		DTLayerCollection dtLayerCollection = new DTLayerCollection();
		DefaultFeatureCollection dfc = new DefaultFeatureCollection();
		String collectionName = collection.getCollectionName();
		while (iter.hasNext()) {
			SimpleFeature sf = iter.next();
			Object num = sf.getAttribute("ALLNUM");
			if (num != null) {
				String numStr = num.toString();
				if (collectionName.equals(numStr)) {
					dfc.add(sf);
					DTLayer neatLine = new DTLayer();
					neatLine.setLayerID("index");
					neatLine.setSimpleFeatureCollection(dfc);
					dtLayerCollection.setNeatLine(neatLine);
				}
			}
		}
		iter.close();
		return dtLayerCollection;
	}

	private static String getDirName(String dirName) {

		String[] dirs = dirName.split("\\\\");
		return dirs[dirs.length - 1];
	}

	public String getFileState() {
		return fileState;
	}

	public void setFileState(String fileState) {
		this.fileState = fileState;
	}

	public String getBrTag() {
		return brTag;
	}

	public void setBrTag(String brTag) {
		this.brTag = brTag;
	}

	public Integer getcIdx() {
		return cIdx;
	}

	public void setcIdx(Integer cIdx) {
		this.cIdx = cIdx;
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}

	public UnZipFile getUnZipFile() {
		return unZipFile;
	}

	public void setUnZipFile(UnZipFile unZipFile) {
		this.unZipFile = unZipFile;
	}

	public String getNeatLine() {
		return neatLine;
	}

	public void setNeatLine(String neatLine) {
		this.neatLine = neatLine;
	}

	public String getEpsg() {
		return epsg;
	}

	public void setEpsg(String epsg) {
		this.epsg = epsg;
	}

	public boolean isTrue() {
		return isTrue;
	}

	public void setTrue(boolean isTrue) {
		this.isTrue = isTrue;
	}

	public DTLayerCollectionList getCollectionList() {
		return collectionList;
	}

	public void setCollectionList(DTLayerCollectionList collectionList) {
		this.collectionList = collectionList;
	}

}
