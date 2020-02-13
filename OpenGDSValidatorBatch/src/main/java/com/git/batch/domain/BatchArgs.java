package com.git.batch.domain;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * 검수수행을 위한 파라미터 정의 클래스
 * @author SG.Lee
<<<<<<< HEAD
 * @since 2018. 5. 4. 오전 10:10:42
 * */
@Parameters(separators = "=")
public class BatchArgs {
	
	/**
	 * 최상위 폴더 경로 
	 */
	public static final String BASEDIR = "--basedir";
	@Parameter(names = BASEDIR, description = "최상위 폴더", required = false)
	private String baseDir="";
=======
 * @Date 2018. 5. 4. 오전 10:10:42
 */
@Parameters(separators = "=")
public class BatchArgs {

	public static final String BASEDIR = "--basedir";
	@Parameter(names = BASEDIR, description = "최상위 폴더", required = false)
	private String baseDir = "";

	public static final String VALTYPE = "--valtype";
	@Parameter(names = VALTYPE, description = "검수타입(수치지도, 지하시설물, 임상도)", required = false)
	private String valType = "";
>>>>>>> open

	/**
	 * 고정옵션여부(필수X) - 입력안될시 레이어 및 옵션경로 참조
	 * 적용시 "nonset" 입력 - valtype 옵션과 같이 적용
	 */
	public static final String PFLAG = "--pflag";
	@Parameter(names = PFLAG, description = "기존옵션여부", required = false)
<<<<<<< HEAD
	private String pFlag=""; 
	
	/**
	 * 검수타입(수치지도, 지하시설물, 임상도) - 고정옵션시 적용 필수
	 *  "nm1" : 수치지도 1:1000검수
		"nm5" : 수치지도 1:5000 검수
		"nm25" : 수치지도 1:25000 검수
		"ug1" : 지하시설물 1:1000검수
		"ug5" : 지하시설물 1:5000 검수
		"ug25" : 지하시설물 1:25000 검수
		"fr1" : 임상도 1:1000검수
		"fr5" : 임상도 1:5000 검수
		"fr25" : 임상도 1:25000 검수
		(pflag가 nonset일 경우 필수)

	 */
	public static final String VALTYPE = "--valtype";
	@Parameter(names = VALTYPE, description = "검수타입(수치지도, 지하시설물, 임상도)", required = false)
	private String valType="";

	/**
	 * 검수 세부 타입 - 고정옵션시 적용 필수
		"qa1" : 정위치
		"qa2" : 구조화
		(pflag가 nonset일 경우 필수)
	 */
	public static final String VALDTYPE = "--valdtype";
	@Parameter(names = VALDTYPE, description = "검수 세부타입(정위치, 구조화)", required = false)
	private String valDType=""; 
	
	/**
	 * 검수 파일 타입
	 * dxf or ngi or shp
	 */
	public static final String FILETYPE = "--filetype";
	@Parameter(names = FILETYPE, description = "파일타입(dxf, ngi, shp)", required = true)
	private String fileType=""; 
	
	/**
	 * 검수 옵션 타입
	 * 1 - 수치지도1.0, 2 - 수치지도 2.0, 3 - 지하시설물 1.0, 4 - 지하시설물 2.0, 5 - 임상도
	 */
=======
	private String pFlag = "";

	public static final String VALDTYPE = "--valdtype";
	@Parameter(names = VALDTYPE, description = "검수 세부타입(정위치, 구조화)", required = false)
	private String valDType = "";

	public static final String FILETYPE = "--filetype";
	@Parameter(names = FILETYPE, description = "파일타입(dxf, ngi, shp)", required = true)
	private String fileType = "";

>>>>>>> open
	public static final String CIDX = "--cidx";
	@Parameter(names = CIDX, description = "옵션타입(0 - 수치지도 basic, 1 - 수치지도1.0, 2 - 수치지도 2.0, 3 - 지하시설물 1.0, 4 - 지하시설물 2.0, 5 - 임상도)", required = true)
	private String cIdx = "";

	/**
	 * 레이어 정의 옵션 경로(pflag가 nonset이 아닐시 필수)
	 */
	public static final String LAYERDEFPATH = "--layerdefpath";
	@Parameter(names = LAYERDEFPATH, description = "레이어 정의 옵션 경로", required = true, variableArity = true)
	private List<String> layerDefPath = new ArrayList<>();
<<<<<<< HEAD
	
	/**
	 * 검수옵션 경로(pflag가 nonset이 아닐시 필수)
	 */
=======

>>>>>>> open
	public static final String VALOPTPATH = "--valoptpath";
	@Parameter(names = VALOPTPATH, description = "검수 옵션경로", required = true, variableArity = true)
	private List<String> valOptPath = new ArrayList<>();
//	private String valOptPath="";
<<<<<<< HEAD
	
	/**
	 * 검수 대상파일 경로
	 */
=======

>>>>>>> open
	public static final String OBJFILEPATH = "--objfilepath";
	@Parameter(names = OBJFILEPATH, description = "검수 대상파일 경로", required = true, variableArity = true)
	private List<String> objFilePath = new ArrayList<>();
//	private String objFilePath="";
<<<<<<< HEAD
	
	/**
	 * 좌표계(ex. epsg:5186)
	 */
	public static final String CRS = "--crs";
	@Parameter(names = CRS, description = "좌표계", required = true)
	private String crs="";
	
	
	
=======

	public static final String CRS = "--crs";
	@Parameter(names = CRS, description = "좌표계", required = true)
	private String crs = "";

	public static final String LANGTYPE = "--langtype";
	@Parameter(names = LANGTYPE, description = "언어", required = true)
	private String langType = "";

	/**
	 * GET, SET 함수
	 * 
	 * @author SG.Lee
	 * @Date 2018. 5. 4. 오전 10:10:43
	 */
>>>>>>> open
	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getValType() {
		return valType;
	}

	public void setValType(String valType) {
		this.valType = valType;
	}

	public String getValDType() {
		return valDType;
	}

	public void setValDType(String valDType) {
		this.valDType = valDType;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getLayerDefPath() {
		return parsingSpace(this.layerDefPath);
	}

	public void setLayerDefPath(List<String> layerDefPath) {
		this.layerDefPath = layerDefPath;
	}

	public String getValOptPath() {
		return parsingSpace(this.valOptPath);
	}

	public void setValOptPath(List<String> valOptPath) {
		this.valOptPath = valOptPath;
	}

	public String getObjFilePath() {
		return parsingSpace(this.objFilePath);
	}

	public void setObjFilePath(List<String> objFilePath) {
		this.objFilePath = objFilePath;
	}

	public String getpFlag() {
		return pFlag;
	}

	public void setpFlag(String pFlag) {
		this.pFlag = pFlag;
	}

	public String getcIdx() {
		return cIdx;
	}

	public void setcIdx(String cIdx) {
		this.cIdx = cIdx;
	}

	public static String getValtype() {
		return VALTYPE;
	}

	public static String getValdtype() {
		return VALDTYPE;
	}

	public static String getLayerdefpath() {
		return LAYERDEFPATH;
	}

	public static String getValoptpath() {
		return VALOPTPATH;
	}

	public static String getObjfilepath() {
		return OBJFILEPATH;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public String getLangType() {
		return langType;
	}

	public void setLangType(String langType) {
		this.langType = langType;
	}

	public String parsingSpace(List<String> paths) {
		String res = "";
		for (int i = 0; i < paths.size(); i++) {
			if (i == 0) {
				res += paths.get(i);
				continue;
			}
			res += " " + paths.get(i);
		}
		return res;
	}

}
