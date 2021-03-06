package com.git.batch.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.geotools.feature.SchemaException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.git.batch.step.Progress;
import com.git.gdsbuilder.file.FileMeta;
import com.git.gdsbuilder.file.FileMetaList;
import com.git.gdsbuilder.file.writer.SHPFileWriter;
import com.git.gdsbuilder.parser.file.QAFileParser;
import com.git.gdsbuilder.parser.qa.QATypeParser;
import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.type.dt.collection.DTLayerCollectionList;
import com.git.gdsbuilder.type.validate.error.ErrorLayer;
import com.git.gdsbuilder.type.validate.layer.QALayerTypeList;
import com.git.gdsbuilder.type.validate.option.en.LangType;
import com.git.gdsbuilder.validator.collection.CollectionValidator;
import com.git.gdsbuilder.validator.collection.OpenCollectionValidator;
import com.git.gdsbuilder.validator.fileReader.UnZipFile;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * 검수수행 요청을 처리하는 Service 클래스
 * @author SG.LEE
 */
/**
 * @author SG.LEE
 *
 */
/**
 * @author SG.LEE
 *
 */
public class BathService {

	public static int totalValidSize = 0;
	/**
	 * 검수 수행 {@link Progress}
	 */
	public static Progress pb;

	// file dir
	protected String ERR_OUTPUT_DIR;
	/**
	 * 에러 레이어 Output 폴더경로
	 */
	protected String ERR_FILE_DIR;
	/**
	 * 에러 레이어 Output 파일이름
	 */
	protected String ERR_OUTPUT_NAME;
	/**
	 * 에러 레이어 Output 압축파일 이름
	 */
	protected String ERR_ZIP_DIR;

	/**
	 * 검수수행 단계( 1 - 파일업로드 완료)
	 */
	protected int fileUpload = 1;
	/**
	 * 검수수행 단계( 2 - 검수수행중)
	 */
	protected int validateProgresing = 2;
	/**
	 * 검수수행 단계( 3 - 검수성공)
	 */
	protected int validateSuccess = 3;
	/**
	 * 검수수행 단계( 4 - 검수실패)
	 */
	protected int validateFail = 4;

	Logger logger = LoggerFactory.getLogger(BathService.class);

