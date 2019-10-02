/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.type;

/**
 * @className FTMQAOptions.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 15. 오전 11:25:57
 */
public class FTMQAOptions {

	public enum Type {

		FCODELOGICALATTRIBUTE("FCodeLogicalAttribute", "Wrong F Code (Forest)", "FCode불일치", "AttributeError", "속성오류"), 
		FLABELLOGICALATTRIBUTE("FLabelLogicalAttribute","Wrong F Label (Forest)", "AttributeError", "Label불일치","속성오류"), 
		DISSOLVE("Dissolve", "Discord of adjacent attribute", "인접속성병합오류", "AttributeError", "속성오류"), 
		
		MULTIPART("MultiPart", "Selection of wrong multiple parts", "다중객체존재오류", "GraphicError", "그래픽오류"),
		SMALLAREA("SmallArea", "Areas under tolerance limit", "미세폴리곤존재오류", "GraphicError", "그래픽오류"),
		FENTITYINHOLE("FEntityInHole", "Holes in polygons", "홀(Hole)폴리곤존재오류","GraphicError", "그래픽오류"),
		SELFENTITY("SelfEntity", "Overlapping features", "단독존재오류","GraphicError", "그래픽오류");
		
		String errCode;
		String errNameE;
		String errName;
		String errTypeE;
		String errType;

		private Type(String errCode, String errNameE, String errName, String errTypeE,String errType) {
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

		public void setErrName(String errName) {
			this.errName = errName;
		}
		
		public String getErrTypeE() {
			return errTypeE;
		}

		public void setErrTypeE(String errTypeE) {
			this.errTypeE = errTypeE;
		}

		public String getErrType() {
			return errType;
		}

		public void setErrType(String errType) {
			this.errType = errType;
		}
		
		

	}

}
