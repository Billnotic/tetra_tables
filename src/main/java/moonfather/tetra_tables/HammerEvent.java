package moonfather.tetra_tables;

import moonfather.tetra_tables.blocks.TetraTable;

import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.TetraToolActions;

import java.util.Arrays;
import java.util.List;

public class HammerEvent
{
    private static final ResourceLocation WORKBENCH_ADVANCEMENT = new ResourceLocation("tetra", "upgrades/workbench");
    private static final List<String> DEFAULT_WOOD_TYPES = Arrays.asList(
            "acacia",
            "bamboo",
            "birch",
            "cherry",
            "crimson",
            "dark_oak",
            "jungle",
            "mangrove",
            "oak",
            "spruce",
            "warped"
    );

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        BlockState state = event. getLevel().getBlockState(event.getPos());
        if (state.is(workbench) && ! event.getEntity().isCrouching())
        {
            if (! state.is(Blocks.CRAFTING_TABLE))  // leave that one alone
            {
                if (event.getHand().equals(InteractionHand.MAIN_HAND) && event.getEntity().getMainHandItem().canPerformAction(TetraToolActions.hammer))
                {
                    Block newTable = tryGetTable(state.getBlock());
                    if (newTable != null)
                    {
                        //Checks if the block targeted by the hammer event is a workbench from handsome adventurer.
                        //If so it sets the block state to handsome.
                        BlockState stateToSet = newTable.defaultBlockState();
                        if (ForgeRegistries.BLOCKS.getKey(state.getBlock()).getNamespace().equals("workshop_for_handsome_adventurer")||ForgeRegistries.BLOCKS.getKey(state.getBlock()).getPath().contains("wfha/"))
                        {
                            stateToSet = stateToSet.setValue(TetraTable.handsome, true);
                        }
                        //This checks if the wood type is not vanilla and everycompat is not installed,
                        //in which case the only type of workbench that CAN exist are handsome.
                        else if (!(DEFAULT_WOOD_TYPES.contains(ForgeRegistries.BLOCKS.getKey(newTable).getPath().replace("tetra_table_",""))) && !ModList.get().isLoaded("everycomp"))
                        {
                            stateToSet = stateToSet.setValue(TetraTable.handsome, true);
                        } else {
                            stateToSet = stateToSet.setValue(TetraTable.handsome, false);
                        }
                        event.getLevel().setBlockAndUpdate(event.getPos(), stateToSet);
                        //((TetraTable) event.getLevel().getBlockState(event.getPos()).getBlock()).setTargetShape(isHandsome);
                        event.getLevel().playSound(event.getEntity(), event.getPos(), SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.5F);
                        if (event.getEntity() instanceof ServerPlayer serverPlayer)
                        {
                            Advancement a = serverPlayer.getServer().getAdvancements().getAdvancement(WORKBENCH_ADVANCEMENT);
                            serverPlayer.getAdvancements().award(a, "hammer");
                        }
                    }
                    else
                    {
                        if (event.getLevel().isClientSide)
                        {
                            if (ModList.get().isLoaded("workshop_for_handsome_adventurer"))
                            {
                                //Temporarily commenting this out so I can figure out why the message displays
                                //Even if successful.
                                //event.getEntity().displayClientMessage(tableNotFound2, true);
                            }
                            else
                            {
                                event.getEntity().displayClientMessage(tableNotFound1, true);
                            }
                        }
                    }
                    event.setResult(Event.Result.DENY);
                    event.setUseItem(Event.Result.DENY);
                    event.setUseBlock(Event.Result.DENY);
                }
            }
        }
    }
    private static final TagKey<Block> workbench = BlockTags.create(new ResourceLocation("forge", "workbench"));;
    private static final Component tableNotFound1 = Component.translatable("message.tetra_tables.tableNotFound1");
    private static final Component tableNotFound2 = Component.translatable("message.tetra_tables.tableNotFound2");

    private static Block tryGetTable(Block block) {
        String id = ForgeRegistries.BLOCKS.getKey(block).getPath();
        String modOrigin = ForgeRegistries.BLOCKS.getKey(block).getNamespace();
        String wood = null;
        String woodOrigin = null;
        //Rewrote this slightly to include checks for everycompat, as well as defaulting a regular crafting table to oak
        //- Billnotic
        if (id.equals("crafting_table")) {
            if (modOrigin.equals("minecraft")) {
                wood = "oak";
            }
        }
        int namePos = id.indexOf("simple_table_");
        if (wood == null) {
            if (namePos >= 0) {
                wood = id.substring(namePos + 13);
            }
        }
        if (wood == null)
        {
            namePos = id.indexOf("_crafting_table");
            if (namePos >= 0)
            {
                if (id.contains("/")){
                    wood = id.substring(id.lastIndexOf("/")+1, namePos);
                    woodOrigin = id.substring(id.indexOf("/")+1,id.lastIndexOf("/"));
                } else {
                    wood = id.substring(0, namePos);
                }
            }
        }
		if (wood == null)
        {
            if (id.contains("crafting_table_"))
            {
                if (id.contains("/")){
                    wood = id.substring(15+id.lastIndexOf("/")+1);
                } else {
                    wood = id.substring(15);
                }
            }
        }
        if (wood == null)
        {
            return null;
        }

        //This is a hard check for certain vct types since they have an odd naming convention when using vct.
        //There might be more but I'm lazy and aren't using them in my modpack lmao, it should be easy to add more tho.
        //- Billnotic
        if (wood.contains("bop_")){
            wood = wood.replace("bop_","");
            woodOrigin = "biomesoplenty";
        } else if (wood.contains("eco_")){
            wood = wood.replace("eco_","");
            woodOrigin = "ecologics";
        } else if (wood.contains("quark_")){
            wood = wood.replace("quark_","");
            woodOrigin = "quark";
        } else if (wood.contains("atr_")){
            wood = wood.replace("atr_","");
            woodOrigin = "aether";
        }
        ResourceLocation rl = null;
        try {
            //Checks if the table is non-vanilla and has a non-handsome variant generated by everycompat
            if (!(DEFAULT_WOOD_TYPES.contains(wood)) && !modOrigin.equals("workshop_for_handsome_adventurer") && ModList.get().isLoaded("everycomp")) {
                //ForgeRegistries.BLOCKS.getKey(block).
                for (var w : WoodTypeRegistry.getTypes()) {
                    //System.out.println(w.getId().getPath()+","+w.getNamespace());
                    if (!w.isVanilla() && w.getId().getPath().equals(wood)) {
                        woodOrigin=w.getNamespace();
                        break;
                    }
                }
                rl = new ResourceLocation("everycomp", "ttln/" + woodOrigin + "/tetra_table_" + wood);
            } else {
                rl = new ResourceLocation(Constants.MODID, "tetra_table_" + wood);
            }
        } catch (Exception ignored){
        }
        if (ForgeRegistries.BLOCKS.containsKey(rl))
        {
            return ForgeRegistries.BLOCKS.getValue(rl);
        }
        return null;
    }
}