	/**
	 * 해당 경로 및 옵션파일을 통한 검수 수행
	 * 
	 * @param baseDir      최상위 폴더
	 * @param valType      검수타입(수치지도, 지하시설물, 임상도)
	 * @param pFlag        기존옵션여부
	 * @param valDType     검수 세부타입(정위치, 구조화)
	 * @param fileType     파일타입(dxf, ngi, shp)
	 * @param category     옵션타입(1 - 수치지도1.0, 2 - 수치지도 2.0, 3 - 지하시설물 1.0, 4 - 지하시설물
	 *                     2.0, 5 - 임상도)
	 * @param layerDefPath 레이어 정의 옵션 경로
	 * @param valOptPath   검수 옵션경로
	 * @param objFilePath  검수 대상파일 경로
	 * @param crs          좌표계
	 * @return boolean 검수 성공여부
	 * @throws Throwable 검수 폴더 생성 {@link Exception}
	 */
	@SuppressWarnings("unchecked")
	public boolean validate(String baseDir, String valType, String pFlag, String valDType, String fileType,
			int category, String layerDefPath, String valOptPath, String objFilePath, String crs, LangType langType)
			throws Throwable {
		java.util.logging.Logger.getLogger("com.git.batch.service.BathService").setLevel(Level.OFF);
		boolean isSuccess = false;

		String qaVer = valDType;// 검수세부타입
		String qaType = valType;
		String prid = pFlag;
		String fileformat = fileType;
		int cIdx = category;
		String epsg = crs;
		String support = fileType;

		// 옵션또는 파일이 제대로 넘어오지 않았을때 강제로 예외발생
		if (qaVer == null || qaType == null || prid == null) {
			if (langType.getLang().equals("ko")) {
				System.out.println("검수 옵션을 재설정하세요");
			} else if (langType.getLang().equals("en")) {
				System.out.println("Please request again.");
			}
			return isSuccess;
		} else if (fileformat == null) {
			if (langType.getLang().equals("ko")) {
				System.out.println("검수 파일 포맷을 재설정하세요.");
			} else if (langType.getLang().equals("en")) {
				System.out.println("Please set the file format.");
			}
			return isSuccess;
		} else {
			long time = System.currentTimeMillis();

			SimpleDateFormat dayTime = new SimpleDateFormat("yyMMdd_HHmmss");
			String cTimeStr = dayTime.format(new Date(time));

			UnZipFile unZipFile = new UnZipFile(objFilePath);
			unZipFile.getFilMeta(objFilePath);
			String comment = unZipFile.getFileState();

			/*
			 * 적어도 해당 파일 형식이 한 개는 있는지 검사
			 */
			boolean checkExt = false;
			if (unZipFile.isFiles()) {
				FileMetaList fList = unZipFile.getFileMetaList();
				for (FileMeta fMeta : fList) {
					if (fMeta.getFileName().endsWith(fileType)) {
						checkExt = true;
						break;
					}
				}
			} else if (unZipFile.isDir()) {
				Map<String, FileMetaList> dirMetaList = unZipFile.getDirMetaList();
				Iterator<?> dirIterator = dirMetaList.keySet().iterator();
				while (dirIterator.hasNext()) {
					String dirPath = (String) dirIterator.next();
					FileMetaList metaList = dirMetaList.get(dirPath);
					for (FileMeta fileMeta : metaList) {
						if (fileMeta.getFileName().endsWith(fileType)) {
							checkExt = true;
							break;
						}
					}
				}
			}
			if (!checkExt) {
				if (langType.getLang().equals("ko")) {
					System.out.println("검수 대상 파일에 " + fileType + "가 존재하지 않습니다.");
				} else if (langType.getLang().equals("en")) {
					System.out.println(fileType + " does not exist in the target file.");
				}
				throw new Throwable();
			}

			// option parsing
			JSONParser jsonP = new JSONParser();
			JSONObject option = null;
			JSONArray layers = null;
			try {
				option = (JSONObject) ((Object) jsonP.parse(new FileReader(valOptPath)));
			} catch (ClassCastException e) {
				if (langType.getLang().equals("ko")) {
					System.out.println("잘못된 옵션 파일입니다.");
				} else if (langType.getLang().equals("en")) {
					System.out.println("Invalid option file");
				}
				throw new Throwable();
			}
			try {
				layers = (JSONArray) ((Object) jsonP.parse(new FileReader(layerDefPath)));
			} catch (ClassCastException e) {
				if (langType.getLang().equals("ko")) {
					System.out.println("잘못된 레이어 정의 파일입니다.");
				} else if (langType.getLang().equals("en")) {
					System.out.println("Invalid layer definition file.");
				}
				throw new Throwable();
			}

			Object neatLine = option.get("border");
			String neatLineCode = null;
			if (neatLine != null) {
				JSONObject neatLineObj = (JSONObject) neatLine;
				neatLineCode = (String) neatLineObj.get("code");
			}

			JSONArray attrFilterArry = null;
			JSONArray stateFilterArry = null;
			Object filterObj = option.get("filter");
			if (filterObj != null) {
				JSONObject filterJson = (JSONObject) filterObj;
				Object attrObj = filterJson.get("attribute");
				if (attrObj != null) {
					attrFilterArry = (JSONArray) attrObj;
				}
				Object stateObj = filterJson.get("state");
				if (stateObj != null) {
					stateFilterArry = (JSONArray) stateObj;
				}
			}

			JSONArray typeValidate = (JSONArray) option.get("definition");
			for (int j = 0; j < layers.size(); j++) {
				JSONObject lyrItem = (JSONObject) layers.get(j);
				Boolean isExist = false;
				for (int i = 0; i < typeValidate.size(); i++) {
					JSONObject optItem = (JSONObject) typeValidate.get(i);
					String typeName = (String) optItem.get("name");
					if (typeName.equals((String) lyrItem.get("name"))) {
						optItem.put("layers", (JSONArray) lyrItem.get("layers"));
						isExist = true;
					}
				}
				if (!isExist) {
					JSONObject obj = new JSONObject();
					obj.put("name", (String) lyrItem.get("name"));
					obj.put("layers", (JSONArray) lyrItem.get("layers"));
					typeValidate.add(obj);
				}
			}
			// options
			QATypeParser validateTypeParser = new QATypeParser(typeValidate);
			QALayerTypeList validateLayerTypeList = validateTypeParser.getValidateLayerTypeList();
			if (validateLayerTypeList == null) {
				comment += validateTypeParser.getComment();
				if (!comment.equals("")) {
					// logger.info(comment);
					System.out.println(comment);
				}
				return isSuccess;
			}
			validateLayerTypeList.setCategory(cIdx);

			// set err directory
			ERR_OUTPUT_DIR = baseDir + File.separator + "error";

			String entryName = unZipFile.getEntryName();
			ERR_OUTPUT_NAME = entryName + "_" + cTimeStr;

			ERR_FILE_DIR = ERR_OUTPUT_DIR + File.separator + ERR_OUTPUT_NAME;
			createFileDirectory(ERR_FILE_DIR);

			if (cIdx == 0) { // 수치지도 basic
				String fileDir = unZipFile.getUpzipPath();
				isSuccess = executorValidate(fileDir, validateLayerTypeList, epsg, attrFilterArry, stateFilterArry,
						langType);
			} else {
				// files
				QAFileParser parser = new QAFileParser(epsg, cIdx, support, unZipFile, neatLineCode);
				boolean parseTrue = parser.isTrue();
				if (!parseTrue) {
					comment += parser.getFileState();
					if (!comment.equals("")) {
						// logger.info(comment);
						System.out.println(comment);
					}
					return isSuccess;
				}

				DTLayerCollectionList collectionList = parser.getCollectionList();
				if (collectionList == null) {
					// 파일 다 에러
					comment += parser.getFileState();
					if (!comment.equals("")) {
						// logger.info(comment);
						System.out.println(comment);
					}
					return isSuccess;
				} else {
					// 몇개만 에러
					comment += parser.getFileState();
					if (!comment.equals("")) {
						// logger.info(comment);
						System.out.println(comment);
					}
				}
				// excute validation
				isSuccess = executorValidate(collectionList, validateLayerTypeList, epsg, langType);
				parser = null;
				collectionList = null;
			}
			if (isSuccess) {
				pb.terminate();// Process 완료
			} else {
				pb.terminate();// Process 완료
			}
			validateTypeParser = null;
			validateLayerTypeList = null;
			unZipFile = null;
			return isSuccess;
		}
	}

