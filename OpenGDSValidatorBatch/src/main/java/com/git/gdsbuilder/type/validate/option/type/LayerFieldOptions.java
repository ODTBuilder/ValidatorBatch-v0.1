/**
 * 
 */
package com.git.gdsbuilder.type.validate.option.type;

/**
 * @className LayerFieldOptions.java
 * @description
 * @author DY.Oh
 * @since 2018. 4. 2. 오전 10:14:08
 */
public class LayerFieldOptions {
	
	public enum Type {

	LAYERFIELDFIXMISS("LayerFixMiss", "Feature with wrong attribute value", "필드구조오류", "LayerError", "레이어오류"),
	LAYERTYPEFIXMISS("LayerFixMiss", "Feature with wrong geometry type", "Geometry타입오류","LayerError", "레이어오류");
		
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
