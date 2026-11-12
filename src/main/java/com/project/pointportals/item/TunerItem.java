package com.project.pointportals.item;

import com.project.pointportals.block.TeleporterBlock;
import com.project.pointportals.component.PortalTarget;
import com.project.pointportals.registry.ModBlocks;
import com.project.pointportals.registry.ModDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TunerItem extends Item {
	public TunerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos clickedPos = context.getClickedPos();
		BlockState clickedState = level.getBlockState(clickedPos);
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();

		// Only interact with Teleporter blocks
		if (!clickedState.is(ModBlocks.TELEPORTER)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		// Crouch + Right Click: Clear tuner memory
		if (player != null && player.isShiftKeyDown()) {
			if (stack.has(ModDataComponents.PORTAL_TARGET)) {
				stack.remove(ModDataComponents.PORTAL_TARGET);
				level.playSound(null, clickedPos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 1.2F);
				player.sendOverlayMessage(Component.translatable("message.pointportals.tuner_cleared").withStyle(ChatFormatting.YELLOW));
				return InteractionResult.SUCCESS;
			}
		}

		PortalTarget storedTarget = stack.get(ModDataComponents.PORTAL_TARGET);

		if (storedTarget == null) {
			// First Node: record portal coordinates and dimension
			stack.set(ModDataComponents.PORTAL_TARGET, PortalTarget.of(level, clickedPos));
			level.playSound(null, clickedPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
			if (player != null) {
				player.sendOverlayMessage(
					Component.translatable("message.pointportals.first_portal_selected", clickedPos.getX(), clickedPos.getY(), clickedPos.getZ())
						.withStyle(ChatFormatting.AQUA)
				);
			}
			return InteractionResult.SUCCESS;
		} else {
			// Second Node: validate against self-linking
			if (storedTarget.dimension().equals(level.dimension()) && storedTarget.pos().equals(clickedPos)) {
				level.playSound(null, clickedPos, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0F, 0.8F);
				if (player != null) {
					player.sendOverlayMessage(
						Component.translatable("message.pointportals.cannot_self_link").withStyle(ChatFormatting.RED)
					);
				}
				return InteractionResult.FAIL;
			}

			// Clear tuner memory upon completing link selection
			stack.remove(ModDataComponents.PORTAL_TARGET);
			level.playSound(null, clickedPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.5F);

			// Mark current block as linked
			level.setBlock(clickedPos, clickedState.setValue(TeleporterBlock.LINKED, true), 3);

			if (player != null) {
				player.sendOverlayMessage(
					Component.translatable("message.pointportals.portals_linked").withStyle(ChatFormatting.GREEN)
				);
			}
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.has(ModDataComponents.PORTAL_TARGET) || super.isFoil(stack);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void appendHoverText(
		ItemStack itemStack,
		Item.TooltipContext context,
		TooltipDisplay display,
		Consumer<Component> builder,
		TooltipFlag tooltipFlag
	) {
		super.appendHoverText(itemStack, context, display, builder, tooltipFlag);

		PortalTarget target = itemStack.get(ModDataComponents.PORTAL_TARGET);
		if (target != null) {
			target.addToTooltip(context, builder, tooltipFlag, itemStack.getComponents());
		} else {
			builder.accept(
				Component.translatable("tooltip.pointportals.tuner_unbound").withStyle(ChatFormatting.DARK_GRAY)
			);
		}
	}
}