	private boolean executorValidate(String fileDir, QALayerTypeList validateLayerTypeList, String epsg,
			JSONArray attrFilter, JSONArray stateFilter, LangType langType) throws SchemaException {
		// 콘솔창에 로그 안찍히게 하기
		org.geotools.util.logging.Logging.getLogger("org").setLevel(Level.OFF);

		pb = new Progress(epsg, attrFilter, stateFilter, langType);
		pb.countOpenTotalTask(fileDir, validateLayerTypeList);
		pb.startProgress();

		boolean isSuccess = false;
		try {
			OpenCollectionValidator validator = new OpenCollectionValidator(fileDir, validateLayerTypeList, epsg,
					attrFilter, stateFilter, langType);

			long time = System.currentTimeMillis();
			SimpleDateFormat dayTime = new SimpleDateFormat("yyMMdd_HHmmss");
			String cTimeStr = dayTime.format(new Date(time));
			String fileName = ERR_FILE_DIR + "\\" + cTimeStr;

			isSuccess = writeErrShp(epsg, validator.collectionAttributeValidate(), fileName + "_attribute_err.shp",
					"Attribute", langType);
			isSuccess = writeErrShp(epsg, validator.collectionGraphicValidate(), fileName + "_graphic_err.shp",
					"Graphic", langType);
		} catch (IOException e) {
			if (langType.getLang().equals("ko")) {
				System.out.println("검수 요청이 실패했습니다.");
			} else if (langType.getLang().equals("en")) {
				System.out.println("Request failed");
			}
		}
		return isSuccess;
	}

