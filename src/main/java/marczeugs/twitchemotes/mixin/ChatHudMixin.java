package marczeugs.twitchemotes.mixin;


import java.util.ArrayList;
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

import com.mojang.blaze3d.systems.RenderSystem;

import marczeugs.twitchemotes.Emote;
import marczeugs.twitchemotes.TwitchEmotesMod;
import marczeugs.twitchemotes.mixinstates.ChatHudMixinState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;


@Mixin(ChatHud.class)
public class ChatHudMixin extends DrawableHelper {
	@Shadow @Final private MinecraftClient client;
	
	
	@ModifyConstant(method = "render", constant = { @Constant(doubleValue = 9.0d) })
	public double getChatLineHeight(double variable) {
		return TwitchEmotesMod.CHAT_LINE_BASE_HEIGHT;
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
	private void processChatLine(MatrixStack matrixStack, int i, CallbackInfo ci, int j, int k, boolean bl, double d, int l, double e, double f, double g, double h, int m, int n, ChatHudLine chatHudLine, double p, int q, int r, int s, double t) {
		String chatLineText = chatHudLine.getText().getString();
		ChatHudMixinState.mentionedInLine = TwitchEmotesMod.mentionPattern.matcher(chatLineText).find();

		ChatHudMixinState.chatLineParts = new ArrayList<Object>();
		ChatHudMixinState.emoteInLine = false;

		Matcher emoteMatcher = TwitchEmotesMod.emotePattern.matcher(chatLineText);
		int lastMatchStart = 0;

		while(emoteMatcher.find()) {
			if(!ChatHudMixinState.emoteInLine)
			ChatHudMixinState.emoteInLine = true;

			ChatHudMixinState.chatLineParts.add(chatLineText.substring(lastMatchStart, emoteMatcher.start(1)));
			ChatHudMixinState.chatLineParts.add(TwitchEmotesMod.twitchEmotes.get(emoteMatcher.group(1)));
			lastMatchStart = emoteMatcher.end(1);
		}

		ChatHudMixinState.chatLineParts.add(chatLineText.substring(lastMatchStart));

		if(ChatHudMixinState.emoteInLine)
			ChatHudMixinState.chatLineYOffset += TwitchEmotesMod.CHAT_LINE_EMOTE_HEIGHT - TwitchEmotesMod.CHAT_LINE_BASE_HEIGHT;
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
				? (int) (TwitchEmotesMod.CHAT_LINE_EMOTE_HEIGHT - TwitchEmotesMod.CHAT_LINE_BASE_HEIGHT)
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
			target = "Lnet/minecraft/client/font/TextRenderer;drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"
		)
	)
	private int redirectTextDraw(TextRenderer textRenderer, MatrixStack matrixStack, Text text, float x, float y, int color) {
		int messageOpacity = (color >> 24) & 0xFF;
		float textXOffset = 0;
		int lastTextDrawReturnValue = 0;

		for(Object chatLinePart : ChatHudMixinState.chatLineParts) {
			if(chatLinePart instanceof String) {
				lastTextDrawReturnValue = textRenderer.drawWithShadow(
					matrixStack, 
					(String) chatLinePart, 
					(int) textXOffset, 
					y - ChatHudMixinState.chatLineYOffset + (ChatHudMixinState.emoteInLine
						? (int) (TwitchEmotesMod.CHAT_LINE_EMOTE_HEIGHT - TwitchEmotesMod.CHAT_LINE_BASE_HEIGHT) / 2
						: 0
					), 
					color
				);
				textXOffset += textRenderer.getStringWidth(new LiteralText((String) chatLinePart));
			} else {
				Emote emote = ((Emote) chatLinePart);
				int width = emote.width;
				int height = emote.height;
				
				this.client.getTextureManager().bindTexture(emote.identifier);
				RenderSystem.color4f(1.0f, 1.0f, 1.0f, (float) messageOpacity / 255.0f);

				int frame = emote.animated
					? ((int) (((System.currentTimeMillis() - TwitchEmotesMod.startTimestamp) / emote.delay) % emote.frames))
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
