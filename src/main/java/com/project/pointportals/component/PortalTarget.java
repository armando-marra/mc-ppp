package com.project.pointportals.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;

/**
 * Data Component holding the coordinate and dimension of a selected portal.
 * Implements modern TooltipProvider for rich in-game inspection.
 */
public record PortalTarget(ResourceKey<Level> dimension, BlockPos pos) implements TooltipProvider {
	public static final Codec<PortalTarget> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(PortalTarget::dimension),
			BlockPos.CODEC.fieldOf("pos").forGetter(PortalTarget::pos)
		).apply(instance, PortalTarget::new)
	);

	public static final StreamCodec<ByteBuf, PortalTarget> STREAM_CODEC = StreamCodec.composite(
		ResourceKey.streamCodec(Registries.DIMENSION), PortalTarget::dimension,
		BlockPos.STREAM_CODEC, PortalTarget::pos,
		PortalTarget::new
	);

	public static PortalTarget of(Level level, BlockPos pos) {
		return new PortalTarget(level.dimension(), pos);
	}

	public static PortalTarget fromGlobalPos(GlobalPos globalPos) {
		return new PortalTarget(globalPos.dimension(), globalPos.pos());
	}

	public GlobalPos toGlobalPos() {
		return GlobalPos.of(dimension, pos);
	}

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
		consumer.accept(
			Component.translatable(
				"tooltip.pointportals.tuner_target",
				dimension.identifier().toString(),
				pos.getX(),
				pos.getY(),
				pos.getZ()
			).withStyle(ChatFormatting.AQUA)
		);
	}
}
