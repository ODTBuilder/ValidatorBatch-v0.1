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
package com.git.gdsbuilder.type.validate.option.type;

import com.git.gdsbuilder.type.validate.option.en.LangType;

/**
 * 수치지도 검수 항목 20가지에 대한 영문 검수 항목명, 한글 검수 항목명, 오류 타입 등의 정보를 저장하는 Enum 클래스
 * 
 * @author DY.Oh
 *
 */

public class OpenDMQAOptions {

	public enum QAType {

		SMALLLENGTH("SmallLength", "Segments between length tolerance limit", "허용범위이하길이오류", "GraphicError", "그래픽오류"),
		SMALLAREA("SmallArea", "Areas between tolerance limit", "허용범위이하면적오류", "GraphicError", "그래픽오류"),
		FIXEDVALUE("FixedValue", "Overlapping features", "문자의정확성", "AttributeError", "속성오류"),
		SELFENTITY("SelfEntity", "Overlapping features", "단독존재오류", "GraphicError", "그래픽오류"),
		ATTRIBUTEFIXMISS("AttributeFixMiss", "Feature with wrong attribute field", "필수속성오류", "AttributeError", "속성오류"),
		ENTITYDUPLICATED("EntityDuplicated", "Duplicated features", "요소중복오류", "GraphicError", "그래픽오류"),
		CONOVERDEGREE("ConOverDegree", "Unsmooth contour line curves", "등고선꺾임오류", "GraphicError", "그래픽오류"),
		CONINTERSECTED("ConIntersected", "Contour line intersections", "등고선교차오류", "GraphicError", "그래픽오류"),
		CONBREAK("ConBreak", "Contour line disconnections", "등고선끊김오류", "GraphicError", "그래픽오류"),
		USELESSPOINT("UselessPoint", "Useless points in contour line", "직선화미처리오류", "GraphicError", "그래픽오류"),
		ZVALUEAMBIGUOUS("ZValueAmbiguous", "Wrong elevation", "고도값오류", "AttributeError", "속성오류"),
		POINTDUPLICATED("PointDuplicated", "Duplicated point", "중복점오류", "GraphicError", "그래픽오류"),
		OUTBOUNDARY("OutBoundary", "Feature crossing the boundary", "경계초과오류", "GraphicError", "그래픽오류"),
		NODEMISS("NodeMiss", "Missing node", "선형노드오류", "GraphicError", "그래픽오류"),
		REFENTITYNONE("RefEntityNone", "Missing adjacent feature", "인접요소부재오류", "AdjacentError", "인접오류"),
		REFATTRIBUTEMISS("RefAttributeMiss", "Missing attribute of adjacent features", "인접요소속성오류", "AdjacetError",
				"인접오류"),
		LAYERMISS("LayerMiss", "Feature with wrong geometry type", "계층오류", "GraphicError", "그래픽오류"),
		OVERSHOOT("Overshoot", "Feature crossing the sheet", "기준점초과오류", "GraphicError", "그래픽오류"),
		ENTITYTWISTED("EntityTwisted", "Twisted entity", "폴리곤꼬임오류", "GraphicError", "그래픽오류"),
		ATTRIBUTEMISS("AttributeMiss", "Feature with wrong attribute value", "속성오류", "AttributeError", "속성오류");

		/**
		 * 검수옵션 생성 및 오류 레이어 생성 시 사용되는 에러코드
		 */
		String errCode;
		/**
		 * 검수 항목 명(영문)
		 */
		String errNameE;
		/**
		 * 검수 항목 명(한글)
		 */
		String errName;
		/**
		 * 오류 타입(영문)
		 */
		String errTypeE;
		/**
		 * 오류 타입(한글)
		 */
		String errType;

		private QAType(String errCode, String errNameE, String errName, String errTypeE, String errType) {
			this.errCode = errCode;
			this.errNameE = errNameE;
			this.errName = errName;
			this.errTypeE = errTypeE;
			this.errType = errType;
		}

		public String getErrCode() {
			return errCode;
		}

		public String getErrType(LangType langType) {

			String type = null;
			if (langType.getLang().equals("ko")) {
				type = errType;
			} else if (langType.getLang().equals("en")) {
				type = errTypeE;
			}
			return type;
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

		public static String getName(String optionName, LangType langType) {

			String name = null;
			for (QAType type : values()) {
				if (optionName.contains(type.errCode)) {
					name = type.getErrName(langType);
					break;
				}
			}
			return name;
		}
	}

}
