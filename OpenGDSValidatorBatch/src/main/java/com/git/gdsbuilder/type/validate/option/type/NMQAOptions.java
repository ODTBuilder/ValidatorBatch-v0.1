/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.type;

/**
 * @className NFMQAOptions.java
 * @description 
 * @author DY.Oh
 * @since 2018. 3. 15. 오전 11:25:05
 * */
public class NMQAOptions {

	public enum Type {

		CONBREAK("ConBreak", "등고선끊김오류","그래픽오류"), 
		CONINTERSECTED("ConIntersected", "등고선교차오류","그래픽오류"), 
		CONOVERDEGREE("ConOverDegree", "등고선꺾임오류","그래픽오류"), 
		USELESSPOINT("UselessPoint", "직선화미처리오류","그래픽오류"), 
		SMALLAREA("SmallArea", "허용범위이하면적오류","그래픽오류"), 
		SMALLLENGTH("SmallLength", "허용범위이하길이오류","그래픽오류"), 
		OVERSHOOT("Overshoot", "기준점초과오류","그래픽오류"), 
		SELFENTITY("SelfEntity", "단독존재오류","그래픽오류"), 
		OUTBOUNDARY("OutBoundary", "경계초과오류","그래픽오류"), 
		ENTITYDUPLICATED("EntityDuplicated", "요소중복오류","그래픽오류"), 
		ENTITYOPENMISS("EntityOpenMiss", "객체폐합오류","그래픽오류"), 
		TWISTEDPOLYGON("TwistedPolygon", "폴리곤꼬임오류","그래픽오류"), 
		NODEMISS("NodeMiss", "선형노드오류","그래픽오류"), 
		POINTDUPLICATED("PointDuplicated", "중복점오류","그래픽오류"), 
		ONEACRE("OneAcre", "지류계오류","그래픽오류"), 
		ONESTAGE("OneStage", "경지계오류","그래픽오류"), 
		BUILDINGSITEMISS("BuildingSiteMiss", "건물부지오류","그래픽오류"), 
		BOUNDARYMISS("BoundaryMiss", "경계누락오류","그래픽오류"), 
		CENTERLINEMISS("CenterLineMiss", "중심선누락오류","그래픽오류"), 
		ENTITYINHOLE("EntityInHole", "홀(Hole)중복오류","그래픽오류"), 
		HOLEMISPLACEMENT("HoleMisPlaceMent", "홀(Hole)존재오류","그래픽오류"), 
		LINEARDISCONNECTION("LinearDisconnection", "선형단락오류","그래픽오류"), 
		MULTIPART("MultiPart", "다중객체존재오류","그래픽오류"), 
		SYMBOLOUT("SymbolOut", "심볼45단독존재오류","그래픽오류"), 
		
		ZVALUEAMBIGUOUS("ZValueAmbiguous", "고도값오류","속성오류"), 
		BRIDGENAME("BridgeName", "교량명오류","속성오류"), 
		ADMINMISS("AdminMiss", "행정명오류","속성오류"), 
		NUMERICALVALUE("NumericalValue", "수치값오류","속성오류"), 
		UFIDMISS("UFIDMiss", "UFID오류","속성오류"), 
		
		REFENTITYNONE("RefEntityNone", "인접요소부재오류","인접오류"), 
		REFATTRIBUTEMISS("RefAttributeMiss", "인접요소속성오류","인접오류"), 
		REFZVALUEMISS("RefZValueMiss", "인접요소고도값오류","인접오류"); 

		
		String errNameE;
		String errName;
		String errType;

		private Type(String errNameE, String errName, String errType) {
			this.errNameE = errNameE;
			this.errName = errName;
			this.errType = errType;
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

	}
	
}
