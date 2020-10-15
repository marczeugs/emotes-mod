package marczeugs.emotes.mixin;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import marczeugs.emotes.ChatHudLineStyledPart;
import marczeugs.emotes.Emote;
import marczeugs.emotes.EmotesMod;
import marczeugs.emotes.mixinstates.ChatHudMixinState;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;


@Mixin(ChatHud.class)
public class ChatHudMixin extends DrawableHelper {
	@Shadow @Final private MinecraftClient client;
	
	
	@ModifyConstant(method = "render", constant = { @Constant(doubleValue = 9.0d) })
	public double getChatLineHeight(double variable) {
		return EmotesMod.CHAT_LINE_BASE_HEIGHT;
	}

	@ModifyConstant(method = "render", constant = { @Constant(doubleValue = -8.0d) })
	public double getChatTextYOffset(double variable) {
		return -9.0d;
	}


	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE", 
			target = "Lcom/mojang/blaze3d/systems/RenderSystem;pushMatrix()V"
		)
	)
	private void initialiseVariables(MatrixStack matrixStack, int i, CallbackInfo ci) {
		ChatHudMixinState.chatLineYOffset = 0;
	}

	@Inject(
		method = "render", 
		at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/gui/hud/ChatHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V",
			ordinal = 0
		),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void processChatLine(MatrixStack matrices, int tickDelta, CallbackInfo ci, int i, int j, boolean bl, double d, int k, double e, double f, double g, double h, int l, int m, ChatHudLine<OrderedText> chatHudLine, double o, int p, int q, int r, double s) {
		ChatHudMixinState.chatLineParts = new ArrayList<Object>();
		ChatHudMixinState.emoteInLine = false;
		ChatHudMixinState.mentionedInLine = false;

		List<ChatHudLineStyledPart> lineParts = new ArrayList<ChatHudLineStyledPart>();
		lineParts.add(new ChatHudLineStyledPart(null));
		chatHudLine.getText().accept((index, style, codePoint) -> {
			ChatHudLineStyledPart currentLinePart = lineParts.get(lineParts.size() - 1);

			if(!style.equals(currentLinePart.style)) {
				if(currentLinePart.style != null) {
					lineParts.add(new ChatHudLineStyledPart(style));
					currentLinePart = lineParts.get(lineParts.size() - 1);
				} else {
					currentLinePart.style = style;
				}
			}
			
			currentLinePart.textBuilder.append(Character.toChars(codePoint));
			return true;
		});
		
		for(ChatHudLineStyledPart linePart : lineParts) {
			String lineString = linePart.textBuilder.toString();
			Matcher emoteMatcher = EmotesMod.emotePattern.matcher(lineString);
			int lastMatchStart = 0;

			while(emoteMatcher.find()) {
				if(!ChatHudMixinState.emoteInLine)
					ChatHudMixinState.emoteInLine = true;

				ChatHudMixinState.chatLineParts.add(OrderedText.styledString(lineString.substring(lastMatchStart, emoteMatcher.start(1)), linePart.style));
				ChatHudMixinState.chatLineParts.add(EmotesMod.emotes.get(emoteMatcher.group(1)));
				lastMatchStart = emoteMatcher.end(1);
			}

			ChatHudMixinState.chatLineParts.add(OrderedText.styledString(lineString.substring(lastMatchStart), linePart.style));

			if(!ChatHudMixinState.mentionedInLine && EmotesMod.mentionPattern.matcher(lineString).find())
				ChatHudMixinState.mentionedInLine = true;
		}

		if(ChatHudMixinState.emoteInLine)
			ChatHudMixinState.chatLineYOffset += EmotesMod.CHAT_LINE_EMOTE_HEIGHT - EmotesMod.CHAT_LINE_BASE_HEIGHT;
	}

	@Redirect(
		method = "render",
		at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/gui/hud/ChatHud;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V"
		)
	)
	private void redirectFill(MatrixStack matrixStack, int x1, int y1, int x2, int y2, int color) {
		int messageBackgroundOpacity = (color >> 24) & 0xFF;
		
		DrawableHelper.fill(
			matrixStack,
			x1,
			y1 - ChatHudMixinState.chatLineYOffset,
			x2,
			y2 - ChatHudMixinState.chatLineYOffset + (ChatHudMixinState.emoteInLine
				? (int) (EmotesMod.CHAT_LINE_EMOTE_HEIGHT - EmotesMod.CHAT_LINE_BASE_HEIGHT)
				: 0
			), 
			ChatHudMixinState.mentionedInLine
				? ((128 << 16) + (MathHelper.clamp(messageBackgroundOpacity * 2, 0, (255 - (255 - messageBackgroundOpacity) / 2)) << 24))
				: (messageBackgroundOpacity << 24)
		);
	}

	@Redirect(
		method = "render",
		at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/OrderedText;FFI)I"
		)
	)
	private int redirectTextDraw(TextRenderer textRenderer, MatrixStack matrixStack, OrderedText orderedText, float x, float y, int color) {
		int messageOpacity = (color >> 24) & 0xFF;
		float textXOffset = 0;
		int lastTextDrawReturnValue = 0;

		for(Object chatLinePart : ChatHudMixinState.chatLineParts) {
			if(chatLinePart instanceof OrderedText) {
				lastTextDrawReturnValue = textRenderer.drawWithShadow(
					matrixStack, 
					(OrderedText) chatLinePart, 
					(int) textXOffset, 
					y - ChatHudMixinState.chatLineYOffset + (ChatHudMixinState.emoteInLine
						? (int) (EmotesMod.CHAT_LINE_EMOTE_HEIGHT - EmotesMod.CHAT_LINE_BASE_HEIGHT) / 2
						: 0
					), 
					color
				);
				textXOffset += textRenderer.getWidth((OrderedText) chatLinePart);
			} else {
				Emote emote = ((Emote) chatLinePart);
				int width = emote.width;
				int height = emote.height;
				
				this.client.getTextureManager().bindTexture(emote.identifier);
				RenderSystem.color4f(1.0f, 1.0f, 1.0f, (float) messageOpacity / 255.0f);

				int frame = emote.animated
					? ((int) (((System.currentTimeMillis() - EmotesMod.startTimestamp) / emote.delay) % emote.frames))
					: 0;
					
				DrawableHelper.drawTexture(
					matrixStack,
					(int) textXOffset, (int) y - ChatHudMixinState.chatLineYOffset - 1, 
					13 * width / height, 13, 
					(frame / (8192 / emote.height)) * emote.width, (frame % (8192 / emote.height)) * emote.height, 
					emote.width, emote.height, 
					emote.textureWidth, emote.textureHeight
				);
				// Arguments: drawX drawY drawWidth drawHeight texturePartX texturePartY
				// texturePartWidth texturePartHeight textureWidth textureHeight

				textXOffset += 13 * width / height + 1;
			}
		}

		return lastTextDrawReturnValue;
	}
}
