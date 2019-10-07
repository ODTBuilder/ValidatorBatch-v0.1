package com.git.gdsbuilder.type.validate.option.en;

public enum LangType {

	EN("en"), KO("ko");

	String lang;

	private LangType(String lang) {
		this.lang = lang;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public static LangType getLang(String lang) {

		for (LangType langType : values()) {
			if (lang.contains(langType.lang)) {
				return langType;
			}
		}
		throw new IllegalArgumentException("No matching constant for [" + lang + "]");
	}
}
