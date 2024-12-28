package com.portingdeadmods.stickit.client;

import com.portingdeadmods.stickit.client.command.CommandClientPlonk;
import com.portingdeadmods.stickit.client.registry.RegistryTESRs;
import com.portingdeadmods.stickit.client.render.tile.TESRPlacedItems;
import com.portingdeadmods.stickit.common.networking.PlaceItemPayload;
import com.portingdeadmods.stickit.common.networking.RotateTilePayload;
import com.portingdeadmods.stickit.common.registry.RegistryItems;
import com.portingdeadmods.stickit.common.tile.TilePlacedItems;
import com.portingdeadmods.stickit.common.util.EntityUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Consumer;

import static com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM;
import static net.neoforged.neoforge.client.settings.KeyConflictContext.IN_GAME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;

public class ClientEvents {
    public static final KeyMapping KEY_PLACE = new KeyMapping("key.stickit.place", IN_GAME, KEYSYM, GLFW_KEY_P, "key.categories.stickit");

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientEvents::setupClient);
        modEventBus.addListener(ClientEvents::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.addListener(ClientEvents::onKeyInput);
        NeoForge.EVENT_BUS.addListener(ClientEvents::serverStarting);
    }

    public static void setupClient(FMLClientSetupEvent event) {
        RegistryTESRs.init();
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientEvents.KEY_PLACE);
    }

    /**
  *  TODO: Switch over to this over:
  *  {@link com.portingdeadmods.stickit.common.block.BlockPlacedItems#initializeClient(Consumer)}
  */
//    public static void onClientExtensions(RegisterClientExtensionsEvent event) {
//        event.registerBlock(new IClientBlockExtensions() {
//            @Override
//            public boolean addHitEffects(BlockState state, Level Level, HitResult target, ParticleEngine manager) {
//                return true;
//            }
//
//            @Override
//            public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
//                return true;
//            }
//        }, );
//    }

    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.getOverlay() == null && mc.screen == null) {
            if (KEY_PLACE.consumeClick() && player != null) {
                HitResult hitRaw = mc.hitResult;
                if (hitRaw != null && hitRaw.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult hit = (BlockHitResult) hitRaw;
                    ItemStack held = player.getMainHandItem();
                    if (!held.isEmpty()) {
                        int renderType = TESRPlacedItems.getRenderTypeFromStack(held);
                        ItemStack toPlace = new ItemStack(RegistryItems.placed_items, 1);
                        RegistryItems.placed_items.setHeldStack(toPlace, held, renderType);
                        EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, toPlace);
                        if (toPlace.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction()) {
                            PacketDistributor.sendToServer(new PlaceItemPayload(hit, renderType));
                            ItemStack newHeld = RegistryItems.placed_items.getHeldStack(toPlace);
                            EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, newHeld);
                        } else {
                            EntityUtils.setHeldItemSilent(player, InteractionHand.MAIN_HAND, held);
                        }
                    } else if (player.isShiftKeyDown()) {
                        if (!rotatePlacedItemsTile(player.level(), hit.getBlockPos())) {
                            rotatePlacedItemsTile(player.level(), hit.getBlockPos().relative(hit.getDirection()));
                        }
                    }
                }
            }
        }
    }

    private static boolean rotatePlacedItemsTile(Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof TilePlacedItems) {
            ((TilePlacedItems) te).rotateTile();
            PacketDistributor.sendToServer(new RotateTilePayload(pos));
            return true;
        }
        return false;
    }

    public static void serverStarting(ServerStartingEvent event) {
        new CommandClientPlonk().register(event.getServer().getCommands().getDispatcher());
    }
}