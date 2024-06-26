package dev.dfonline.codeclient.location;

import dev.dfonline.codeclient.CodeClient;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Plot extends Location {
    /**
     * The position the player was at before they were teleported, in any scenario but plot borders.
     */
    public Vec3d devPos;
    protected Integer id;
    protected String name;
    protected String owner;
    protected Integer originX;
    protected Integer originZ;
    protected boolean hasUnderground = false;
    protected Boolean hasBuild;
    protected Boolean hasDev;
    protected Size size;

    public void setOrigin(int x, int z) {
        this.originX = x;
        this.originZ = z;
    }

    public Vec3d getPos() {
        return new Vec3d(originX, 0, originZ);
    }

    public void copyValuesFrom(Plot plot) {
        if (plot.id != null) this.id = plot.id;
        if (plot.name != null) this.name = plot.name;
        if (plot.owner != null) this.owner = plot.owner;
        if (plot.originX != null) this.originX = plot.originX;
        if (plot.originZ != null) this.originZ = plot.originZ;
        if (plot.hasBuild != null) this.hasBuild = plot.hasBuild;
        if (plot.hasDev != null) this.hasDev = plot.hasDev;
        if (plot.size != null) this.size = plot.size;
        if (plot.devPos != null) this.devPos = plot.devPos;
    }

    public void setHasUnderground(boolean hasUnderground) {
        this.hasUnderground = hasUnderground;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Size assumeSize() {
        if (size == null) return Size.MASSIVE;
        else return size;
    }

    public Integer getX() {
        return originX;
    }

    public Integer getZ() {
        return originZ;
    }

    public int getFloorY() {
        return hasUnderground ? 5 : 50;
    }

    public Boolean isInPlot(BlockPos pos) {
        return isInArea(pos.toCenterPos()) || isInDev(pos.toCenterPos());
    }

    /**
     * The play or build area.
     */
    public Boolean isInArea(Vec3d pos) {
        Size size = assumeSize();

        double x = pos.getX();
        double z = pos.getZ();

        boolean inX = (x >= originX) && (x <= originX + size.size + 1);
        boolean inZ = (z >= originZ) && (z <= originZ + size.size + 1);

        return inX && inZ;
    }

    public Boolean isInDev(BlockPos pos) {
        return isInDev(new Vec3d(pos.getX() + 1, pos.getY(), pos.getZ()));
    }

    public Boolean isInDev(Vec3d pos) {
        Size size = assumeSize();

        int x = (int) pos.getX();
        int z = (int) pos.getZ();

        boolean inX = (x <= originX) && (x >= originX - 19);
        boolean inZ = (z >= originZ) && (z <= originZ + size.size);

        return inX && inZ;
    }

    /**
     * Searches code space for all codeblocks which match the scan argument.
     * Checks both top and second sign.
     * Returns null if the plot origin is unknown or world is null.
     */
    @Nullable
    public HashMap<BlockPos, SignText> scanForSigns(Pattern name, Pattern scan) {
        if (CodeClient.MC.world == null || originX == null || originZ == null || !(CodeClient.location instanceof Plot))
            return null;
        HashMap<BlockPos, SignText> signs = new HashMap<>();
        for (int y = ((Plot) CodeClient.location).getFloorY(); y < 255; y += 5) {
            int xEnd = originX + 1;
            for (int x = originX - 20; x < xEnd; x++) {
                int zEnd = originZ + assumeSize().size;
                for (int z = originZ; z < zEnd; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockEntity block = CodeClient.MC.world.getBlockEntity(pos);
                    if (block instanceof SignBlockEntity sign) {
                        SignText text = sign.getFrontText();
                        Matcher nameMatch = name.matcher(text.getMessage(0, false).getString());
                        if (!nameMatch.matches()) continue;
                        Matcher scanMatch = scan.matcher(text.getMessage(1, false).getString());
                        if (scanMatch.matches()) signs.put(pos, text);
                    }
                }
            }
        }
        return signs;
    }

    public HashMap<BlockPos, SignText> scanForSigns(Pattern scan) {
        return scanForSigns(Pattern.compile("(PLAYER|ENTITY) EVENT|FUNCTION|PROCESS"), scan);
    }

    public BlockPos findFreePlacePos() {
        return findFreePlacePos(new BlockPos(originX - 1, 5, originZ));
    }

    public BlockPos findFreePlacePos(BlockPos origin) {
        if (originX == null || CodeClient.MC.world == null) return null;
        var world = CodeClient.MC.world;
        int y = Math.max(origin.getY(), 5);
        int x = origin.getX();
        while (y < 255) {
            while (originX - x <= 18) {
                x--;
                BlockPos pos = new BlockPos(x, y, originZ);
                if (world.getBlockState(pos.east()).isAir() && world.getBlockState(pos.west()).isAir()) return pos;
            }
            y += 5;
            x = originX - 1;
        }
        return null;
    }

    public enum Size {
        BASIC(50),
        LARGE(100),
        MASSIVE(300);

        public final int size;

        Size(int size) {
            this.size = size;
        }
    }
}
