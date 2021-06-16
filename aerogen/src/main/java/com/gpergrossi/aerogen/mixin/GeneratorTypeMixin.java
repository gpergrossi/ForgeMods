package com.gpergrossi.aerogen.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.gpergrossi.aerogen.AeroGenGeneratorType;

import net.minecraft.client.world.GeneratorType;

@Mixin(GeneratorType.class)
public abstract class GeneratorTypeMixin
{
    @Shadow @Final @Mutable
    protected static List<GeneratorType> VALUES;
    
    //public static final int PUTSTATIC = org.objectweb.asm.Opcodes.PUTSTATIC;
    
    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void OnSetValues(CallbackInfo info) {
        VALUES.add(AeroGenGeneratorType.AEROGEN_SKY);
    }
}
