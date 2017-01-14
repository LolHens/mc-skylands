package org.lolhens.skylands.tileentities

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{World, WorldServer}
import org.lolhens.skylands.SkylandsMod

/**
  * Created by pierr on 14.01.2017.
  */
class TileEntityBeanPlant extends TileEntity with ITickable {
  private var progress: Int = 0

  override def update(): Unit = {
    worldObj match {
      case world: WorldServer =>
        val worldSkylands = world.getMinecraftServer.worldServerForDimension(SkylandsMod.skylands.skylandsDimensionType.getId)

        def drawBlock(position: BlockPos, blockState: IBlockState) = {
          if (position.getY < 255)
            if (isReplaceable(world, position))
              world.setBlockState(position, blockState)
          if (position.getY > 240) {
            val skyPosition = position.add(0, -240, 0)
            if (isReplaceable(worldSkylands, skyPosition))
              worldSkylands.setBlockState(skyPosition, blockState)
          }
        }

        def drawLayer(position: BlockPos) = {
          for (
            x <- -2 to 2;
            z <- -2 to 2
          ) drawBlock(position.add(x, 0, z), SkylandsMod.skylands.beanstem.getDefaultState)
        }

        val radius: Double = Math.min(progress.toDouble / 5, 10)
        val position = pos.add(Math.sin(progress.toDouble / 20) * radius, progress, Math.cos(progress.toDouble / 20) * radius)

        if (position.getY > 350)
          world.setBlockState(pos, SkylandsMod.skylands.beanstem.getDefaultState)

        drawLayer(position)

        progress += 1

      case _ =>
    }
  }


  private def isReplaceable(world: World, pos: BlockPos): Boolean = {
    val state = world.getBlockState(pos)
    val block = state.getBlock

    state.getBlock.isAir(state, world, pos) ||
      state.getBlock.isLeaves(state, world, pos) ||
      state.getBlock.isWood(world, pos) ||
      Seq(Material.AIR, Material.LEAVES).contains(block.getDefaultState.getMaterial) ||
      Seq(Blocks.GRASS, Blocks.DIRT, Blocks.LOG, Blocks.LOG2, Blocks.SAPLING, Blocks.VINE).contains(block)

  }
}