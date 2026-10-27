package ibx.utility.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * 悬赏板:宽 2 × 高 2 的四格组合方块(同门、床的做法)。点的那格是左下主格,往右、往上各占一格;
 * 只有主格掉落物品,拆掉任意一格整块都没(其余几格在邻格更新里变空气)。
 * 左右按站在正面看:正面朝 FACING,从正面看右手边是 FACING 逆时针转 90°。
 */
public class QuestBoardBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<QuestBoardBlock> CODEC = simpleCodec(QuestBoardBlock::new);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final Map<Part, Map<Direction, VoxelShape>> SHAPES = makeShapes();

    public enum Part implements StringRepresentable {
        BOTTOM_LEFT("bottom_left", 0, 0),
        BOTTOM_RIGHT("bottom_right", 1, 0),
        TOP_LEFT("top_left", 0, 1),
        TOP_RIGHT("top_right", 1, 1);

        private final String name;
        /** 0 = 左列,1 = 右列 */
        public final int col;
        /** 0 = 下排,1 = 上排 */
        public final int row;

        Part(String name, int col, int row) {
            this.name = name;
            this.col = col;
            this.row = row;
        }

        public static Part of(int col, int row) {
            return row == 0 ? (col == 0 ? BOTTOM_LEFT : BOTTOM_RIGHT) : (col == 0 ? TOP_LEFT : TOP_RIGHT);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public QuestBoardBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, Part.BOTTOM_LEFT));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    // 碰撞箱照模型的立柱、钉板、横撑、横档、搁板、柱脚、横梁量的(纸、墨水瓶不算)。
    // 下面是正面朝北时左列的(从正面看左手边是东),单位像素;右列按 x 镜像
    private static Map<Part, Map<Direction, VoxelShape>> makeShapes() {
        double[][] bottom = {
                {12, 0, 2, 16, 2, 14},
                {13, 2, 6.5, 16, 16, 9.5},
                {0, 9, 7, 13, 16, 9},
                {0, 12, 9, 13, 14, 10},
                {0, 4, 7.5, 13, 6, 8.5},
                {0, 7, 4, 13, 8, 9},
        };
        double[][] top = {
                {13, 0, 6.5, 16, 13, 9.5},
                {0, 0, 7, 13, 13, 9},
                {0, 8, 9, 13, 10, 10},
                {0, 13, 5.5, 16, 16, 10.5},
        };
        Map<Part, Map<Direction, VoxelShape>> shapes = new EnumMap<>(Part.class);
        for (Part part : Part.values()) {
            VoxelShape shape = Shapes.empty();
            for (double[] b : part.row == 0 ? bottom : top) {
                double x0 = part.col == 0 ? b[0] : 16 - b[3];
                double x1 = part.col == 0 ? b[3] : 16 - b[0];
                shape = Shapes.or(shape, Block.box(x0, b[1], b[2], x1, b[4], b[5]));
            }
            shapes.put(part, Shapes.rotateHorizontal(shape));
        }
        return shapes;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(PART)).get(state.getValue(FACING));
    }

    private static Direction right(Direction facing) {
        return facing.getCounterClockWise();
    }

    private static BlockPos partPos(BlockPos origin, Direction facing, Part part) {
        return origin.relative(right(facing), part.col).above(part.row);
    }

    private static BlockPos originOf(BlockPos pos, BlockState state) {
        Part part = state.getValue(PART);
        return pos.relative(right(state.getValue(FACING)), -part.col).below(part.row);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos origin = context.getClickedPos();
        Level level = context.getLevel();
        for (Part part : Part.values()) {
            BlockPos pos = partPos(origin, facing, part);
            if (pos.getY() > level.getMaxY() || !level.getWorldBorder().isWithinBounds(pos)
                    || !level.getBlockState(pos).canBeReplaced(context)) {
                return null;
            }
        }
        return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.BOTTOM_LEFT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {
        Direction facing = state.getValue(FACING);
        for (Part part : Part.values()) {
            if (part != Part.BOTTOM_LEFT) {
                level.setBlock(partPos(pos, facing, part), state.setValue(PART, part), Block.UPDATE_ALL);
            }
        }
    }

    /** 和这一格连着的那一格(朝 direction 方向)应该是哪一格;不连着返回 null */
    private static @Nullable Part linked(Part part, Direction facing, Direction direction) {
        Direction right = right(facing);
        if (direction == Direction.UP && part.row == 0) return Part.of(part.col, 1);
        if (direction == Direction.DOWN && part.row == 1) return Part.of(part.col, 0);
        if (direction == right && part.col == 0) return Part.of(1, part.row);
        if (direction == right.getOpposite() && part.col == 1) return Part.of(0, part.row);
        return null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState,
                                     RandomSource random) {
        Direction facing = state.getValue(FACING);
        Part expected = linked(state.getValue(PART), facing, directionToNeighbour);
        if (expected != null && !(neighbourState.is(this) && neighbourState.getValue(FACING) == facing
                && neighbourState.getValue(PART) == expected)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    // 创造模式拆别的格:先把主格不掉落地拆掉,免得连带拆主格时掉出物品(同 DoublePlantBlock.preventDropFromBottomPart)
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.preventsBlockDrops() && state.getValue(PART) != Part.BOTTOM_LEFT) {
            BlockPos origin = originOf(pos, state);
            BlockState originState = level.getBlockState(origin);
            if (originState.is(this) && originState.getValue(PART) == Part.BOTTOM_LEFT) {
                level.setBlock(origin, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, origin, Block.getId(originState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    // 镜像后左右对调:朝向翻过来的同时,原来的左列落到了右手边
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) {
            return state;
        }
        Part part = state.getValue(PART);
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING))).setValue(PART, Part.of(1 - part.col, part.row));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
