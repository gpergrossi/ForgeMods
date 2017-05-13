package dev.mortus.test;

public class DiscordSpamGenerator {

	public static void main(String[] args) {
		
		DiscordSpamGenerator dsg = new DiscordSpamGenerator();
		
		dsg.addString("logan is a douche canoe");
		
		System.out.println(dsg.toString());
		
	}
	
	StringBuilder sb;
	
	public DiscordSpamGenerator() {
		sb = new StringBuilder();
	}
	
	public void clear() {
		sb = new StringBuilder();
	}
	
	public void addString(String str) {
		for (char c : str.toCharArray()) {
			this.addLetter(c);
		}
	}
	
	public void addLetter(char c) {
		if (Character.isAlphabetic(c)) {
			sb.append(":regional_indicator_").append(Character.toLowerCase(c)).append(": ");
		} else if (c == ' ') {
			sb.append("        ");
		} else if (Character.isDigit(c)) {
			switch (c) {
				case '0': sb.append(":zero: "); break;
				case '1': sb.append(":one: "); break;
				case '2': sb.append(":two: "); break;
				case '3': sb.append(":three: "); break;
				case '4': sb.append(":four: "); break;
				case '5': sb.append(":five: "); break;
				case '6': sb.append(":six: "); break;
				case '7': sb.append(":seven: "); break;
				case '8': sb.append(":eight: "); break;
				case '9': sb.append(":nine: "); break;
			}
		} else if (c == '#') {
			sb.append(":hash: ");
		} else if (c == '\n') {
			sb.append('\n');
		} else {
			System.err.println("No icon for character '"+c+"'");
		}
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
}
