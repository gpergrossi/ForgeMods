package com.gpergrossi.aerogen;

import java.util.OptionalLong;

import com.mojang.serialization.Lifecycle;

import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.source.BiomeAccessType;
import net.minecraft.world.biome.source.DirectBiomeAccessType;
import net.minecraft.world.dimension.DimensionType;

/**
 * From the Fabric Wiki:
 *   DimensionType is the registry wrapper around your dimension. 
 *   It has an ID and is what you register to add your Dimension. 
 *   It's also responsible for saving and loading your dimension from a file.
 */
public class AeroGenDimensionType
{
    public static final Identifier AEROGEN_SKY_ID = new Identifier("aerogen:sky");
    public static final RegistryKey<DimensionType> AEROGEN_SKY_REGISTRY_KEY = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, AEROGEN_SKY_ID);
    
    
    /* Dimension Type Configuration */
    /* (These are the same fields you would find in a Datapack Dimension) */
    
    public static final OptionalLong FIXED_TIME = OptionalLong.empty();
    
    public static final boolean HAS_SKY_LIGHT = true;
    public static final boolean HAS_CEILING = false;            // Affects map rendering.
    public static final boolean ULTRAWARM = false;              // If true: water cannot be placed, sponges dry out.
    public static final boolean NATURAL = true;                 // If true: pigmen spawn from nether portals, If false: compasses spin.
    
    public static final double COORDINATE_SCALE = 1.0;          // How coordinates are scaled when teleporting to/from this dimension.
    
    public static final boolean HAS_ENDER_DRAGON_FIGHT = false; 
    public static final boolean PIGLIN_SAFE = false;            // If true: piglins turn into zombified piglins
    public static final boolean BED_WORKS = true;               // If true: players can set spawn with beds, If false: beds explode
    public static final boolean RESPAWN_ANCHOR_WORKS = false;   // If true: players can set spawn with respawn anchors, If false: respawn anchors explode
    public static final boolean HAS_RAIDS = true;               // Pillager raids enabled? If true: spawns pillager captains and enables village raid mechanics.
    
    public static final int MINIMUM_Y = 0;
    public static final int HEIGHT = 256;
    public static final int LOGICAL_HEIGHT = HEIGHT - MINIMUM_Y;
    
    public static final BiomeAccessType BIOME_ACCESS_TYPE = DirectBiomeAccessType.INSTANCE; // This optionally filters the biome source. DirectBiomeAccessType does no filtering.
    public static final Identifier INFINIBURN = BlockTags.INFINIBURN_OVERWORLD.getId();     // This determines which blocks can burn forever.
    public static final Identifier SKY_PROPERTIES = new Identifier("aerogen:sky");          // This determines what the sky looks like on the client.
    public static final float AMBIENT_LIGHT = 0.0f;                                         // This is the ambient lighting in your dimension. Does not affect game mechanics, only visuals.

    /* End of Dimension Type Configuration */
    
    
    public static final DimensionType AEROGEN_SKY = DimensionType.create(
            FIXED_TIME, HAS_SKY_LIGHT, HAS_CEILING, ULTRAWARM, NATURAL, COORDINATE_SCALE, 
            HAS_ENDER_DRAGON_FIGHT, PIGLIN_SAFE, BED_WORKS, RESPAWN_ANCHOR_WORKS, HAS_RAIDS, 
            MINIMUM_Y, HEIGHT, LOGICAL_HEIGHT, BIOME_ACCESS_TYPE, INFINIBURN, 
            AEROGEN_SKY_ID, 0.0F);
}
