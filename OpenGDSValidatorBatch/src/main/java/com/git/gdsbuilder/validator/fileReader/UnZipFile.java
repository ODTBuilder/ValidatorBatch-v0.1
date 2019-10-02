package com.git.gdsbuilder.validator.fileReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

import com.git.gdsbuilder.file.FileMeta;
import com.git.gdsbuilder.file.FileMetaList;


/**
 * @className UnZipFile.java
 * @description 압축파일를 지정경로에 압축을 푸는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 1:50:27
 */
public class UnZipFile {

	private final static String forestFName = "forest";

	private String fileState = "";
	private String brTag = "<br>";

	int totalFileSize = 0;
	
	String fileName;
	String entryName;
	String fileDirectory;
	Map<String, Object> fileNames;

	String upzipPath;
	Map<String, FileMetaList> dirMetaList = new HashMap<>();
	FileMetaList fileMetaList = new FileMetaList();

	List<String> failsNames;
	boolean isDir;
	boolean isFiles;

	public UnZipFile(String upzipPath) {
		this.upzipPath = upzipPath;
		this.isDir = false;
		this.isFiles = false;
	}

	public void decompress(File zipFile, int catetoryIdx) throws Throwable {

		List<String> faisFiles = new ArrayList<>();

		int BUFFER_SIZE = 4096;
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry zipentry = null;
		try {
			// 디렉토리생성
			String zipFileName = zipFile.getName(); // 파일 이름.확장자
			this.fileName = zipFileName;
			int comma = zipFileName.lastIndexOf(".");
			this.entryName = zipFileName.substring(0, comma); // 파일 이름
			FileUtils.forceMkdir(new File(upzipPath));
			// 파일 스트림
			fis = new FileInputStream(zipFile);
			// Zip 파일 스트림
			zis = new ZipInputStream(new BufferedInputStream(fis, BUFFER_SIZE), Charset.forName("euc-kr"));
			// Fentry가 없을때까지 뽑기
			while ((zipentry = zis.getNextEntry()) != null) {
				String zipentryName = zipentry.getName();
				File file = new File(upzipPath, zipentryName);
				// entiry가 폴더면 폴더 생성
				if (zipentry.isDirectory()) {
					file.mkdirs();
				} else {
					// 파일이면 파일 만들기
					boolean isTrue = createFile(file, zis);
					if (!isTrue) {
						faisFiles.add(file.getName());
					}
				}
			}
			if (faisFiles.size() > 0) {
				failsNames = faisFiles;
			}

			if (catetoryIdx == 5) {
				File unzipFolder = new File(this.upzipPath);
				createCollectionFolders(unzipFolder);
			}
			getFilMeta(this.upzipPath);
			this.fileDirectory = this.upzipPath + File.separator + entryName;
		} catch (Throwable e) {
			// LOGGER.info(e.getMessage());
		} finally {
			if (zis != null)
				zis.close();
			if (fis != null)
				fis.close();
		}
	}

	/**
	 * 폴더 내에 폴더가 있을시 하위 폴더 탐색
	 * 
	 * @author SG.Lee
	 * @since 2018. 4. 18. 오전 9:09:33
	 * @param source
	 *            void
	 */
	@SuppressWarnings("unused")
	private static void subDirList(String source) {
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
	 * 임상도 폴더 재생성
	 * 
	 * @author SG.Lee
	 * @since 2018. 4. 18. 오후 1:24:16
	 * @param unzipFolder
	 *            void
	 */
	private static File[] createCollectionFolders(File unzipFolder) {
		// boolean equalFlag = false; // 파일명이랑 압축파일명이랑 같을시 대비 flag값
		// String unzipName = unzipFolder.getName();
		String unzipPath = unzipFolder.getPath();
		if (unzipFolder.exists() == false) {
			System.out.println("경로가 존재하지 않습니다");
		}

		File[] fileList = unzipFolder.listFiles();
		List<File> indexFiles = new ArrayList<File>();
		String parentPath = unzipFolder.getParent(); // 상위 폴더 경로

		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) { 		
				/*
				 * String message = "[디렉토리] "; message = fileList[ i
				 * ].getName(); System.out.println( message );
				 * 
				 * subDirList( fileList[ i ].getPath());//하위 폴더 탐색
				 */ } else {
				String filePath = fileList[i].getPath();
				String fFullName = fileList[i].getName();
				int Idx = fFullName.lastIndexOf(".");
				String _fileName = fFullName.substring(0, Idx);
				String ext = fFullName.substring(Idx + 1);
				/*
				 * if (_fileName.equals(unzipName)) { equalFlag = true; }
				 */

				if (_fileName.endsWith("index")) {
					indexFiles.add(fileList[i]);// 도곽파일 리스트 add(shp,shx...)
				} else {
					if (_fileName.contains(".")) {
						moveDirectory(_fileName.substring(0, _fileName.lastIndexOf(".")), forestFName + "." + ext,
								filePath, unzipPath);
					} else {
						moveDirectory(_fileName, forestFName + "." + ext, filePath, unzipPath);
					}
				}
			}
		}

		fileList = unzipFolder.listFiles();

