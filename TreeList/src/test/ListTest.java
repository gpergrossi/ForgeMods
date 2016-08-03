package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import list.TreeList;

public class ListTest {

	public static void main(String[] args) {
		TreeList<Character, Character> tree = new TreeList<Character, Character>();
		
		int count = 7;
		
		List<Character> numbers = new ArrayList<Character>();
		for (int i = 0; i < 26; i++) numbers.add((char) ('A'+i));
		for (int i = 0; i < 26; i++) numbers.add((char) ('a'+i));
		for (int i = 0; i < 10; i++) numbers.add((char) ('0'+i));
		Collections.shuffle(numbers);
		
		for(int i = 0; i < count; i++) {
			char c = numbers.get(i);
			System.out.println("\nAdding "+c);
			tree.getRoot().add(c, c);	
		}
		
		System.out.println("\nresult:");
		tree.getRoot().print();
		
		//tree.getRoot().getIndex(0);
		
	}
	
}
