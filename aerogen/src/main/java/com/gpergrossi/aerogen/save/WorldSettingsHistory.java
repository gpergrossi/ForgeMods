package com.gpergrossi.aerogen.save;

import java.util.Map;
import java.util.TreeMap;
import com.gpergrossi.aerogen.AeroGenMod;
import com.gpergrossi.aerogen.AeroGeneratorSettings;

import jline.internal.Log;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;

public class WorldSettingsHistory extends WorldSavedData {

	private static final String DATA_NAME = AeroGenMod.MODID + "WorldSettingsHistory";
	
	public static WorldSettingsHistory forWorld(World world) {
		MapStorage storage = world.getPerWorldStorage();
		WorldSettingsHistory instance = (WorldSettingsHistory) storage.getOrLoadData(WorldSettingsHistory.class, DATA_NAME);
		if (instance == null) {
			instance = new WorldSettingsHistory(DATA_NAME);
			instance.initialize(world.getWorldInfo());
			storage.setData(DATA_NAME, instance);
		}
		instance.world = world;
		return instance;
	}
	
	public static final class SettingsRevision {
		private final int id;
		private final SettingsRevision previousRevision;
		private String settingsJSON;
		private AeroGeneratorSettings settings;
		
		protected SettingsRevision(int id) {
			this(id, null, null);
		}
		
		protected SettingsRevision(int id, SettingsRevision previousRevision, String settingsJSON) {		
			this.id = id;
			this.previousRevision = previousRevision;
			this.settingsJSON = settingsJSON;
		}

		public int getID() {
			return id;
		}

		public AeroGeneratorSettings getSettings() {
			if (settings == null) {
				this.settings = new AeroGeneratorSettings();
				AeroGeneratorSettings prevSettings = null;
				if (previousRevision != null) prevSettings = previousRevision.getSettings();
				this.settings = AeroGeneratorSettings.loadJSON(settingsJSON, prevSettings);
			}
			return settings;
		}
		
		public String getSettingsJSON() {
			if (settings != null) {
				AeroGeneratorSettings prevSettings = null;
				if (previousRevision != null) prevSettings = previousRevision.getSettings();
				this.settingsJSON = AeroGeneratorSettings.saveJSON(settings, prevSettings);
			}
			return settingsJSON;
		}
	}
	
	
	
	private World world;
	private Map<Integer, SettingsRevision> settingsRevisions;
	private SettingsRevision currentRevision;
		
	public WorldSettingsHistory(String dataIdentifier) {
		super(dataIdentifier);
		this.settingsRevisions = new TreeMap<>();
	}

	public AeroGeneratorSettings getCurrentSettings() {
		return currentRevision.getSettings();
	}
	

	
	private static final int NBT_COMPOUND = 10;
	
	private void initialize(WorldInfo info) {
		SettingsRevision baseRevision = new SettingsRevision(0, null, info.getGeneratorOptions());
		AeroGeneratorSettings settings = baseRevision.getSettings();
		settings.seed = info.getSeed();
		settings.genStructures = info.isMapFeaturesEnabled();
		
		this.settingsRevisions.put(0, baseRevision);
		this.currentRevision = baseRevision;
		this.setDirty(true);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		Log.info("Loading "+DATA_NAME);

		final NBTTagList revisionsNBT = nbt.getTagList("Revisions", NBT_COMPOUND);
		SettingsRevision lastRevision = null;
		
		for (int i = 0; i < revisionsNBT.tagCount(); i++) {
			final NBTTagCompound revisionNBT = revisionsNBT.getCompoundTagAt(i);
			
			final int id = revisionNBT.getInteger("ID");
			final String settingsJSON = revisionNBT.getString("Settings");
			final SettingsRevision revision = new SettingsRevision(id, lastRevision, settingsJSON);
			
			if (lastRevision != null && id <= lastRevision.getID()) {
				throw new RuntimeException("Bad "+DATA_NAME+" format! Revisions not in ascending order!");
			}
			
			settingsRevisions.put(id, revision);
			lastRevision = revision;
		}
		this.currentRevision = lastRevision;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		Log.info("Saving "+DATA_NAME);
		
		final NBTTagList revisionsNBT = new NBTTagList();
		
		for (SettingsRevision revision : settingsRevisions.values()) {
			final NBTTagCompound revisionNBT = new NBTTagCompound();
			
			revisionNBT.setInteger("ID", revision.getID());
			revisionNBT.setString("Settings", revision.getSettingsJSON());
			
			revisionsNBT.appendTag(revisionNBT);
		}
				
		nbt.setTag("Revisions", revisionsNBT);
		
		saveWorldSettings();
		return nbt;
	}

	private void saveWorldSettings() {
		String json = this.currentRevision.getSettingsJSON();
		
		WorldInfo worldInfo = world.getWorldInfo();
		if (worldInfo.getGeneratorOptions().equals(json)) return;
		
		Log.info("Attempting to save new generator options to \""+worldInfo.getWorldName()+"/level.dat\"...");
		
		final int generatorOptionsFieldIndex = 6;
		Object value = ReflectionHelper.getPrivateValue(WorldInfo.class, worldInfo, generatorOptionsFieldIndex);
		if (value.getClass() != String.class) {
			Log.error("Could not get generator options private field. Index is likely invalid!");
			return;
		}
		
		String options = (String) value;
		if (!options.equals(worldInfo.getGeneratorOptions())) {
			Log.error("Could not get generator options private field. Index is likely invalid!");
			return;
		}
		
		try {
			ReflectionHelper.setPrivateValue(WorldInfo.class, worldInfo, json, generatorOptionsFieldIndex);
		} catch (UnableToAccessFieldException e) {
			Log.error("Could not access generator options private field!");
			return;
		}
		
		if (!worldInfo.getGeneratorOptions().equals(json)) {
			Log.error("Could not set generator options private field. Index is likely invalid!");
			return;
		}
		
		Log.info("Successfully saved new generator options.");
	}
	
}
