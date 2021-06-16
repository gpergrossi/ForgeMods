package com.gpergrossi.aerogen;

import net.fabricmc.api.ModInitializer;

public class AeroGenMod implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("Initializing AeroGen");
        
        AeroGenDimensionType.AddToRegistry();
    }
}