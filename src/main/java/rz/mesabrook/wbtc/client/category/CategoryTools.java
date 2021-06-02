package rz.mesabrook.wbtc.client.category;

import net.minecraft.item.ItemStack;
import rz.mesabrook.wbtc.init.ModBlocks;
import rz.mesabrook.wbtc.init.ModItems;

/**
 * Original Author: MrCrayfish
 * Adapted by RavenholmZombie for use in Immersibrook.
 * 
 * https://github.com/MrCrayfish
 * https://github.com/RavenholmZombie
 */
public class CategoryTools extends AbstractCategory {

	public CategoryTools()
	{
		super("im.filter.tools", new ItemStack(ModItems.DIAMOND_SOD));
	}
	
	@Override
	public void init() 
	{
		// Sods
		add(ModItems.WOOD_SOD);
		add(ModItems.STONE_SOD);
		add(ModItems.IRON_SOD);
		add(ModItems.GOLD_SOD);
		add(ModItems.DIAMOND_SOD);
		add(ModItems.ALUMINUM_SOD);
		add(ModItems.ALUMINUM_SWORD);
		
		// Specials
		add(ModItems.ZOE_CANE);
		add(ModItems.LEVI_HAMMER);
		
		// First Aid Kit
		add(ModItems.FIRST_AID_KIT);
		
		// Night Vision Goggles
		add(ModItems.NV_GOGGLES);

		// Poppers
		add(ModItems.POPPER_RED);
		add(ModItems.POPPER_GREEN);
		add(ModItems.POPPER_BLUE);
	}

}
