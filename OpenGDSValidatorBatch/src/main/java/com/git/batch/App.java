package com.git.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.git.batch.domain.BatchArgs;
import com.git.batch.service.BathService;

/**
 * 검수 수행 배치파일 Main 클래스
 * @author SG.Lee
 * @since 2018. 5. 14. 오후 3:48:11
 * */
public class App {
	
	/**
	 * 
	 */
	static Logger logger = LoggerFactory.getLogger(App.class);

	/**
	 * Main 함수
	 * @author SG.LEE
	 * @param args 검수수행을 위한 파라미터 {@link BatchArgs} 참고
	 */
	public static void main(String[] args){
		
		BathService service = new BathService();
		boolean flag = false;
		System.out.println("\n검수를 진행합니다.");
//		System.out.println("\nVerification is in progress.");
		
		BatchArgs params = new BatchArgs();
		JCommander cmd = new JCommander(params);
 
		try { // Parse given arguments
			cmd.parse(args);			
			
			String baseDir = params.getBaseDir();
			String valType = params.getValType();
			String pFlag = params.getpFlag();
			String valDType = params.getValDType();
			String fileType = params.getFileType();
			int category = Integer.parseInt(params.getcIdx());
			String layerDefPath = params.getLayerDefPath();
			String valOptPath = params.getValOptPath();
			String objFilePath = params.getObjFilePath();
			String crs = params.getCrs();

			try {
				flag = service.validate(baseDir, valType, pFlag, valDType, fileType, category, layerDefPath, valOptPath,
						objFilePath, crs);
				if (flag) {
					System.out.println("요청 성공");
//					System.out.println("Request successful");
					System.exit(200);
				} else {
					System.out.println("요청 실패");
//					System.out.println("Request failed");
					System.exit(500);
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
//				System.out.println(e.toString());
				System.out.println("요청 실패");
//				System.out.println("Request failed");
				System.exit(500);
			}

		} catch (ParameterException e) {
			JCommander.getConsole().println(e.toString());
			cmd.usage();
			System.exit(500);
		}
	}
}
