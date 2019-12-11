package marczeugs.twitchemotes.mixin;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import marczeugs.twitchemotes.Emote;
import marczeugs.twitchemotes.TwitchEmotesMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.util.math.MathHelper;


@Mixin(ChatHud.class)
public class ChatHudMixin extends DrawableHelper {
	@Shadow @Final private MinecraftClient client;
	@Shadow @Final private List<ChatHudLine> visibleMessages;
	@Shadow private int scrolledLines;
	@Shadow private boolean hasUnreadNewMessages;

	@Shadow public int getVisibleLineCount() { return 0; }
	@Shadow public boolean isChatFocused() { return true; }
	@Shadow public int getWidth() { return 0; }
	@Shadow public double getChatScale() { return 0; }
	@Shadow private boolean method_23677() { return false; }
	@Shadow private static double getMessageOpacityMultiplier(int int_1) { return 0; }
	
	
	@Nullable @Overwrite
	public void render(int int_1) {
		if(this.method_23677()) {
			int visibleLineCount = this.getVisibleLineCount();
			int lineCount = this.visibleMessages.size();

			if(lineCount > 0) {
				boolean chatFocused = this.isChatFocused();

				double chatScale = this.getChatScale();
				int scaledChatWidth = MathHelper.ceil((double) this.getWidth() / chatScale);
				RenderSystem.pushMatrix();
				RenderSystem.translatef(2.0F, 8.0F, 0.0F);
				RenderSystem.scaled(chatScale, chatScale, 1.0D);
				double double_2 = this.client.options.chatOpacity * 0.9D + 0.1D;
				double double_3 = this.client.options.textBackgroundOpacity;
				int displayedLineCount = 0;
				Matrix4f matrix4f_1 = Matrix4f.method_24021(0.0F, 0.0F, -100.0F);

// CHANGED
				int lineYOffset = 0;
// /CHANGED

				int messageAge;
				int messageTextOpacity;
				int messageBackgroundOpacity;

				for(int i = 0; i + this.scrolledLines < this.visibleMessages.size() && i < visibleLineCount; ++i) {
					ChatHudLine chatLine = (ChatHudLine) this.visibleMessages.get(i + this.scrolledLines);

					if(chatLine != null) {
						messageAge = int_1 - chatLine.getCreationTick();

						if(messageAge < 200 || chatFocused) {
							double double_4 = chatFocused ? 1.0D : getMessageOpacityMultiplier(messageAge);
							messageTextOpacity = (int) (255.0D * double_4 * double_2);
							messageBackgroundOpacity = (int) (255.0D * double_4 * double_3);
							++displayedLineCount;

							if(messageTextOpacity > 3) {
// CHANGED
								String chatLineText = chatLine.getText().asFormattedString();
								boolean mentionedInLine = TwitchEmotesMod.mentionPattern.matcher(chatLineText).find();

								List<Object> chatLineParts = new ArrayList<Object>();
								chatLineParts.add(chatLineText);

								List<Object> patchedParts = new ArrayList<Object>();

								boolean emotesInText = false;

								for(Object nextPart : chatLineParts) {
									if(nextPart instanceof String) {
										String nextStringPart = (String) nextPart;
										Matcher emoteMatcher = TwitchEmotesMod.emotePattern.matcher(nextStringPart);
										int lastMatchStart = 0;

										while(emoteMatcher.find()) {
											if(!emotesInText)
												emotesInText = true;

											patchedParts.add(nextStringPart.substring(lastMatchStart, emoteMatcher.start(1)));
											patchedParts.add(TwitchEmotesMod.twitchEmotes.get(emoteMatcher.group(1)));
											lastMatchStart = emoteMatcher.end(1);
										}

										patchedParts.add(nextStringPart.substring(lastMatchStart));
									} else {
										patchedParts.add(nextPart);
									}
								}

								chatLineParts = patchedParts;

								int lineHeight = 11;
								if(emotesInText)
									lineHeight = 15;

								fill(
									matrix4f_1, -2, lineYOffset - lineHeight, scaledChatWidth + 4, lineYOffset,
									mentionedInLine
											? ((128 << 16) + (MathHelper.clamp(messageBackgroundOpacity * 2, 0,(255 - (255 - messageBackgroundOpacity) / 2)) << 24))
											: (messageBackgroundOpacity << 24)
								);

								GlStateManager.enableBlend();

								float textXOffset = 0;

								for(Object chatLinePart : chatLineParts) {
									if (chatLinePart instanceof String) {
										this.client.textRenderer.drawWithShadow(
											(String) chatLinePart, 
											textXOffset, (float) (lineYOffset - (8 + (lineHeight - 9) / 2)), 
											16777215 + (messageTextOpacity << 24)
										);
										textXOffset += this.client.textRenderer.getStringWidth((String) chatLinePart);
									} else {
										Emote emote = ((Emote) chatLinePart);
										int width = emote.width;
										int height = emote.height;

										client.getTextureManager().bindTexture(emote.identifier);
										GlStateManager.color4f(1.0F, 1.0F, 1.0F, (float) messageTextOpacity / 255);

										int frame = emote.animated
											? ((int) (((System.currentTimeMillis() - TwitchEmotesMod.startTimestamp) / emote.delay) % emote.frames))
											: 0;
										
										DrawableHelper.blit(
											(int) textXOffset, lineYOffset - 14, 
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

								lineYOffset -= lineHeight;
// /CHANGED
								RenderSystem.disableAlphaTest();
								RenderSystem.disableBlend();
							}
						}
					}
				}

				if(chatFocused) {
					this.client.textRenderer.getClass();
					int chatLineHeight = 9;
					RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
					int int_13 = lineCount * chatLineHeight + lineCount;
					messageAge = displayedLineCount * chatLineHeight + displayedLineCount;
					int int_15 = this.scrolledLines * messageAge / lineCount;
					int int_16 = messageAge * messageAge / int_13;
					if (int_13 != messageAge) {
						messageTextOpacity = int_15 > 0 ? 170 : 96;
						messageBackgroundOpacity = this.hasUnreadNewMessages ? 13382451 : 3355562;
						fill(0, -int_15, 2, -int_15 - int_16, messageBackgroundOpacity + (messageTextOpacity << 24));
						fill(2, -int_15, 1, -int_15 - int_16, 13421772 + (messageTextOpacity << 24));
					}
				}

				RenderSystem.popMatrix();
			}
		}
	}
	
	/*@Inject(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z"), locals = LocalCapture.PRINT)
	private void onAddMessage(Text text, int int_1, int int_2, boolean boolean_1, CallbackInfo ci, int in_3, List<Text> wrappedLines, boolean boolean_2) {
		//System.out.println(wrappedLines);
	}*/
}
