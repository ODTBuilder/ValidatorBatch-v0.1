/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.type;

import com.git.gdsbuilder.type.validate.option.en.LangType;

/**
 * @className NFMQAOptions.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 15. 오전 11:25:05
 */
public class DMQAOptions {

	public enum Type {

		CONBREAK("ConBreak", "Contour line disconnections", "등고선끊김오류", "GraphicError", "그래픽오류"),
		CONINTERSECTED("ConIntersected", "Contour line intersections", "등고선교차오류", "GraphicError", "그래픽오류"),
		CONOVERDEGREE("ConOverDegree", "Unsmooth contour line curves", "등고선꺾임오류", "GraphicError", "그래픽오류"),
		USELESSPOINT("UselessPoint", "Useless points in contour line", "직선화미처리오류", "GraphicError", "그래픽오류"),
		SMALLAREA("SmallArea", "Areas between tolerance limit", "허용범위이하면적오류", "GraphicError", "그래픽오류"),
		SMALLLENGTH("SmallLength", "Segments between length tolerance limit", "허용범위이하길이오류", "GraphicError", "그래픽오류"),
		OVERSHOOT("Overshoot", "Feature crossing the sheet", "기준점초과오류", "GraphicError", "그래픽오류"),
		SELFENTITY("SelfEntity", "Overlapping features", "단독존재오류", "GraphicError", "그래픽오류"),
		OUTBOUNDARY("OutBoundary", "Feature crossing the boundary", "경계초과오류", "GraphicError", "그래픽오류"),
		ENTITYDUPLICATED("EntityDuplicated", "Duplicated features", "요소중복오류", "GraphicError", "그래픽오류"),
		ENTITYOPENMISS("EntityOpenMiss", "Unclosed feature", "객체폐합오류", "GraphicError", "그래픽오류"),
		TWISTEDPOLYGON("TwistedPolygon", "Twisted polygons", "폴리곤꼬임오류", "GraphicError", "그래픽오류"),
		NODEMISS("NodeMiss", "Missing node", "선형노드오류", "GraphicError", "그래픽오류"),
		POINTDUPLICATED("PointDuplicated", "Duplicated point", "중복점오류", "GraphicError", "그래픽오류"),
		ONEACRE("OneAcre", "Mismatching farmland size (Total)", "지류계오류", "GraphicError", "그래픽오류"),
		ONESTAGE("OneStage", "Excluded farmland (Part)", "경지계오류", "GraphicError", "그래픽오류"),
		BUILDINGSITEMISS("BuildingSiteMiss", "Land and facility mismatch", "건물부지오류", "GraphicError", "그래픽오류"),
		BOUNDARYMISS("BoundaryMiss", "Missing boundary", "경계누락오류", "GraphicError", "그래픽오류"),
		CENTERLINEMISS("CenterLineMiss", "Missing center line", "중심선누락오류", "GraphicError", "그래픽오류"),
		ENTITYINHOLE("EntityInHole", "Hole with entity", "홀(Hole)중복오류", "GraphicError", "그래픽오류"),
		HOLEMISPLACEMENT("HoleMisPlaceMent", "Hole misplacement", "홀(Hole)존재오류", "GraphicError", "그래픽오류"),
		LINEARDISCONNECTION("LinearDisconnection", "Linear disconnection", "선형단락오류", "GraphicError", "그래픽오류"),
		MULTIPART("MultiPart", "Selection of wrong multiple parts", "다중객체존재오류", "GraphicError", "그래픽오류"),
		SYMBOLOUT("SymbolOut", "Symbol misplacement", "심볼단독존재오류", "GraphicError", "그래픽오류"),

		ZVALUEAMBIGUOUS("ZValueAmbiguous", "Wrong elevation", "고도값오류", "AttributeError", "속성오류"),
		BRIDGENAME("BridgeName", "Wrong bridge name", "교량명오류", "AttributeError", "속성오류"),
		ADMINMISS("AdminMiss", "Administrative boundary mismatch", "행정명오류", "AttributeError", "속성오류"),
		NUMERICALVALUE("NumericalValue", "Wrong numerical value", "수치값오류", "AttributeError", "속성오류"),
		UFIDMISS("UFIDMiss", "Missing UFID", "UFID오류", "AttributeError", "속성오류"),

		REFENTITYNONE("DRefEntityNone", "Missing adjacent feature", "인접요소부재오류", "AdjacentError", "인접오류"),
		REFATTRIBUTEMISS("RefAttributeMiss", "Missing attribute of adjacent features", "인접요소속성오류", "AdjacentError",
				"인접오류"),
		REFZVALUEMISS("RefZValueMiss", "Wrong elevation of adjacent feature", "인접요소고도값오류", "AdjacentError", "인접오류");

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
