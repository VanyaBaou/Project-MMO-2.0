package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.GlossaryScreen;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.rmi.log.LogHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TooltipHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean tooltipOn = true;
    private static String lastKey = "";
    private static int salvageFromArrayPos = 0, salvageFromArrayLength, salvageToArrayPos = 0, salvageToArrayLength;
    private static int treasureFromArrayPos = 0, treasureFromArrayLength, treasureToArrayPos = 0, treasureToArrayLength;
    private static long lastTime = System.nanoTime();
    private static Object[] salvageFromArray, salvageToArray;
    private static Object[] treasureFromArray, treasureToArray;

    public static void handleTooltip( ItemTooltipEvent event )
    {
        {
            if( !tooltipOn )
                return;

            PlayerEntity player = event.getPlayer();

            if( player != null )
            {
                ItemStack itemStack = event.getItemStack();
                Item item = itemStack.getItem();
                List<ITextComponent> tooltip = event.getToolTip();
                int level;

                if( item.getRegistryName() == null )
                    return;

                String regKey = item.getRegistryName().toString();
                float hardness;
                double dValue;
                Material material = null;

                if( ClientHandler.OPEN_MENU.isKeyDown() )
                {
                    GlossaryScreen.setButtonsToKey( regKey );
                    Minecraft.getInstance().displayGuiScreen( new GlossaryScreen( Minecraft.getInstance().player.getUniqueID(), new TranslationTextComponent( "pmmo.glossary" ), false ) );
                    return;
                }

                Map<String, Double> craftReq = JsonConfig.data.get( JType.REQ_CRAFT ).get( regKey );
                Map<String, Double> wearReq = JsonConfig.data.get( JType.REQ_WEAR ).get( regKey );
                Map<String, Double> toolReq = JsonConfig.data.get( JType.REQ_TOOL ).get( regKey );
                Map<String, Double> weaponReq = JsonConfig.data.get( JType.REQ_WEAPON ).get( regKey );
                Map<String, Double> useReq = JsonConfig.data.get( JType.REQ_USE ).get( regKey );
                Map<String, Double> placeReq = JsonConfig.data.get( JType.REQ_PLACE ).get( regKey );
                Map<String, Double> breakReq = JsonConfig.data.get( JType.REQ_BREAK ).get( regKey );
                Map<String, Double> xpValueGeneral = JsonConfig.data.get( JType.XP_VALUE_GENERAL ).get( regKey );
                Map<String, Double> xpValueBreaking = JsonConfig.data.get( JType.XP_VALUE_BREAK ).get( regKey );
                Map<String, Double> xpValueCrafting = JsonConfig.data.get( JType.XP_VALUE_CRAFT ).get( regKey );
                Map<String, Double> xpValueSmelting = JsonConfig.data.get( JType.XP_VALUE_SMELT ).get( regKey );
                Map<String, Double> xpValueCooking = JsonConfig.data.get( JType.XP_VALUE_COOK ).get( regKey );
                Map<String, Double> xpValueBrewing = JsonConfig.data.get( JType.XP_VALUE_BREW ).get( regKey );
                Map<String, Double> xpValueGrowing = JsonConfig.data.get( JType.XP_VALUE_GROW ).get( regKey );
                Map<String, Map<String, Double>> salvageInfo = JsonConfig.data2.get( JType.SALVAGE ).get( regKey );
                Map<String, Map<String, Double>> salvageFrom = JsonConfig.data2.get( JType.SALVAGE_FROM ).get( regKey );
                Map<String, Map<String, Double>> treasureInfo = JsonConfig.data2.get( JType.TREASURE ).get( regKey );
                Map<String, Map<String, Double>> treasureFromInfo = JsonConfig.data2.get( JType.TREASURE_FROM ).get( regKey );
                Map<String, Double> heldItemXpBoost = JsonConfig.data.get( JType.XP_BONUS_HELD ).get( regKey );
                Map<String, Double> wornItemXpBoost = JsonConfig.data.get( JType.XP_BONUS_WORN ).get( regKey );

                if( item instanceof BlockItem)
                {
                    material = ( (BlockItem) item).getBlock().getDefaultState().getMaterial();
                    hardness = ( (BlockItem) item).getBlock().getDefaultState().getBlockHardness( null, null );
                    if( hardness > 0 )
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo.hardness", DP.dp( hardness ) ).getString() ) );
//                    tooltip.add( new StringTextComponent( XP.checkMaterial( material ) + " " + XP.getSkill( material ) ) );
                }

                //XP VALUE
                {
                    if( xpValueGeneral != null && xpValueGeneral.size() > 0 )      //XP GENERAL
                    {
                        tooltip.add( new TranslationTextComponent( "pmmo.xpValue" ) );

                        for( String key : xpValueGeneral.keySet() )
                        {
                            dValue = xpValueGeneral.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }

                    if( xpValueBreaking != null && xpValueBreaking.size() > 0 )      //XP BREAK
                    {
                        tooltip.add( new TranslationTextComponent( "pmmo.xpValueBreak" ) );

                        for( String key : xpValueBreaking.keySet() )
                        {
                            dValue = xpValueBreaking.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }

                    if( xpValueCrafting != null && xpValueCrafting.size() > 0 )      //XP CRAFT
                    {
                        tooltip.add( new TranslationTextComponent( "pmmo.xpValueCraft" ) );

                        for( String key : xpValueCrafting.keySet() )
                        {
                            dValue = xpValueCrafting.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }

                    if( xpValueSmelting != null && xpValueSmelting.size() > 0 )      //XP SMELT
                    {
                        tooltip.add( new TranslationTextComponent( "pmmo.xpValueSmelt" ) );

                        for( String key : xpValueSmelting.keySet() )
                        {
                            dValue = xpValueSmelting.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }

                    if( xpValueCooking != null && xpValueCooking.size() > 0 )      //XP COOK
                    {
                        tooltip.add( new TranslationTextComponent( "pmmo.xpValueCook" ) );

                        for( String key : xpValueCooking.keySet() )
                        {
                            dValue = xpValueCooking.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }

                    if( xpValueBrewing != null && xpValueBrewing.size() > 0 )      //XP BREW
                    {
                        tooltip.add( new TranslationTextComponent( "pmmo.xpValueBrew" ) );

                        for( String key : xpValueBrewing.keySet() )
                        {
                            dValue = xpValueBrewing.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }

                    if( xpValueGrowing != null && xpValueGrowing.size() > 0 )      //XP GROW
                    {
                        tooltip.add( new TranslationTextComponent( "pmmo.xpValueGrow" ) );

                        for( String key : xpValueGrowing.keySet() )
                        {
                            dValue = xpValueGrowing.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }
                }

                //REQ
                {
                    if( craftReq != null && craftReq.size() > 0 )
                        addTooltipTextSkill( "pmmo.toCraft", JType.REQ_CRAFT, craftReq, event );

                    if( wearReq != null && wearReq.size() > 0 )
                        addTooltipTextSkill( "pmmo.toWear", JType.REQ_WEAR, wearReq, event );

                    if( wornItemXpBoost != null && wornItemXpBoost.size() > 0 )
                        addTooltipTextSkillPercentage( "pmmo.itemXpBoostWorn", wornItemXpBoost, event );

                    if( toolReq != null && toolReq.size() > 0 )
                        addTooltipTextSkill( "pmmo.tool", JType.REQ_TOOL, toolReq, event );
                    if( weaponReq != null && weaponReq.size() > 0 )
                        addTooltipTextSkill( "pmmo.weapon", JType.REQ_WEAPON, weaponReq, event );

                    if( heldItemXpBoost != null && heldItemXpBoost.size() > 0 )
                        addTooltipTextSkillPercentage( "pmmo.itemXpBoostHeld", heldItemXpBoost, event );

                    if( useReq != null && useReq.size() > 0 )
                        addTooltipTextSkill( "pmmo.use", JType.REQ_USE, useReq, event );

                    if( placeReq != null && placeReq.size() > 0 )
                    {
                        if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable)
                            addTooltipTextSkill( "pmmo.plant", JType.REQ_PLACE, placeReq, event );
                        else
                            addTooltipTextSkill( "pmmo.place", JType.REQ_PLACE, placeReq, event );
                    }

                    if( breakReq != null && breakReq.size() > 0 )
                    {
                        if( XP.correctHarvestTool( material ).equals( "axe" ) )
                            addTooltipTextSkill( "pmmo.chop", JType.REQ_BREAK, breakReq, event );
                        else if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable )
                            addTooltipTextSkill( "pmmo.harvest", JType.REQ_BREAK, breakReq, event );
                        else
                            addTooltipTextSkill( "pmmo.break", JType.REQ_BREAK, breakReq, event );
                    }
                }

                //INFO
                {
                    if( JsonConfig.data.get( JType.INFO_ORE ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_ORE ).get( regKey ).containsKey( "extraChance" ) )
                    {
                        dValue = XP.getExtraChance( player.getUniqueID(), item.getRegistryName(), JType.INFO_ORE, true );
                        if( dValue > 0 )  //ORE EXTRA CHANCE
                            tooltip.add( new TranslationTextComponent( "pmmo.oreExtraDrop", DP.dp( dValue / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                        else
                            tooltip.add( new TranslationTextComponent( "pmmo.oreExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                    }

                    if( JsonConfig.data.get( JType.INFO_LOG ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_LOG ).get( regKey ).containsKey( "extraChance" ) )
                    {
                        dValue = XP.getExtraChance( player.getUniqueID(), item.getRegistryName(), JType.INFO_LOG, true );
                        if( dValue > 0 )  //LOg EXTRA CHANCE
                            tooltip.add( new TranslationTextComponent( "pmmo.logExtraDrop", DP.dp( dValue / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                        else
                            tooltip.add( new TranslationTextComponent( "pmmo.logExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                    }

                    if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_PLANT ).get( regKey ).containsKey( "extraChance" ) )
                    {
                        dValue = XP.getExtraChance( player.getUniqueID(), item.getRegistryName(), JType.INFO_PLANT, true );
                        if( dValue > 0 )  //PLANT EXTRA CHANCE
                            tooltip.add( new TranslationTextComponent( "pmmo.plantExtraDrop", DP.dp( dValue / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                        else
                            tooltip.add( new TranslationTextComponent( "pmmo.plantExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                    }

                    if( JsonConfig.data.get( JType.INFO_SMELT ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_SMELT ).get( regKey ).containsKey( "extraChance" ) )
                    {
                        dValue = XP.getExtraChance( player.getUniqueID(), item.getRegistryName(), JType.INFO_SMELT, true );
                        if( dValue > 0 )  //SMELT EXTRA CHANCE
                            tooltip.add( new TranslationTextComponent( "pmmo.smeltExtraDrop", DP.dp( dValue / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                        else
                            tooltip.add( new TranslationTextComponent( "pmmo.smeltExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                    }

                    if( JsonConfig.data.get( JType.INFO_COOK ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_COOK ).get( regKey ).containsKey( "extraChance" ) )
                    {
                        dValue = XP.getExtraChance( player.getUniqueID(), item.getRegistryName(), JType.INFO_COOK, true );
                        if( dValue > 0 )  //COOK EXTRA CHANCE
                            tooltip.add( new TranslationTextComponent( "pmmo.cookExtraDrop", DP.dp( dValue / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                        else
                            tooltip.add( new TranslationTextComponent( "pmmo.cookExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                    }

                    if( JsonConfig.data.get( JType.INFO_BREW ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_BREW ).get( regKey ).containsKey( "extraChance" ) )
                    {
                        dValue = XP.getExtraChance( player.getUniqueID(), item.getRegistryName(), JType.INFO_BREW, true );
                        if( dValue > 0 )  //BREW EXTRA CHANCE
                            tooltip.add( new TranslationTextComponent( "pmmo.brewExtraDrop", DP.dp( dValue / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                        else
                            tooltip.add( new TranslationTextComponent( "pmmo.brewExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                    }

                    if( System.nanoTime() - lastTime > 900000000 )
                    {
                        lastTime = System.nanoTime();

                        salvageToArrayPos++;
                        if( salvageToArrayPos >= salvageToArrayLength )
                            salvageToArrayPos = 0;

                        salvageFromArrayPos++;
                        if( salvageFromArrayPos >= salvageFromArrayLength )
                            salvageFromArrayPos = 0;

                        treasureToArrayPos++;
                        if( treasureToArrayPos >= treasureToArrayLength )
                            treasureToArrayPos = 0;

                        treasureFromArrayPos++;
                        if( treasureFromArrayPos >= treasureFromArrayLength )
                            treasureFromArrayPos = 0;
                    }

                    if( !lastKey.equals( regKey ) )
                    {
                        if( salvageInfo != null )
                        {
                            salvageToArray = salvageInfo.keySet().toArray();
                            salvageToArrayLength = salvageToArray.length;
                            salvageToArrayPos = 0;
                        }
                        if( salvageFrom != null )
                        {
                            salvageFromArray = salvageFrom.keySet().toArray();
                            salvageFromArrayLength = salvageFromArray.length;
                            salvageFromArrayPos = 0;
                        }
                        if( treasureInfo != null )
                        {
                            treasureToArray = treasureInfo.keySet().toArray();
                            treasureToArrayLength = treasureToArray.length;
                            treasureToArrayPos = 0;
                        }
                        if( treasureFromInfo != null )
                        {
                            treasureFromArray = treasureFromInfo.keySet().toArray();
                            treasureFromArrayLength = treasureFromArray.length;
                            treasureFromArrayPos = 0;
                        }

                        lastKey = regKey;
                    }

                    if( salvageInfo != null )
                    {
                        try
                        {
                            level = Skill.SMITHING.getLevel( player );
                            Map<String, Double> salvageToItemMap;
                            int reqLevel, finalLevel, salvageMax, potentialReturnAmount;
                            double baseChance, xpPerItem, chancePerLevel, maxSalvageMaterialChance, chance, durabilityPercent;
                            String key = (String) salvageToArray[ salvageToArrayPos ];

                            salvageToItemMap = salvageInfo.get( key );
                            reqLevel = (int) Math.floor( salvageToItemMap.get( "levelReq" ) );
                            finalLevel = level - reqLevel;

                            baseChance = salvageToItemMap.get( "baseChance" );
                            xpPerItem = salvageToItemMap.get( "xpPerItem" );
                            chancePerLevel = salvageToItemMap.get( "chancePerLevel" );
                            maxSalvageMaterialChance = salvageToItemMap.get( "maxChance" );
                            chance = baseChance + ( chancePerLevel * finalLevel );

                            if( chance > maxSalvageMaterialChance )
                                chance = maxSalvageMaterialChance;

                            salvageMax = (int) Math.floor( salvageToItemMap.get( "salvageMax" ) );
                            durabilityPercent = ( 1.00D - ( (double) itemStack.getDamage() / (double) itemStack.getMaxDamage() ) );

                            if( Double.isNaN( durabilityPercent ) )
                                durabilityPercent = 1;

                            potentialReturnAmount = (int) Math.floor( salvageMax * durabilityPercent );
                            Item salvageItem = XP.getItem( key );

                            if( finalLevel < 0 )
                            {
                                tooltip.add( new TranslationTextComponent( "pmmo.cannotSalvageLackLevel", reqLevel ).setStyle( XP.textStyle.get( "red" ) ) );
                            }
                            else
                            {
                                tooltip.add( new TranslationTextComponent( "pmmo.salvagesIntoCountItem", potentialReturnAmount, new TranslationTextComponent( salvageItem.getTranslationKey() ) ).setStyle( XP.textStyle.get( potentialReturnAmount > 0 ? "green" : "red" ) ) );
                                tooltip.add( new TranslationTextComponent( "pmmo.xpEachChanceEach", " " + DP.dp( xpPerItem ), DP.dp( chance ) ).setStyle( XP.textStyle.get( chance > 0 ? "green" : "red" ) ) );
                            }
                        }
                        catch( Exception e )
                        {
                            LOGGER.error( e );
                        }
                    }

                    if( salvageFrom != null )
                    {
                        try
                        {
                            tooltip.add( new TranslationTextComponent( "pmmo.canBeSalvagedFrom" ).setStyle( XP.textStyle.get( "green" ) ) );
                            level = Skill.SMITHING.getLevel( player );

                            String key = (String) salvageFromArray[ salvageFromArrayPos ];
                            String displayName = new TranslationTextComponent( XP.getItem( key ).getTranslationKey() ).getString();

                            Map<String, Double> salvageFromMap = salvageFrom.get( key );

                            tooltip.add( new TranslationTextComponent( "pmmo.valueFromValue", " " + (int) (double) salvageFromMap.get( "salvageMax" ), displayName ).setStyle( XP.textStyle.get( salvageFromMap.get( "levelReq" ) > level ? "red" : "green" ) ) );
                        }
                        catch( Exception e )
                        {
                            LOGGER.error( e );
                        }
                    }

                    if( treasureInfo != null )
                    {
                        try
                        {
                            level = Skill.EXCAVATION.getLevel( player );
                            Map<String, Double> salvageToItemMap;
                            double chance, xpPerItem;
                            int minCount, maxCount;
                            String key = (String) treasureToArray[ treasureToArrayPos ];

                            salvageToItemMap = treasureInfo.get( key );
                            chance = DP.mapCapped( level, salvageToItemMap.get( "startLevel" ), salvageToItemMap.get( "endLevel" ), salvageToItemMap.get( "startChance" ), salvageToItemMap.get( "endChance" ) );
                            xpPerItem = salvageToItemMap.get( "xpPerItem" );
                            String itemName = new TranslationTextComponent( XP.getItem( key ).getTranslationKey() ).getString();
                            minCount = (int) (double) salvageToItemMap.get( "minCount" );
                            maxCount = (int) (double) salvageToItemMap.get( "maxCount" );

                            if( chance > 100 )
                                chance = 100;
                            if( chance <= 0 )
                                chance = 0;

                            tooltip.add( new TranslationTextComponent( "pmmo.containsCountItemTreasure", ( minCount == maxCount ? minCount : minCount + "-" + maxCount ), itemName ).setStyle( XP.textStyle.get( "green" ) ) );
                            tooltip.add( new TranslationTextComponent( "pmmo.xpEachChance", " " + DP.dp( xpPerItem ), DP.dp( chance ) ).setStyle( XP.textStyle.get( chance > 0 ? "green" : "red" ) ) );
                        }
                        catch( Exception e )
                        {
                            LOGGER.error( e );
                        }
                    }

                    if( treasureFromInfo != null )
                    {
                        try
                        {
                            tooltip.add( new TranslationTextComponent( "pmmo.treasureFrom" ).setStyle( XP.textStyle.get( "green" ) ) );
                            level = Skill.EXCAVATION.getLevel( player );

                            String key = (String) treasureFromArray[ treasureFromArrayPos ];
                            String displayName = new TranslationTextComponent( XP.getItem( key ).getTranslationKey() ).getString();

                            Map<String, Double> treasureFromMap = treasureFromInfo.get( key );
                            Map<String, Double> treasureToMap = JsonConfig.data2.get( JType.TREASURE ).get( key ).get( regKey );
                            int minCount = (int) (double) treasureFromMap.get( "minCount" );
                            int maxCount = (int) (double) treasureFromMap.get( "maxCount" );
                            double chance = DP.mapCapped( level, treasureToMap.get( "startLevel" ), treasureToMap.get( "endLevel" ), treasureToMap.get( "startChance" ), treasureToMap.get( "endChance" ) );

                            tooltip.add( new TranslationTextComponent( "pmmo.valueFromValue", " " + ( minCount == maxCount ? minCount : minCount + "-" + maxCount ), displayName ).setStyle( XP.textStyle.get( chance > 0 ? "green" : "red" ) ) );
                        }
                        catch( Exception e )
                        {
                            LOGGER.error( e );
                        }
                    }
                }
            }
        }
    }

    private static void addTooltipTextSkill( String tKey, JType jType, Map<String, Double> theMap, ItemTooltipEvent event )
    {
        PlayerEntity player = event.getPlayer();
        List<ITextComponent> tooltip = event.getToolTip();
        Item item = event.getItemStack().getItem();
        double level, value;

        if( theMap.size() > 0 )
        {
            tooltip.add( new TranslationTextComponent( tKey ).setStyle( XP.textStyle.get( XP.checkReq( player, item.getRegistryName(), jType ) ? "green" : "red" ) ) );

            for( String key : theMap.keySet() )
            {
                level = Skill.getSkill( key ).getLevel( player );

                value = theMap.get( key );
                tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dpSoft( value ) ).setStyle( XP.textStyle.get( level < value ? "red" : "green" ) ) );
            }
        }
    }

    private static void addTooltipTextSkillPercentage( String tKey, Map<String, Double> theMap, ItemTooltipEvent event )
    {
        List<ITextComponent> tooltip = event.getToolTip();
        double value;

        if( theMap.size() > 0 )
        {
            tooltip.add( new TranslationTextComponent( tKey ).setStyle( XP.textStyle.get( "green" ) ) );

            for( String key : theMap.keySet() )
            {
                value = theMap.get( key );
                if( value < 0 )
                    tooltip.add( new TranslationTextComponent( "pmmo.levelDisplayPercentage", " " + DP.dp( value ), new TranslationTextComponent( "pmmo." + key ).getString() ).setStyle( XP.textStyle.get( "red" ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.levelDisplayPercentage", " +" + DP.dp( value ), new TranslationTextComponent( "pmmo." + key ).getString() ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
            }
        }
    }
}