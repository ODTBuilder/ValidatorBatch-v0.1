package com.git.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.git.batch.domain.BatchArgs;
import com.git.batch.service.BathService;
import com.git.gdsbuilder.type.validate.option.en.LangType;

/**
 * <<<<<<< HEAD 검수 수행 배치파일 Main 클래스
 * 
 * @author SG.Lee
 * @since 2018. 5. 14. 오후 3:48:11
 */
public class App {

	static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws Throwable {

		BathService service = new BathService();
		boolean flag = false;
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
			String langStr = params.getLangType();

			LangType langType = LangType.getLang(langStr);
			if (langType.getLang().equals("ko")) {
				System.out.println("\n검수를 진행합니다.");
			} else if (langType.getLang().equals("en")) {
				System.out.println("\nVerification is in progress.");
			}

			try {
				flag = service.validate(baseDir, valType, pFlag, valDType, fileType, category, layerDefPath, valOptPath,
						objFilePath, crs, langType);
				if (flag) {
					if (langType.getLang().equals("ko")) {
						System.out.println("검수를 완료하였습니다.");
					} else if (langType.getLang().equals("en")) {
						System.out.println("Request successful");
					}
					System.exit(200);
				} else {
					if (langType.getLang().equals("ko")) {
						System.out.println("검수를 실패하였습니다.");
					} else if (langType.getLang().equals("en")) {
						System.out.println("Request failed");
					}
					System.exit(500);
				}
			} catch (Throwable e) {
				if (langType.getLang().equals("ko")) {
					System.out.println("검수를 실패하였습니다.");
				} else if (langType.getLang().equals("en")) {
					System.out.println("Request failed");
				}
				System.exit(500);
			}
		} catch (ParameterException e) {
			JCommander.getConsole().println(e.toString());
			cmd.usage();
			System.exit(500);
		}
	}
}
