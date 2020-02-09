package com.bagel.rosewood.common.world.gen.feature;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import com.bagel.rosewood.core.registry.RosewoodBlocks;
import com.mojang.datafixers.Dynamic;

import net.minecraft.block.LogBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorldWriter;
import net.minecraft.world.gen.IWorldGenerationReader;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

@SuppressWarnings("unused")
public class RosewoodTreeFeature extends AbstractTreeFeature<NoFeatureConfig> {
	public RosewoodTreeFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> p_i51443_1_, boolean p_i51443_2_) {
		super(p_i51443_1_, p_i51443_2_);
	}

	public boolean place(Set<BlockPos> changedBlocks, IWorldGenerationReader worldIn, Random rand, BlockPos position,
			MutableBoundingBox boundsIn) {

		int branches = 2 + rand.nextInt(3);
		int height = 4 + rand.nextInt(2) + rand.nextInt(3) + rand.nextInt(3);
		int bonusCanopies = rand.nextInt(3);

		boolean flag = true;

		int i = height;// rand.nextInt(3) + rand.nextInt(3) + 5;
		if (position.getY() >= 1 && position.getY() + i + 1 <= worldIn.getMaxHeight()) {
			for (int j = position.getY(); j <= position.getY() + 1 + i; ++j) {
				int k = 1;
				if (j == position.getY()) {
					k = 0;
				}

				if (j >= position.getY() + 1 + i - 2) {
					k = 2;
				}

				BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

				for (int l = position.getX() - k; l <= position.getX() + k && flag; ++l) {
					for (int i1 = position.getZ() - k; i1 <= position.getZ() + k && flag; ++i1) {
						if (j >= 0 && j < worldIn.getMaxHeight()) {
							if (!func_214587_a(worldIn, blockpos$mutableblockpos.setPos(l, j, i1))) {
								flag = false;
							}
						} else {
							flag = false;
						}
					}
				}
			}

			if (!flag) {
				return false;
			} else if (isSoil(worldIn, position.down(), getSapling()) && position.getY() < worldIn.getMaxHeight() - branches - 1) {
				//base log
				this.setDirtAt(worldIn, position.down(), position);

				Direction direction = Direction.Plane.HORIZONTAL.random(rand);

				int logX = position.getX();
				int logZ = position.getZ();
				boolean canopy = false;

				for (int k1 = 0; k1 < height; ++k1) {
					int logY = position.getY() + k1;
					BlockPos blockpos = new BlockPos(logX, logY, logZ);
					if (isAirOrLeaves(worldIn, blockpos)) {
						this.placeLogAt(changedBlocks, worldIn, blockpos, boundsIn, Direction.UP);
					}
					if (rand.nextInt(6) == 0 && k1 > 3 && k1 < height && canopy == false) {
						int leafSize = 1 + rand.nextInt(2);
						for(int k3 = -leafSize; k3 <= leafSize; ++k3) {
							for(int j4 = -leafSize; j4 <= leafSize; ++j4) {
								if (Math.abs(k3) != leafSize || Math.abs(j4) != leafSize) {
									this.placeLeafAt(changedBlocks, worldIn, blockpos.add(k3, 0, j4), boundsIn);
								}
							}
						}
						canopy = true;
						
					}
				}

				//branches
				ArrayList<String> directions = new ArrayList<String>();

				for (int k2 = 0; k2 < branches; ++k2) {
					Direction offset = Direction.Plane.HORIZONTAL.random(rand);

					while (directions.contains(offset.toString())) {
						offset = Direction.Plane.HORIZONTAL.random(rand);
					}
					directions.add(offset.toString());
					int turns = 1 + rand.nextInt(3);
					
					BlockPos currentPos = position.offset(Direction.UP, height - 1);
					int branchLength = 0;
					int branchHeight = 0;
					
					for (int k4 = 0; k4 < turns; ++k4) {
						branchLength = 1 + rand.nextInt(2) + rand.nextInt(2);
						branchHeight = 1 + rand.nextInt(3) + rand.nextInt(2);
						createHorizontalLog(branchLength, changedBlocks, worldIn, currentPos, offset, boundsIn);
						createVerticalLog(branchHeight, changedBlocks, worldIn, currentPos.offset(offset, branchLength), boundsIn, rand);
						currentPos = currentPos.offset(offset, branchLength).offset(Direction.UP, branchHeight);
					}
					
					int leafSize = 2 + rand.nextInt(2);
					int leafSizeTop = 0;
					if (leafSize == 2) {
						leafSizeTop = leafSize - 1;
					} else {
						leafSizeTop = leafSize - 1 - rand.nextInt(2);
					}
					//first layer of leaves
					for(int k3 = -leafSize; k3 <= leafSize; ++k3) {
						for(int j4 = -leafSize; j4 <= leafSize; ++j4) {
							if (Math.abs(k3) != leafSize || Math.abs(j4) != leafSize) {
								this.placeLeafAt(changedBlocks, worldIn, currentPos.add(k3, 0, j4), boundsIn);
							}
						}
					}
					
					//second layer of leaves
					currentPos = currentPos.offset(Direction.UP, 1);
					for(int k3 = -leafSizeTop; k3 <= leafSizeTop; ++k3) {
						for(int j4 = -leafSizeTop; j4 <= leafSizeTop; ++j4) {
							if (Math.abs(k3) != leafSizeTop || Math.abs(j4) != leafSizeTop) {
								this.placeLeafAt(changedBlocks, worldIn, currentPos.add(k3, 0, j4), boundsIn);
							}
						}
					}

					logX = position.getX();
					logZ = position.getZ();
					

				}

				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	private void createHorizontalLog(
			int branchLength,
			Set<BlockPos> changedBlocks, 
			IWorldGenerationReader worldIn, 
			BlockPos pos, 
			Direction direction, 
			MutableBoundingBox boundsIn) {
		
		int logX = pos.getX();
		int logY = pos.getY();
		int logZ = pos.getZ();
		
		for (int k3 = 0; k3 < branchLength; ++k3) {
			
			logX += direction.getXOffset();
			logZ += direction.getZOffset();
			
			BlockPos blockpos1 = new BlockPos(logX, logY, logZ);
			if (isAirOrLeaves(worldIn, blockpos1)) {
				this.placeLogAt(changedBlocks, worldIn, blockpos1, boundsIn, direction);
			}
		}
	}
	
	private void createVerticalLog(
			int branchHeight,
			Set<BlockPos> changedBlocks, 
			IWorldGenerationReader worldIn, 
			BlockPos pos, 
			MutableBoundingBox boundsIn, Random rand) {
		
		int logX = pos.getX();
		int logY = pos.getY();
		int logZ = pos.getZ();
		boolean canopy = false;
		
		for (int k1 = 0; k1 < branchHeight; ++k1) {
			
			logY += 1;
			
			BlockPos blockpos = new BlockPos(logX, logY, logZ);
			if (isAirOrLeaves(worldIn, blockpos)) {
				this.placeLogAt(changedBlocks, worldIn, blockpos, boundsIn, Direction.UP);	
			}
			if (rand.nextInt(6) == 0 && canopy == false) {
				int leafSize = 1 + rand.nextInt(2);
				for(int k3 = -leafSize; k3 <= leafSize; ++k3) {
					for(int j4 = -leafSize; j4 <= leafSize; ++j4) {
						if (Math.abs(k3) != leafSize || Math.abs(j4) != leafSize) {
							this.placeLeafAt(changedBlocks, worldIn, blockpos.add(k3, 0, j4), boundsIn);
						}
					}
				}
				canopy = true;
			}
		}
	}

	private void placeLogAt(Set<BlockPos> changedBlocks, IWorldWriter worldIn, BlockPos pos,
			MutableBoundingBox boundsIn, Direction direction) {
		this.setLogState(changedBlocks, worldIn, pos,
				RosewoodBlocks.ROSEWOOD_LOG.get().getDefaultState().with(LogBlock.AXIS, direction.getAxis()),
				boundsIn);
	}

	private void placeLeafAt(Set<BlockPos> worldIn, IWorldGenerationReader pos, BlockPos p_175924_3_,
			MutableBoundingBox boundsIn) {
		if (isAirOrLeaves(pos, p_175924_3_)) {
			this.setLogState(worldIn, pos, p_175924_3_, RosewoodBlocks.ROSEWOOD_LEAVES.get().getDefaultState(),
					boundsIn);
		}

	}
}