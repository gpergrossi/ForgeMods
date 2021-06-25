package com.gpergrossi.aerogen;

import java.io.InputStream;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class AeroGenMod implements ModInitializer {
    
    SimpleSynchronousResourceReloadListener resourceReloadListener = null;
    
    @Override
    public void onInitialize() {
        System.out.println("Initializing AeroGen");
        
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this.getReloadListener());
    }

    private SimpleSynchronousResourceReloadListener getReloadListener()
    {
        if (resourceReloadListener == null)
        {
            resourceReloadListener = new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId()
                {
                    return new Identifier("aerogen", "sky");
                }
                
                @Override
                public void reload(ResourceManager manager)
                {
                    for (Identifier id : manager.findResources("aerogen", path -> path.endsWith(".json")))
                    {
                        try (InputStream stream = manager.getResource(id).getInputStream()) {
                            // Consume the stream however you want, medium, rare, or well done.
                        } catch (Exception e) {
                            //error("Error occurred while loading resource json " + id.toString(), e);
                        }
                    }
                }
            };
        }
        return resourceReloadListener;
    }
    
}