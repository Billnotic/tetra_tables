package moonfather.tetra_tables.blocks;

import moonfather.tetra_tables.initialization.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.workbench.AbstractWorkbenchBlock;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;

import java.util.List;

public class TetraTable extends AbstractWorkbenchBlock
{
    private static final VoxelShape SHAPE_TOP = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_LEG1 = Block.box(0.0D, 0.0D, 11.0D, 5.0D, 12.0D, 16.0D);
    private static final VoxelShape SHAPE_LEG2 = Block.box(11.0D, 0.0D, 0.0D, 16.0D, 12.0D, 5.0D);
    private static final VoxelShape SHAPE_LEG3 = Block.box(0.0D, 0.0D, 0.0D, 5.0D, 12.0D, 5.0D);
    private static final VoxelShape SHAPE_LEG4 = Block.box(11.0D, 0.0D, 11.0D, 16.0D, 12.0D, 16.0D);
    //Made SHAPE_HANDSOME not final so that it can be modified depending on if handsome is installed.
    private static final VoxelShape SHAPE_HANDSOME = Shapes.or(SHAPE_TOP, SHAPE_LEG1, SHAPE_LEG2, SHAPE_LEG3, SHAPE_LEG4);
    private static final VoxelShape SHAPE_DEFAULT = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    public static final BooleanProperty handsome = BooleanProperty.create("handsome");
    public TetraTable(Properties properties)
    {
        super(properties);
    }

    public TetraTable()
    {
        super(Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2f, 3f).sound(SoundType.WOOD).pushReaction(PushReaction.DESTROY));
        //Registers the default state for if the block is handsome (uses workbench for handsome adventurer-like model and textures).
        //- Billnotic
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(handsome,false)
        );
    }

    /////////////////////////

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(handsome);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState p_60578_, BlockGetter p_60579_, BlockPos p_60580_)
    {
        if(p_60578_.getValue(handsome)){
            return SHAPE_HANDSOME;
        }
        return SHAPE_DEFAULT;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState p_60581_, BlockGetter p_60582_, BlockPos p_60583_)
    {
        if(p_60581_.getValue(handsome)){
            return SHAPE_HANDSOME;
        }
        return SHAPE_DEFAULT;
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_)
    {
        if(p_60555_.getValue(handsome)){
            return SHAPE_HANDSOME;
        }
        return SHAPE_DEFAULT;
    }



    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return 5;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
    {
        return 20;
    }

    ///////////////////////////////////////////////////////////

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult)
    {
        if (this.isObscured(level, pos))
        {
            if (level.isClientSide)
            {
                player.displayClientMessage(MessageInaccessible, true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        InteractionResult interactionResult = BlockInteraction.attemptInteraction(level, state, pos, player, hand, blockHitResult);
        if (interactionResult != InteractionResult.PASS || hand == InteractionHand.OFF_HAND)
        {
            return interactionResult;
        }
        if (level.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TetraTableBlockEntity ttbe)
        {
            NetworkHooks.openScreen((ServerPlayer) player, ttbe, pos);
        }
        return InteractionResult.CONSUME;
    }
    private final Component MessageInaccessible = Component.translatable("message.tetra_tables.table_obscured");


    private boolean isObscured(Level level, BlockPos pos)
    {
        if (!level.getFluidState(pos.above()).getType().equals(Fluids.EMPTY))
        {
            return true;
        }
        VoxelShape s = level.getBlockState(pos.above()).getFaceOcclusionShape(level, pos.above(), Direction.DOWN);
        if (s.isEmpty())
        {
            return false;
        }
        double area = (s.max(Direction.Axis.X) - s.min(Direction.Axis.X)) * (s.max(Direction.Axis.Z) - s.min(Direction.Axis.Z));
        double lesserDim = Math.min(s.max(Direction.Axis.X) - s.min(Direction.Axis.X), s.max(Direction.Axis.Z) - s.min(Direction.Axis.Z));
        return area > 0.1875 || lesserDim > 0.1875;
    }



    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag)
    {
        list.add(hoverText);
    }
    private final Component hoverText = Component.translatable("block.tetra.basic_workbench.description").withStyle(ChatFormatting.GRAY);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (! equals(newState.getBlock()))
        {
            BlockEntity te = worldIn.getBlockEntity(pos);
            if (te instanceof WorkbenchTile)
            {
                LazyOptional<IItemHandler> oih = te.getCapability(ForgeCapabilities.ITEM_HANDLER);
                oih.ifPresent(
                        ih -> {
                            for (int i = 0; i < ih.getSlots(); i++)
                            {
                                ItemStack itemStack = ih.getStackInSlot(i);
                                if (! itemStack.isEmpty())
                                {
                                    Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemStack.copy());
                                }
                            }
                        }
                );
                te.setRemoved();
            }
        }
    }



    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState)
    {
        return Registration.TETRA_TABLE_BE.get().create(pos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_)
    {
        return null;
    }

    /*private static final Component CONTAINER_TITLE = new TranslatableComponent("container.crafting");
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos blockPos)
    {
        return new SimpleMenuProvider((containerId, inventory, p_52231_) ->
        {
            return new SimpleTableMenu(containerId, inventory, ContainerLevelAccess.create(level, blockPos), Registration.CRAFTING_SINGLE_MENU_TYPE.get());
        }, CONTAINER_TITLE);
    }*/
}
