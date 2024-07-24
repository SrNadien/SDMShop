package net.sdm.sdmshopr.shop.entry.type;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.sdm.sdmshopr.SDMShopR;
import net.sdm.sdmshopr.api.ICustomEntryType;
import net.sdm.sdmshopr.api.IEntryType;
import net.sdm.sdmshopr.client.buyer.BuyerScreen;
import net.sdm.sdmshopr.shop.entry.ShopEntry;
import net.sdm.sdmshopr.utils.StructureUtil;

import java.util.*;
import java.util.function.Predicate;

public class LocateBetaEntryType implements ICustomEntryType {

    public ResourceLocation location;
    public Type type;

    public LocateBetaEntryType(ResourceLocation location, Type type){
        this.location = location;
        this.type = type;
    }

    @Override
    public void addWidgets(BuyerScreen panel) {
//        panel.add(textBox = new TextBox(panel));
//        textBox.setSize(panel.inputField.width, panel.inputField.height);
//        textBox.setPos(8, panel.outputMoneyField.posY + panel.outputMoneyField.height + 2);


    }

    @Override
    public void alignWidgets(BuyerScreen panel) {

    }

    @Override
    public void buy(ServerPlayer player, int countBuy, ShopEntry<?> entry) {
        try {
            switch (type) {
                case STRUCTURE -> locateStructure(player);
//                case POI -> {}
                case BIOME -> locateBiome(player);
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }

    private void locateBiome(ServerPlayer player){
        ResourceKey<Biome> resourceKey = null;
        Registry<Biome> registry = player.level().registryAccess().registryOrThrow(Registries.BIOME);

        for (Map.Entry<ResourceKey<Biome>, Biome> entry : registry.entrySet()) {
            if(registry.getKey(entry.getValue()).toString().equals(location.toString())){
                resourceKey = entry.getKey();
            }
        }

        if(resourceKey == null || registry.getHolder(resourceKey).isEmpty()) return;

        BlockPos blockpos = BlockPos.containing(player.position());
        Holder<Holder.Reference<Biome>> featureHolderSet = registry.getHolder(resourceKey).map(Holder::direct).orElse(null);
        Predicate<Holder<Biome>> b = v -> v == featureHolderSet.get();
        Pair<BlockPos, Holder<Biome>> pair = ((ServerLevel) player.level()).findClosestBiome3d(b, blockpos, 6400, 32, 64);
        if (pair == null) {
            player.sendSystemMessage(Component.literal("Biome not founded ! Are you exactly in the dimension where she might be?"));
        } else {
            String f = "x = " + pair.getFirst().getX() + ",  z = " + pair.getFirst().getZ();
            player.sendSystemMessage(Component.literal("The biome is located at the following coordinates " + f));
        }
    }

    private void locateStructure(ServerPlayer player) throws CommandSyntaxException {

        ResourceKey<Structure> resourceKey = null;

        Registry<Structure> registry = player.level().registryAccess().registryOrThrow(Registries.STRUCTURE);

        for (Map.Entry<ResourceKey<Structure>, Structure> resourceKeyStructureEntry : registry.entrySet()) {
            if(registry.getKey(resourceKeyStructureEntry.getValue()).toString().equals(location.toString())) {
                resourceKey = resourceKeyStructureEntry.getKey();
                break;
            }
        }

        if(resourceKey == null) return;

        if(registry.getHolder(resourceKey).isEmpty()) return;

        BlockPos blockpos = BlockPos.containing(player.position());
        ServerLevel serverlevel = (ServerLevel) player.level();

        HolderSet<Structure> featureHolderSet = registry.getHolder(resourceKey).map(HolderSet::direct).orElse(null);
        Pair<BlockPos, Holder<Structure>> pair = StructureUtil.findNearestMapStructure(serverlevel, featureHolderSet, blockpos, 100, false);
        if (pair == null) {
            player.sendSystemMessage(Component.literal("Structure not founded ! Are you exactly in the dimension where she might be?"));
        } else {
            String f = "x = " + pair.getFirst().getX() + ", y = ?,  z = " + pair.getFirst().getZ();
            player.sendSystemMessage(Component.literal("The structure is located at the following coordinates " + f));
        }
    }


    private static final DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND = new DynamicCommandExceptionType((p_201831_) -> {
        return Component.translatable("commands.locate.structure.not_found", p_201831_);
    });
    private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType((p_207534_) -> {
        return Component.translatable("commands.locate.structure.invalid", p_207534_);
    });
    private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType((p_214514_) -> {
        return Component.translatable("commands.locate.biome.not_found", p_214514_);
    });
    private static final DynamicCommandExceptionType ERROR_POI_NOT_FOUND = new DynamicCommandExceptionType((p_214512_) -> {
        return Component.translatable("commands.locate.poi.not_found", p_214512_);
    });

    @Override
    public boolean canExecute(boolean isSell, int countSell, ShopEntry<?> entry) {
        long playerMoney = SDMShopR.getClientMoney();
        int needMoney = entry.price * countSell;
        if(playerMoney < needMoney || playerMoney - needMoney < 0) return false;
        return true;
    }


    @Override
    public boolean isSellable() {
        return false;
    }

    @Override
    public boolean isCountable() {
        return false;
    }

    @Override
    public Icon getIcon() {
        return Icons.ART;
    }

    @Override
    public void getConfig(ConfigGroup group) {
        group.addString("locate_id", location.toString(), v -> location = new ResourceLocation(v), "minecraft:iglooe");
        group.addEnum("locate_type", type.toString(), v -> type = Type.valueOf(v), getIDs());
    }


    public NameMap<String> getIDs(){
        List<String> ids = new ArrayList<>();
        for (Type value : Type.values()) {
            ids.add(value.name());
        }
        return NameMap.of(Type.BIOME.name(), ids).create();
    }


    @Override
    public Icon getCreativeIcon() {
        return Icons.ART;
    }

    @Override
    public String getID() {
        return "locateType";
    }

    @Override
    public IEntryType copy() {
        return new LocateBetaEntryType(location, type);
    }


    @Override
    public Component getTranslatableForContextMenu() {
        return Component.translatable("sdm.shop.entry.add.context.locationtype");
    }


    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = ICustomEntryType.super.serializeNBT();
        nbt.putString("locate_id", location.toString());
        nbt.putString("locate_type", type.name());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.location = new ResourceLocation(nbt.getString("locate_id"));
        this.type = Type.valueOf(nbt.getString("locate_type"));
    }

    public enum Type {
        BIOME,
        STRUCTURE;
    }
}
