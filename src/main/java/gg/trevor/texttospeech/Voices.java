package gg.trevor.texttospeech;

import lombok.Getter;

public enum Voices
{
	OFF(null),
	MALE_AMERICAN("cmu-bdl-hsmm"),
	MALE_AMERICAN_2("cmu-rms-hsmm"),
	FEMALE_AMERICAN("cmu-slt-hsmm"),
	MALE_BRITISH("dfki-spike-hsmm"),
	MALE_BRITISH_2("dfki-obadiah-hsmm"),
	FEMALE_BRITISH("dfki-prudence-hsmm"),
	FEMALE_BRITISH_2("dfki-poppy-hsmm"),
	MALE_FRENCH_CANADIAN("enst-dennys-hsmm"),
	MALE_FRENCH("upmc-pierre-hsmm"),
	FEMALE_FRENCH("enst-camille-hsmm"),
	FEMALE_FRENCH_2("upmc-jessica-hsmm"),
	MALE_GERMAN("bits3-hsmm"),
	MALE_GERMAN_2("dfki-pavoque-neutral-hsmm"),
	FEMALE_GERMAN("bits1-hsmm"),
	FEMALE_GERMAN_2("istc-lucia-hsmm"),
	MALE_TURKISH("dfki-ot-hsmm");

	@Getter
	private String voicename;

	Voices(String voicename)
	{
		this.voicename = voicename;
	}
}
