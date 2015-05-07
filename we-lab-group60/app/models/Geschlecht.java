package models;

public enum Geschlecht {
	MAENNLICH, WEIBLICH;

	public static String[] toStringArray() {
		return new String[] { "männlich", "weiblich" };
	}

	public static Geschlecht getValueOf(String value) {
		if (value == null) {
			return null;
		}
		if (value.trim().toUpperCase().equals("F")) {
			return Geschlecht.WEIBLICH;
		}
		if (value.trim().toUpperCase().equals("M")) {
			return Geschlecht.MAENNLICH;
		}
		return null;
	}
}
