package me.canrad.blackcat.modules.attack;

import me.canrad.blackcat.BlackCat;
import me.canrad.blackcat.modules.Module;
import me.canrad.blackcat.setting.Setting;
import me.canrad.blackcat.util.BlockUtil;
import me.canrad.blackcat.util.EntityUtil;
import me.canrad.blackcat.util.InventoryUtil;
import me.canrad.blackcat.util.Timer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AutoRedStone
        extends Module {
    private static AutoRedStone INSTANCE;

    private final Setting<Integer> range = this.register(new Setting<>("Range", 6, 0, 8));
    private final Setting<Boolean> autoDisable = this.register(new Setting<>("AutoDisable", false));
    private final Setting<Boolean> rotate = this.register(new Setting<>("Rotate", false));
    Timer delayTimer = new Timer();

    public AutoRedStone() {
        super("AuoRedStone", "Place red stone in the feet of other players!", Module.Category.ATTACK, true, false, false);
        setInstance();
    }

    public static AutoRedStone getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AutoRedStone();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        delayTimer.reset();
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            disable();
            return;
        }
        doRedStone();
        if (autoDisable.getValue()) {
            disable();
        }
        delayTimer.reset();
        delayTimer.passedMs(1000L);
    }

    private void doRedStone() {
        EntityPlayer target = getTarget(range.getValue());
        if (target == null)
            return;
        placeBlock(target);
    }

    private EntityPlayer getTarget(double range) {
        EntityPlayer target = null;
        double distance = Math.pow(range, 2.0) + 1.0;
        for (EntityPlayer player : AutoTrap.mc.world.playerEntities) {
            if (EntityUtil.isntValid(player, range) || isPlaced(player) || BlackCat.speedManager.getPlayerSpeed(player) > 10.0)
                continue;
            if (target == null) {
                target = player;
                distance = AutoTrap.mc.player.getDistanceSq(player);
                continue;
            }
            if (mc.player.getDistanceSq(player) >= distance) continue;
            target = player;
            distance = mc.player.getDistanceSq(player);
        }
        return target;
    }

    private boolean isPlaced(EntityPlayer player) {
        if (player == null) return true;
        BlockPos pos = new BlockPos(player.getPositionVector());
        return mc.world.getBlockState(pos).getBlock() == Blocks.REDSTONE_WIRE;
    }

    private void placeBlock(EntityPlayer target) {
        int originalSlot = InventoryUtil.getSlot();
        int tarSlot = InventoryUtil.findItemInHotbar(Items.REDSTONE);
        if (tarSlot == -1) return;
        InventoryUtil.switchToHotbarSlot(tarSlot, false);
        BlockUtil.placeBlock(new BlockPos(target.getPositionVector()), EnumHand.MAIN_HAND, rotate.getValue(), true, false);
        InventoryUtil.switchToHotbarSlot(originalSlot, false);
    }
}