package dev.che1s.trailtalesdelight.block;

import com.huidu.farmersdelight.api.block.CraftEngineBlockAccess;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehavior;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.RandomTickBlock;
import net.momirealms.craftengine.core.block.property.IntegerProperty;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

/**
 * 原作物在最后一个萌芽阶段后的生长刻中变成可结果实的藤蔓。
 * CraftEngine 的 crop_block 负责普通生长年龄；此行为只负责最后的方块状态转换。
 */
public final class BuddingTransitionBehavior extends BlockBehavior implements RandomTickBlock {
    public static final BlockBehaviorFactory<BuddingTransitionBehavior> FACTORY =
            (BlockDefinition block, ConfigSection section) -> new BuddingTransitionBehavior(
                    block,
                    (IntegerProperty) BlockBehaviorFactory.getProperty(
                            section.path(), block, "age", Integer.class),
                    Key.of(section.getString("mature-block", "trailandtales_delight:lantern_fruits"))
            );

    private final IntegerProperty age;
    private final Key matureBlock;

    private BuddingTransitionBehavior(BlockDefinition definition, IntegerProperty age, Key matureBlock) {
        super(definition);
        this.age = age;
        this.matureBlock = matureBlock;
    }

    @Override
    public boolean canRandomlyTick(ImmutableBlockState state) {
        return state != null && state.get(age) >= age.max;
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args) {
        Optional<ImmutableBlockState> current = BlockStateUtils.getOptionalCustomBlockState(args[0]);
        if (current.isEmpty() || current.get().get(age) < age.max) {
            return;
        }
        BlockDefinition target = CraftEngineBlocks.byId(matureBlock);
        World world = CraftEngineBlockAccess.toWorld(args[1]);
        BlockPos position = CraftEngineBlockAccess.toBlockPos(args[2]);
        if (target == null || world == null || position == null) {
            return;
        }
        CraftEngineBlocks.place(
                new Location(world, position.x(), position.y(), position.z()),
                target.defaultState(),
                false
        );
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
