package com.portingdeadmods.plonk.data;

import com.mojang.serialization.Codec;
import com.portingdeadmods.plonk.Plonk;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class StickItDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Plonk.MOD_ID);

    public static final Supplier<DataComponentType<Boolean>> TAG_HELD = COMPONENTS.registerComponentType("held",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final Supplier<DataComponentType<Integer>> TAG_RENDER_TYPE = COMPONENTS.registerComponentType("render_type",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));



}
