package dev.che1s.trailtalesdelight.block;

import com.huidu.farmersdelight.api.block.CraftEngineBlockAccess;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.block.behavior.CropBlockBehavior;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehavior;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.RandomTickBlock;
import net.momirealms.craftengine.core.block.property.IntegerProperty;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.random.RandomUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

/**
 * 凝乳在黑暗环境中熟成，在光照过强时重置。四个逻辑阶段可减少自定义状态数量，
 * 并通过三次成功的随机刻近似原模组总计十分钟的熟成时间。
 */
public final class CurdAgingBehavior extends BlockBehavior implements RandomTickBlock {
    public static final BlockBehaviorFactory<CurdAgingBehavior> FACTORY =
            (BlockDefinition block, ConfigSection section) -> new CurdAgingBehavior(
                    block,
                    (IntegerProperty) BlockBehaviorFactory.getProperty(
                            section.path(), block, "age", Integer.class),
                    Key.of(section.getString("mature-block", "trailandtales_delight:cheese_wheel")),
                    section.getInt("max-light", 8),
                    section.getInt("age-step", 200)
            );

    private final IntegerProperty age;
    private final Key matureBlock;
    private final int maxLight;
    private final int ageStep;

    private CurdAgingBehavior(BlockDefinition definition, IntegerProperty age, Key matureBlock,
                              int maxLight, int ageStep) {
        super(definition);
        this.age = age;
        this.matureBlock = matureBlock;
        this.maxLight = maxLight;
        this.ageStep = ageStep;
    }

    @Override
    public boolean canRandomlyTick(ImmutableBlockState state) {
        return true;
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args) {
        Optional<ImmutableBlockState> current = BlockStateUtils.getOptionalCustomBlockState(args[0]);
        if (current.isEmpty() || RandomUtils.generateRandomFloat(0F, 1F) >= 0.34F) {
            return;
        }
        ImmutableBlockState state = current.get();
        int light = CropBlockBehavior.getRawBrightness(args[1], args[2]);
        if (light > maxLight) {
            if (state.get(age) != age.min) {
                place(args, state.with(age, age.min));
            }
            return;
        }
        int nextAge = Math.min(age.max, state.get(age) + ageStep);
        if (nextAge >= age.max) {
            BlockDefinition target = CraftEngineBlocks.byId(matureBlock);
            if (target != null) {
                place(args, target.defaultState());
            }
        } else {
            place(args, state.with(age, nextAge));
        }
    }

    private void place(Object[] args, ImmutableBlockState state) {
        World world = CraftEngineBlockAccess.toWorld(args[1]);
        BlockPos position = CraftEngineBlockAccess.toBlockPos(args[2]);
        if (world != null && position != null) {
            CraftEngineBlocks.place(
                    new Location(world, position.x(), position.y(), position.z()),
                    state,
                    false
            );
        }
    }

    @Override
    public boolean isPathFindable(Object thisBlock, Object[] args) {
        return false;
    }

    @Override
    public void fallOn(Object thisBlock, Object[] args) {
    }

    @Override
    public void updateEntityMovementAfterFallOn(Object thisBlock, Object[] args) {
    }
}
