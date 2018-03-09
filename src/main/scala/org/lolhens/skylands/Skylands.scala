package org.lolhens.skylands

import java.io.File

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.{Item, ItemBlock}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.DimensionType
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.functions.LootFunction
import net.minecraft.world.storage.loot.{LootEntry, LootEntryItem, LootPool, RandomValueRange}
import net.minecraftforge.common.{DimensionManager, MinecraftForge}
import net.minecraftforge.event.{LootTableLoadEvent, RegistryEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.lolhens.skylands.block.{BlockBean, BlockBeanStem, BlockCloud}
import org.lolhens.skylands.feature.{FallIntoOverworld, FeatherGliding}
import org.lolhens.skylands.tileentities.TileEntityBeanPlant
import org.lolhens.skylands.world.WorldProviderSkylands

/**
  * Created by pierr on 02.01.2017.
  */
class Skylands(configFile: File) {
  val config = new Config(configFile)

  val blockBeanStem = new BlockBeanStem().setRegistryName("beanstem")
  val blockBean = new BlockBean().setRegistryName("bean")
  val blockCloud = new BlockCloud().setRegistryName("cloud")

  val blocks = Seq(blockBeanStem, blockBean, blockCloud)

  GameRegistry.registerTileEntity(classOf[TileEntityBeanPlant], "bean_tile_entity")

  @SubscribeEvent
  def registerBlocks(event: RegistryEvent.Register[Block]): Unit = {
    println("loading-------------------------------------------------")
    event.getRegistry.registerAll(blocks: _*)
  }

  @SubscribeEvent
  def registerItems(event: RegistryEvent.Register[Item]): Unit = {
    event.getRegistry.registerAll(blocks.map { block =>
      new ItemBlock(block).setRegistryName(block.getRegistryName)
    }: _*)
  }

  val skylandsOverlap = 15

  val skylandsDimensionType: DimensionType = DimensionType.register("Skylands", "sky", config.dimensionId, classOf[WorldProviderSkylands], false)
  DimensionManager.registerDimension(config.dimensionId, skylandsDimensionType)

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  private val beanChests = List(
    "minecraft:chests/stronghold_corridor",
    "minecraft:chests/simple_dungeon",
    "minecraft:chests/nether_bridge",
    "minecraft:chests/igloo_chest",
    "minecraft:chests/abandoned_mineshaft",
    "minecraft:chests/stronghold_crossing",
    "minecraft:chests/jungle_temple",
    "minecraft:chests/desert_pyramid",
    "minecraft:chests/stronghold_library",
    "minecraft:chests/village_blacksmith",
    "minecraft:chests/jungle_temple_dispenser",
    "minecraft:chests/end_city_treasure",
    "minecraft:chests/spawn_bonus_chest"
  )

  @SubscribeEvent
  def onLootTableLoad(event: LootTableLoadEvent): Unit = {
    def LootPool(name: String, entries: List[LootEntry], conditions: List[LootCondition] = Nil, rolls: Range, bonusRolls: Range = 0 to 0) =
      new LootPool(entries.toArray, conditions.toArray, new RandomValueRange(rolls.start, rolls.end), new RandomValueRange(bonusRolls.start, bonusRolls.end), name)

    def LootEntryItem(item: Item, weight: Int = 1, quality: Int = 0, functions: List[LootFunction] = Nil, conditions: List[LootCondition] = Nil) =
      new LootEntryItem(item, weight, quality, functions.toArray, conditions.toArray, item.getRegistryName.toString)

    if (beanChests.contains(event.getName.toString))
      event.getTable.addPool(
        LootPool(
          name = "skylands",
          rolls = 1 to 1,
          entries = List(LootEntryItem(
            Item.getItemFromBlock(blockBean)
          ))
        )
      )
  }

  def keepSkylandsLoaded(): Unit = {
    skylandsDimensionType.setLoadSpawn(true)
    _keepSkylandsLoaded = true
  }

  private var _keepSkylandsLoaded = false

  @SubscribeEvent
  def onWorldTick(event: TickEvent.WorldTickEvent): Unit = {
    if (event.world.provider.getDimensionType == skylandsDimensionType) {

      //Minecraft.getMinecraft.renderGlobal.chunksToUpdate = Sets.newLinkedHashSet[RenderChunk]()
      if (_keepSkylandsLoaded)
        _keepSkylandsLoaded = false
      else if (skylandsDimensionType.shouldLoadSpawn())
        skylandsDimensionType.setLoadSpawn(false)
    }
  }

  var lightUpdates: Set[BlockPos] = Set.empty
  val lightUpdatesLock = new Object()

  @SubscribeEvent
  def onClientTick(event: TickEvent.ClientTickEvent): Unit = {
    /*if (event.phase == TickEvent.Phase.START) {
      val minecraft = Minecraft.getMinecraft
      if (Option(minecraft.world).isDefined) {
        if (!minecraft.isGamePaused) {
          /*val setLightUpdates = minecraft.renderGlobal.setLightUpdates.toSet
          val renderDispatcher = minecraft.renderGlobal.renderDispatcher
          if (setLightUpdates.nonEmpty && !renderDispatcher.hasNoFreeRenderBuilders) {
            //if (minecraft.renderGlobal.chunksToUpdate.isEmpty)
              //println("Processing light updates")

            //lightUpdatesLock.synchronized(this.lightUpdates = setLightUpdates)
            //val lightUpdates = setLightUpdates.groupBy(_.getY).map(e => e._1 -> e._2.size)
            //println(lightUpdates.values.sum + "   " + lightUpdates.toList.sortBy(_._1).map(e => s"${e._1}:${e._2}").mkString(" "))
            //setLightUpdates /*.take(10)*/ .foreach { pos =>
              //if (minecraft.world.getBlockState(pos).getBlock == Blocks.AIR)
                //minecraft.world.setBlockState(pos, Blocks.DIAMOND_BLOCK.getDefaultState, 2)
              //val chunk = minecraft.world.getChunkFromBlockCoords(pos)
              //relightBlock(chunk, pos)
              //markBlocksForUpdate(minecraft.renderGlobal, pos.getX - 1, pos.getY - 1, pos.getZ - 1, pos.getX + 1, pos.getY + 1, pos.getZ + 1, false)
            //}
          }*/
        }
      }
    }*/
  }

  @SubscribeEvent
  def onServerTick(event: TickEvent.ServerTickEvent): Unit = {
    /*if (event.phase == TickEvent.Phase.END) {
      val minecraft = Minecraft.getMinecraft
      if (minecraft.world != null) {
        /*lightUpdatesLock.synchronized {
          if (lightUpdates.nonEmpty) println(lightUpdates.size)
          lightUpdates /*.take(10)*/ .foreach { pos =>
            if (minecraft.world.getBlockState(pos).getBlock == Blocks.AIR)
              minecraft.world.setBlockState(pos, Blocks.AIR.getDefaultState)
            val chunk = minecraft.world.getChunkFromBlockCoords(pos)
            chunk.relightBlock(pos.getX & 15, pos.getY, pos.getZ & 15)
            minecraft.renderGlobal.markBlocksForUpdate(pos.getX - 1, pos.getY - 1, pos.getZ - 1, pos.getX + 1, pos.getY + 1, pos.getZ + 1, false)
          }
          lightUpdates = Set.empty
        }*/
      }
    }*/
  }

  @SubscribeEvent
  def onPlayerTick(event: TickEvent.PlayerTickEvent): Unit = {
    val player = event.player

    player match {
      case player: EntityPlayerMP =>
        FallIntoOverworld.update(player)

      case _ =>
    }

    FeatherGliding.update(player)
  }
}
