package com.gpergrossi.aerogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import jline.internal.Log;

public class AeroGeneratorSettings {
	
	public static final int GENERATOR_VERSION = 1;
	
	public int version = GENERATOR_VERSION;
	
	public long seed = 875849390123L;
	
	public double regionGridSize = 512;
	public double islandCellSize = 64;
	
	public boolean genStructures = true;
	// TODO spawn starting chest
	
	
	
	public AeroGeneratorSettings() {}


	/**
	 * Load all settings from the jsonString on top of the provided base settings.
	 * @param jsonString - json string of settings to be read
	 * @param base - settings to be inherited, or null
	 * @return settings object
	 */
	public static AeroGeneratorSettings loadJSON(String jsonString, AeroGeneratorSettings base) {
		final AeroGeneratorSettings result = new AeroGeneratorSettings();
		if (base != null) result.copyFrom(base);
		
		if (jsonString == null || jsonString.equals("")) {
			jsonString = "{}";
		}

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(AeroGeneratorSettings.class, new InstanceCreator<AeroGeneratorSettings>() {
			@Override
			public AeroGeneratorSettings createInstance(Type type) {
				return result;
			}
		});
		
		Gson gson = builder.create();
		
		return gson.fromJson(jsonString, AeroGeneratorSettings.class);
	}

	/**
	 * Save all settings that are different from the settings in base.
	 * @param settings - this settings object
	 * @param base - settings that are already inherited, or null
	 * @return json string
	 */
	public static String saveJSON(AeroGeneratorSettings settings, AeroGeneratorSettings base) {
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		
		if (base != null) {
			builder.addSerializationExclusionStrategy(new ExclusionStrategy() {
				@Override
				public boolean shouldSkipField(FieldAttributes f) {
					try {
						Field javaField = f.getDeclaringClass().getField(f.getName());
						if (javaField.isAnnotationPresent(DoNotTrack.class)) return true;
						
						Object inherited = javaField.get(base);
						Object value = javaField.get(settings);
						
						if (inherited == null) return (value == null);
						return inherited.equals(value);
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
						Log.error(e.getMessage());
						return false;
					}
				}
				
				@Override
				public boolean shouldSkipClass(Class<?> clazz) {
					return false;
				}
			});
		}
		
		Gson gson = builder.create();
		
		return gson.toJson(settings);
	}
	
	@Override
	public String toString() {
		return saveJSON(this, null);
	}
	
	/**
	 * Annotate fields with this and they will not be automatically compared by the equals() method
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	private static @interface DoNotTrack {}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AeroGeneratorSettings)) return false;
		AeroGeneratorSettings that = (AeroGeneratorSettings) obj;
				
		Field[] fields = AeroGeneratorSettings.class.getDeclaredFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) continue;
			if (field.isAnnotationPresent(DoNotTrack.class)) continue;
			
			Object thisV, thatV;
			try {
				thisV = field.get(this);
				thatV = field.get(that);
				if (!thisV.equals(thatV)) return false;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}

	public void copyFrom(AeroGeneratorSettings settings) {
		Field[] fields = AeroGeneratorSettings.class.getDeclaredFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) continue;
			if (field.isAnnotationPresent(DoNotTrack.class)) continue;
			
			try {
				field.set(this, field.get(settings));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
}
