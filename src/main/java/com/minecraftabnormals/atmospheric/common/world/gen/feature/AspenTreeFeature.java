package com.minecraftabnormals.atmospheric.common.world.gen.feature;

import com.google.common.collect.Lists;
import com.minecraftabnormals.abnormals_core.core.util.TreeUtil;
import com.minecraftabnormals.atmospheric.core.registry.AtmosphericBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.block.SaplingBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AspenTreeFeature extends Feature<BaseTreeFeatureConfig> {

	public AspenTreeFeature(Codec<BaseTreeFeatureConfig> config) {
		super(config);
	}

	@Override
	public boolean place(ISeedReader worldIn, ChunkGenerator generator, Random rand, BlockPos position, BaseTreeFeatureConfig config) {
		int height = 12 + rand.nextInt(4) + rand.nextInt(5) + rand.nextInt(6);
		boolean flag = true;

		if (position.getY() >= 1 && position.getY() + height + 1 <= worldIn.getMaxBuildHeight()) {
			for (int j = position.getY(); j <= position.getY() + 1 + height; ++j) {
				int k = 1;
				if (j == position.getY()) {
					k = 0;
				}
				if (j >= position.getY() + 1 + height - 2) {
					k = 2;
				}
				BlockPos.Mutable mutable = new BlockPos.Mutable();

				for (int l = position.getX() - k; l <= position.getX() + k && flag; ++l) {
					for (int i1 = position.getZ() - k; i1 <= position.getZ() + k && flag; ++i1) {
						if (j >= 0 && j < worldIn.getMaxBuildHeight()) {
							if (!TreeUtil.isAirOrLeaves(worldIn, mutable.set(l, j, i1))) {
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
			} else if (TreeUtil.isValidGround(worldIn, position.below(), (SaplingBlock) AtmosphericBlocks.ASPEN_SAPLING.get()) && position.getY() < worldIn.getMaxBuildHeight()) {
				// base log
				TreeUtil.setDirtAt(worldIn, position.below());
				List<BlockPos> logsPlaced = new ArrayList<>();

				int logX = position.getX();
				int logZ = position.getZ();
				int leafHeight = height - 7 - rand.nextInt(3) - rand.nextInt(3);
				int branchHeight = leafHeight - 2 - rand.nextInt(3);
				int bonusBranchHeight = branchHeight - 2 - rand.nextInt(3);

				for (int k1 = 0; k1 < height; ++k1) {
					int logY = position.getY() + k1;
					BlockPos blockpos = new BlockPos(logX, logY, logZ);
					if (TreeUtil.isAirOrLeaves(worldIn, blockpos)) {
						TreeUtil.placeDirectionalLogAt(worldIn, blockpos, Direction.UP, rand, config);
						logsPlaced.add(blockpos);
					}

					if (k1 >= leafHeight) {
						for (Direction direction : Direction.values()) {
							if (direction.getAxis().getPlane() == Direction.Plane.HORIZONTAL) {
								TreeUtil.placeLeafAt(worldIn, blockpos.relative(direction), rand, config);
								BlockPos offsetPos = blockpos.relative(direction).relative(direction.getClockWise());
								if (k1 > leafHeight && k1 < height - 1 && (rand.nextInt(4) != 0 || worldIn.getBlockState(offsetPos.below()).isAir()))
									TreeUtil.placeLeafAt(worldIn, offsetPos, rand, config);
							}
						}

						// Third layer of leaves
						if (k1 > leafHeight + 1 && k1 < height - 2) {
							for (int k3 = -2; k3 <= 2; ++k3) {
								for (int j4 = -2; j4 <= 2; ++j4) {
									if ((Math.abs(k3) != 2 || Math.abs(j4) != 2) && rand.nextBoolean()) {
										TreeUtil.placeLeafAt(worldIn, blockpos.offset(k3, 0, j4), rand, config);
									}
								}
							}
						}
					} else if ((k1 == branchHeight && branchHeight > 3 && rand.nextInt(5) != 0) || (k1 == bonusBranchHeight && bonusBranchHeight > 2 && rand.nextInt(3) != 0)) {
						int branchSize = 1 + rand.nextInt(2);
						if (rand.nextBoolean())
							branchSize += 1 + rand.nextInt(2);
						ArrayList<Direction> usedDirections = Lists.newArrayList();
						while (usedDirections.size() < branchSize) {
							Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
							if (!usedDirections.contains(randomDirection))
								usedDirections.add(randomDirection);
						}
						for (Direction direction : usedDirections) {
							TreeUtil.placeLeafAt(worldIn, blockpos.relative(direction), rand, config);
						}
					}
				}

				TreeUtil.placeLeafAt(worldIn, new BlockPos(logX, height + 4, logZ), rand, config);

				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}