		// 도엽별 폴더 생성후 도곽파일 이동복사
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) {
				for (File iFile : indexFiles) {
					try {
						FileNio2Copy(iFile.getPath(), fileList[i].getPath() + File.separator + iFile.getName());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println(e.getMessage());
					}
				}
			}
		}

		// index파일 삭제
		for (File iFile : indexFiles) {
			iFile.delete();
		}

		// 원래 폴더 삭제
		/*
		 * if (!equalFlag) { unzipFolder.delete(); }
		 */

		// 파일 사용후 객체초기화
		fileList = null;
		indexFiles = null;

		return new File(parentPath).listFiles();
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
	private static String moveDirectory(String folderName, String fileName, String beforeFilePath,
			String afterFilePath) {
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
	 * @throws IOException
	 *             void
	 */
	private static void FileNio2Copy(String source, String dest) throws IOException {
		Files.copy(new File(source).toPath(), new File(dest).toPath());
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 2. 21. 오후 4:28:42
	 * @param fileDirectory
	 * @param type
	 * @decription
	 */
	private void getFilMeta(String fileDirectory) {

		File outputFile = new File(fileDirectory);
		File[] subDirs = outputFile.listFiles();
		for (File subDir : subDirs) {
			if (subDir.isDirectory()) {
				FileMetaList fileMetaList = new FileMetaList();
				// shp
				isDir = true;
				File[] dirs = subDir.listFiles();
				for (File file : dirs) {
					totalFileSize++;
					String filePath = file.getPath();
					String fileName = file.getName();
					FileMeta fileMeta = new FileMeta();
					fileMeta.setFileName(fileName);
					fileMeta.setFilePath(filePath);
					fileMeta.setTrue(true);
					if (fileName.endsWith("ngi") || fileName.endsWith("nda") || fileName.endsWith("dxf")
							|| filePath.endsWith("shp") || filePath.endsWith("shx") || filePath.endsWith("dbf")
							|| filePath.endsWith("prj") || filePath.endsWith("sbn") || filePath.endsWith("sbx")
							|| filePath.endsWith("xml")) {
						if (fileName.endsWith(".ngi")) {
							fileMeta.setFileType("ngi");
							fileMetaList.add(fileMeta);
						} else if (fileName.endsWith(".dxf")) {
							fileMeta.setFileType("dxf");
							fileMetaList.add(fileMeta);
						} else if (fileName.endsWith(".shp")) {
							fileMeta.setFileType("shp");
							fileMetaList.add(fileMeta);
						}
					}
				}
				dirMetaList.put(subDir.getPath(), fileMetaList);
			} else {
				totalFileSize++;
				fileMetaList.setName(subDir.getParent());
				// other
				isFiles = true;
				String filePath = subDir.getPath();
				String fileName = subDir.getName();

				FileMeta fileMeta = new FileMeta();
				fileMeta.setFileName(fileName);
				fileMeta.setFilePath(filePath);
				fileMeta.setTrue(true);

				if (fileName.endsWith("ngi") || fileName.endsWith("nda") || fileName.endsWith("dxf")
						|| fileName.endsWith("shp") || fileName.endsWith("shx") || fileName.endsWith("dbf")
						|| fileName.endsWith("prj") || fileName.endsWith("sbn") || fileName.endsWith("sbx")
						|| fileName.endsWith("xml")) {
					if (fileName.endsWith(".ngi")) {
						fileMeta.setFileType("ngi");
						fileMetaList.add(fileMeta);
					} else if (fileName.endsWith(".dxf")) {
						fileMeta.setFileType("dxf");
						fileMetaList.add(fileMeta);
					} else if (fileName.endsWith(".shp")) {
						fileMeta.setFileType("shp");
						fileMetaList.add(fileMeta);
					}
				}
			}
		}
	}

	private boolean createFile(File file, ZipInputStream zis) throws Throwable {

		// 디렉토리 확인
		File parentDir = new File(file.getParent());
		// 디렉토리가 없으면 생성하자
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}
		// 파일 스트림 선언
		boolean isTrue = false;
		try (FileOutputStream fos = new FileOutputStream(file)) {
			byte[] buffer = new byte[256];
			int size = 0;
			// Zip스트림으로부터 byte뽑아내기
			while ((size = zis.read(buffer)) > 0) {
				// byte로 파일 만들기
				fos.write(buffer, 0, size);
			}
			fos.close();
			isTrue = true;
			
		} catch (Exception e) {
			isTrue = false;
		}
		return isTrue;
	}
	
	public int getTotalSize(){
		return this.totalFileSize;
	}
/*
 * 
	private String getFileName(String filePath) {
		String[] splitArr = filePath.split("\\\\");
		String fileName = splitArr[splitArr.length - 1];
		int pos = fileName.lastIndexOf(".");
		return fileName.substring(0, pos);
	}
 */

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

	public static String getForestfname() {
		return forestFName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public String getFileDirectory() {
		return fileDirectory;
	}

	public void setFileDirectory(String fileDirectory) {
		this.fileDirectory = fileDirectory;
	}

	public Map<String, Object> getFileNames() {
		return fileNames;
	}

	public void setFileNames(Map<String, Object> fileNames) {
		this.fileNames = fileNames;
	}

	public String getUpzipPath() {
		return upzipPath;
	}

	public void setUpzipPath(String upzipPath) {
		this.upzipPath = upzipPath;
	}

	public Map<String, FileMetaList> getDirMetaList() {
		return dirMetaList;
	}

	public void setDirMetaList(Map<String, FileMetaList> dirMetaList) {
		this.dirMetaList = dirMetaList;
	}

	public FileMetaList getFileMetaList() {
		return fileMetaList;
	}

	public void setFileMetaList(FileMetaList fileMetaList) {
		this.fileMetaList = fileMetaList;
	}

	public List<String> getFailsNames() {
		return failsNames;
	}

	public void setFailsNames(List<String> failsNames) {
		this.failsNames = failsNames;
	}

	public boolean isDir() {
		return isDir;
	}

	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}

	public boolean isFiles() {
		return isFiles;
	}

	public void setFiles(boolean isFiles) {
		this.isFiles = isFiles;
	}
	
}
