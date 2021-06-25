package com.gpergrossi.aerogen;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;

public class AeroGenDimensionOptions
{
    // TODO where should the aerogen:sky identifier be defined? obvious it shouldn't be redefined everywhere its used.
    public static final RegistryKey<DimensionOptions> AEROGEN_SKY = RegistryKey.of(Registry.DIMENSION_KEY, new Identifier("aerogen:sky")); 
}
