package moonfather.tetra_tables.initialization;

import moonfather.workshop_for_handsome_adventurer.dynamic_resources.WoodTypeLister;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DynamicWoodListAccessor
{
    @NotNull
    public static List<String> getWoodIds()
    {
        return WoodTypeLister.getWoodIds();
    }

    @NotNull
    public static String getHostMod(String wood)
    {
        return WoodTypeLister.getHostMod(wood);
    }
}