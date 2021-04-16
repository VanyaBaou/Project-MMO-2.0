package harmonised.pmmo.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class TooltipSupplier {
	public static final Logger LOGGER = LogManager.getLogger();
	private static Map<JType, Map<ResourceLocation, Function<ItemStack, Map<String, Double>>>> tooltips = new HashMap<>();
	
	/**registers a Function to be used in providing the requirements for specific item
	 * skill requirements. The map consists of skill name and skill value pairs.  
	 * The ResouceLocation and JType parameters are conditions for when this check
	 * should be applied and are used by PMMO to know which functions apply in which
	 * contexts.  The function itself links to the compat mod's logic which handles 
	 * the external behavior.
	 *  
	 * @param res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param func returns a map of skills and required levels
	 */
	public static void registerTooltipData(ResourceLocation res, JType jType, Function<ItemStack, Map<String, Double>> func) 
	{
		if (func == null) {LOGGER.info("Supplied Function Null"); return;}
		if (jType == null) {LOGGER.info("Supplied JType Null"); return;}
		if (res == null) {LOGGER.info("Supplied ResourceLocation Null"); return;}
		
		if (!tooltips.containsKey(jType)) {
			LOGGER.info("New tooltip category created for: "+jType.toString()+" "+res.toString());
			tooltips.put(jType, new HashMap<ResourceLocation, Function<ItemStack, Map<String, Double>>>());
		}
		if (tooltipExists(res, jType)) return;
		//TODO implement existing function checker/logger though might not be needed
		tooltips.get(jType).put(res, func);
	}
	
	/**this is an internal method to check if a function exists for the given conditions
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @return whether or not a function is registered for the parameters
	 */
	public static boolean tooltipExists(ResourceLocation res, JType jType)
	{
		if (jType == null) return false;
		if (res == null) return false;
		if (!tooltips.containsKey(jType)) return false;
		return tooltips.get(jType).containsKey(res);
	}
	
	/**this is executed by PMMO where the requirde map for tooltips is used.  some PMMO
	 * behavior uses this map before a a requirement check for adding things like dynamic
	 * values. 
	 * 
	 * @param res res the block, item, or entity registrykey
	 * @param jType the PMMO behavior type
	 * @param stack the itemstack being evaluated for skill requirements
	 * @return the skill requirements of the item.
	 */	
	public static Map<String, Double> getTooltipData(ResourceLocation res, JType jType, ItemStack stack)
	{
		if (tooltipExists(res, jType)) {
			Map<String, Double> suppliedData = tooltips.get(jType).get(res).apply(stack);
			return suppliedData == null ? new HashMap<>() : suppliedData;
		}	
		return JsonConfig.data.getOrDefault(jType, new HashMap<>()).getOrDefault(res.toString(), new HashMap<>());
	}
}
