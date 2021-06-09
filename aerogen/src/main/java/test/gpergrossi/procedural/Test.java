package test.gpergrossi.procedural;

import java.lang.reflect.Field;

public class Test {

	public static class Foo<T extends Foo<T>> {
		
		public Foo() {}

		@SuppressWarnings("unchecked")
		protected void initialize() {
			Class<T> clazz = (Class<T>) this.getClass();
			T instance = (T) this;
			
			System.out.println("Reflection values:");
			
			for (Field field : clazz.getDeclaredFields()) {
				String name = field.getName();
				Object value;
				
				try {
					value = field.get(instance);
					System.out.println(name+" = "+value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
					System.out.println(name+" was not accessible");
				}
			}
		}
		
	}
	
	public static class Bar extends Foo<Bar> {
		
		int a = 5;
		int b = 20;
		int c;
		
		public Bar(int c) {
			super();
			this.c = c;
			
			initialize();
		}
		
	}
	
	public static void main(String[] args) {
		Bar foo = new Bar(2);
	}
	
}
