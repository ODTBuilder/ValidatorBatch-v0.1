/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.type;

import com.git.gdsbuilder.type.validate.option.en.LangType;

/**
 * @className UFMQAOptions.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 15. 오전 11:24:38
 */
public class UFMQAOptions {

	public enum Type {

		UAVRGDPH10("UAvrgDPH10", "Wrong mean depth(Graphic) (Underground)", "평균심도오류(정위치)", "GraphicError", "그래픽오류"),
		ULEADERLINE("ULeaderline", "Leader line overlapping (Underground)", "지시선교차오류", "GraphicError", "그래픽오류"),
		UNODEMISS("UNodeMiss", "Missing node on line (Underground)", "시설물선형노드오류", "GraphicError", "그래픽오류"),
		SYMBOLINLINE("SymbolInLine", "Missing symbol on line (Underground)", "선형내심볼미존재오류", "GraphicError", "그래픽오류"),
		LINECROSS("LineCross", "Crossing pipes (Underground)", "관로상하월오류", "GraphicError", "그래픽오류"),
		SYMBOLSDISTANCE("SymbolsDistance", "Distance between symbols (Underground)", "심볼간격오류", "GraphicError", "그래픽오류"),
		USYMBOLOUT("USymbolOut", "Symbol misplacement (Underground)", "심볼단독존재오류", "GraphicError", "그래픽오류"),

		UAVRGDPH20("UAvrgDPH20", "Wrong mean depth(Attribute) (Underground)", "평균심도오류(구조화)", "AttributeError", "속성오류"),
		SYMBOLDIRECTION("SymbolDirection", "Mismatching direction of symbol (Underground)", "시설물심볼방향오류",
				"AttributeError", "속성오류");

		String errCode;
		String errNameE;
		String errName;
		String errTypeE;
		String errType;

		private Type(String errCode, String errNameE, String errName, String errTypeE, String errType) {
			this.errCode = errCode;
			this.errNameE = errNameE;
			this.errName = errName;
			this.errTypeE = errTypeE;
			this.errType = errType;
		}

		public String getErrCode() {
			return errCode;
		}

		public void setErrCode(String errCode) {
			this.errCode = errCode;
		}

		public String getErrNameE() {
			return errNameE;
		}

		public void setErrNameE(String errNameE) {
			this.errNameE = errNameE;
		}

		public String getErrName() {
			return errName;
		}

		public String getErrName(LangType langType) {

			String name = null;
			if (langType.getLang().equals("ko")) {
				name = errName;
			} else if (langType.getLang().equals("en")) {
				name = errNameE;
			}
			return name;
		}

		public void setErrName(String errName) {
			this.errName = errName;
		}

		public String getErrType() {
			return errType;
		}

		public void setErrType(String errType) {
			this.errType = errType;
		}

		public String getErrTypeE() {
			return errTypeE;
		}

		public void setErrTypeE(String errTypeE) {
			this.errTypeE = errTypeE;
		}
	}

}