	private boolean executorValidate(DTLayerCollectionList collectionList, QALayerTypeList validateLayerTypeList,
			String epsg, LangType langType) {
		// 콘솔창에 로그 안찍히게 하기
		org.geotools.util.logging.Logging.getLogger("org").setLevel(Level.OFF);

		// 도엽별 검수 쓰레드 생성
		List<Future<?>> futures = new ArrayList<>();
		ExecutorService execService = Executors.newFixedThreadPool(3,
				new ThreadFactoryBuilder().setNameFormat("도엽별 검수-%d").build());

		pb = new Progress(langType);
		for (final DTLayerCollection collection : collectionList) {
			pb.countTotalTask(validateLayerTypeList, collection,
					collectionList.getCloseLayerCollections(collection.getMapRule()));
		}
		pb.startProgress();

		for (final DTLayerCollection collection : collectionList) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					CollectionValidator validator = null;
					try {
						DTLayerCollectionList closeCollections = collectionList
								.getCloseLayerCollections(collection.getMapRule());
						validator = new CollectionValidator(collection, closeCollections, validateLayerTypeList, langType);
					} catch (Exception e) {
						e.printStackTrace();
					}
					writeErrShp(epsg, validator);
				}
			};

			Future<?> future = execService.submit(runnable);
			futures.add(future);
		}
		// final long totalAmount = collectionList.getAllLayerSize();
		int futureCount = 0;

		for (int i = 0; i < futures.size(); i++) {
			Future<?> tmp = futures.get(i);
			try {
				tmp.get();
				futureCount++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		execService.shutdown();
		pb.terminate();
		// System.out.println("검수가 완료되었습니다.");
		return futureCount == collectionList.size();
	}

	private boolean writeErrShp(String epsg, CollectionValidator validator) {
		try {
			// 오류레이어 발행
			ErrorLayer errLayer = validator.getErrLayer();
			int errSize = errLayer.getErrFeatureList().size();
			if (errSize > 0) {
				SHPFileWriter.writeSHP(epsg, errLayer, ERR_FILE_DIR + "\\" + errLayer.getCollectionName() + "_err.shp");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean writeErrShp(String epsg, ErrorLayer errLayer, String fileName, String qaType, LangType langType) {
		try {
			// 오류레이어 발행
			if (errLayer != null) {
				int errSize = errLayer.getErrFeatureList().size();
				if (errSize > 0) {
					SHPFileWriter.writeSHP(epsg, errLayer, fileName);
				}
			}
			if (langType.getLang().equals("ko")) {
				System.out.println(qaType + " 완료.");
			} else if (langType.getLang().equals("en")) {
				System.out.println(qaType + " Success.");
			}
			return true;
		} catch (Exception e) {
			if (langType.getLang().equals("ko")) {
				System.out.println(qaType + " 실패.");
			} else if (langType.getLang().equals("en")) {
				System.out.println(qaType + " Fail.");
			}
			return false;
		}
	}

	private void createFileDirectory(String directory) {
		File file = new File(directory);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 폴더 내에 폴더가 있을시 하위 폴더 탐색
	 * 
	 * @author SG.Lee <<<<<<< HEAD
	 * @since 2018. 4. 18. 오전 9:09:33
	 * @param source void =======
	 * @Date 2018. 4. 18. 오전 9:09:33
	 * @param source void >>>>>>> open
	 */
	@SuppressWarnings("unused")
	private void subDirList(String source) {
		File dir = new File(source);

		File[] fileList = dir.listFiles();
		List<File> indexFiles = new ArrayList<File>();

		for (int i = 0; i < fileList.length; i++) {
			File file = fileList[i];

			if (file.isFile()) {
				String filePath = file.getPath();
				String fFullName = file.getName();

				int Idx = fFullName.lastIndexOf(".");
				String _fileName = fFullName.substring(0, Idx);

				String parentPath = file.getParent(); // 상위 폴더 경로

				if (_fileName.endsWith("index")) {
					indexFiles.add(fileList[i]);// 도곽파일 리스트 add(shp,shx...)
				} else {
					if (_fileName.contains(".")) {
						moveDirectory(_fileName.substring(0, _fileName.lastIndexOf(".")), fFullName, filePath,
								parentPath);
					} else {
						moveDirectory(_fileName, fFullName, filePath, parentPath);
					}
				}
			}
		}

		fileList = dir.listFiles();
		// 도엽별 폴더 생성후 도곽파일 이동복사
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) {
				String message = "[디렉토리] ";
				message = fileList[i].getName();
				System.out.println(message);
				for (File iFile : indexFiles) {
					try {
						FileNio2Copy(iFile.getPath(), fileList[i].getPath() + File.separator + iFile.getName());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.getMessage();
					}
				}
			}
		}

		// index파일 삭제
		for (File iFile : indexFiles) {
			iFile.delete();
		}

		// 파일 사용후 객체초기화
		fileList = null;
		indexFiles = null;
	}

	/**
	 * 파일이동
	 * 
	 * @author SG.Lee
	 * @since 2018. 4. 18. 오전 9:46:27
	 * @param folderName
	 * @param fileName
	 * @param beforeFilePath
	 * @param afterFilePath
	 * @return String
	 */
	private String moveDirectory(String folderName, String fileName, String beforeFilePath, String afterFilePath) {
		String path = afterFilePath + "/" + folderName;
		String filePath = path + "/" + fileName;

		File dir = new File(path);

		if (!dir.exists()) { // 폴더 없으면 폴더 생성
			dir.mkdirs();
		}

		try {
			File file = new File(beforeFilePath);

			if (file.renameTo(new File(filePath))) { // 파일 이동
				return filePath; // 성공시 성공 파일 경로 return
			} else {
				return null;
			}
		} catch (Exception e) {
			e.getMessage();
			return null;
		}
	}

	/**
	 * 파일복사
	 * 
	 * @author SG.Lee
	 * @since 2018. 4. 18. 오전 9:45:55
	 * @param source
	 * @param dest
	 * @throws IOException void
	 */
	private void FileNio2Copy(String source, String dest) throws IOException {
		Files.copy(new File(source).toPath(), new File(dest).toPath());
	}

}
