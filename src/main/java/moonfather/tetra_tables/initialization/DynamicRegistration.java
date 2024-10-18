package moonfather.tetra_tables.initialization;

import moonfather.tetra_tables.Constants;
import moonfather.tetra_tables.blocks.TetraTable;
import moonfather.tetra_tables.blocks.WoodenBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;

public class DynamicRegistration
{
    public static void registerBlocksForThirdPartyWood(RegisterEvent event)
    {
        if (! event.getRegistryKey().equals(Registries.ITEM))  // Registries.BLOCK is too early
        {
            return;
        }
        if (! ModList.get().isLoaded("workshop_for_handsome_adventurer") || ModList.get().isLoaded("everycomp"))
        {
            return;
        }
        try  // because of unfreeze fuckery
        {
            final String mc = "minecraft";
            boolean frozen = ((ForgeRegistry) ForgeRegistries.BLOCKS).isLocked();
            if (frozen) { ((ForgeRegistry) ForgeRegistries.BLOCKS).unfreeze(); }

            for (String wood : DynamicWoodListAccessor.getWoodIds())
            {
                if (DynamicWoodListAccessor.getHostMod(wood).equals(mc) && ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(Constants.MODID, "tetra_table_" + wood)))
                {
                    continue; // done manually, acacia for example
                }
                Block block = new TetraTable();
                Item item = new WoodenBlockItem(block);
                ForgeRegistries.BLOCKS.register("tetra_table_" + wood, block);
                ForgeRegistries.ITEMS.register("tetra_table_" + wood, item);
                Registration.blocks_table3.add(() -> block);
                CreativeTabEvent.itemsToAdd.add(() -> item);
            }
            if (frozen) { ((ForgeRegistry) ForgeRegistries.BLOCKS).freeze(); }
        }
        catch (Exception ignored)
        {
            System.out.println(ignored.getMessage());
        }
    }
}
