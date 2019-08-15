package marczeugs.twitchemotes.mixin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.platform.GlStateManager;

import marczeugs.twitchemotes.Emote;
import marczeugs.twitchemotes.TwitchEmotesMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.util.math.MathHelper;

@Mixin(ChatHud.class)
public class ChatHudMixin extends DrawableHelper {
	@Shadow @Final private MinecraftClient client;
	@Shadow @Final private List<ChatHudLine> visibleMessages;
	@Shadow private int scrolledLines;
	@Shadow private boolean field_2067;
	
	@Shadow public int getVisibleLineCount() { return 0; }
	@Shadow public boolean isChatFocused() { return true; }
	@Shadow public int getWidth() { return 0; }
	@Shadow public double getChatScale() { return 0; }
	@Shadow private static double method_19348(int int_1) { return 0; }
	
	
	@Nullable
	@Overwrite
	public void render(int currentTimeStamp) throws IOException {
		if(this.client.options.chatVisibility != ChatVisibility.HIDDEN) {
			int visibleLineCount = this.getVisibleLineCount();
			int lineCount = this.visibleMessages.size();
			
			if(lineCount > 0) {
				boolean chatFocused = this.isChatFocused();

				double chatScale = this.getChatScale();
				int scaledChatWidth = MathHelper.ceil((double) this.getWidth() / chatScale);
				GlStateManager.pushMatrix();
				GlStateManager.translatef(2.0F, 8.0F, 0.0F);
				GlStateManager.scaled(chatScale, chatScale, 1.0D);
				double chatOpacity = this.client.options.chatOpacity * 0.9D + 0.1;
				double textBackgroundOpacity = this.client.options.textBackgroundOpacity;
				int displayedLineCount = 0;
				
				// CHANGED
				int lineYOffset = 0;
				// /CHANGED
				

				int messageAge;
				int messageTextOpacity;
				int messageBackgroundOpacity;
				for(int i = 0; i + this.scrolledLines < this.visibleMessages.size() && i < visibleLineCount; ++i) {
					ChatHudLine chatLine = (ChatHudLine) this.visibleMessages.get(i + this.scrolledLines);
					if(chatLine != null) {
						messageAge = currentTimeStamp - chatLine.getTimestamp();
						if(messageAge < 200 || chatFocused) {
							double messageOpacity = chatFocused ? 1.0D : method_19348(messageAge);
							messageTextOpacity = (int) (255.0D * messageOpacity * chatOpacity);
							messageBackgroundOpacity = (int) (255.0D * messageOpacity * textBackgroundOpacity);
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
								if(emotesInText) lineHeight = 15;
								
								fill(-2, lineYOffset - lineHeight, scaledChatWidth + 4, lineYOffset, mentionedInLine ? 
									((128 << 16) + (MathHelper.clamp(messageBackgroundOpacity * 2, 0, (255 - (255 - messageBackgroundOpacity) / 2)) << 24)) : 
									(messageBackgroundOpacity << 24)
								);
								
								GlStateManager.enableBlend();
								
								
								float testXOffset = 0;
								
								for(Object chatLinePart : chatLineParts) {
									if(chatLinePart instanceof String) {
										this.client.textRenderer.drawWithShadow((String) chatLinePart, testXOffset, (float) (lineYOffset - (8 + (lineHeight - 9) / 2)), 16777215 + (messageTextOpacity << 24));
										
										testXOffset += this.client.textRenderer.getStringWidth((String) chatLinePart);
									} else {
										Emote emote = ((Emote) chatLinePart);
										int width = emote.width;
										int height = emote.height;
										
										client.getTextureManager().bindTexture(emote.identifier);
									    GlStateManager.color4f(1.0F, 1.0F, 1.0F, (float) messageTextOpacity / 255);
									    
									    
									    int frame = emote.animated ? ((int) (((System.currentTimeMillis() - TwitchEmotesMod.startTimestamp) / emote.delay) % emote.frames)) : 0;
									    DrawableHelper.blit((int) testXOffset, lineYOffset - 14, 13 * width / height, 13, 0.0f, emote.height * frame, emote.width, emote.height, emote.width, emote.height * emote.frames);
										// Arguments: drawX drawY drawWidth drawHeight texturePartX texturePartY texturePartWidth texturePartHeight textureWidth textureHeight
									    
									    testXOffset += 13 * width / height + 1;
									}
								}
								
								
								lineYOffset -= lineHeight;
								// /CHANGED
								
								GlStateManager.disableAlphaTest();
								GlStateManager.disableBlend();
							}
						}
					}
				}

				if(chatFocused) {
					this.client.textRenderer.getClass();
					int chatLineHeight = 9;
					GlStateManager.translatef(-3.0F, 0.0F, 0.0F);
					int int_13 = lineCount * chatLineHeight + lineCount;
					int int_17 = displayedLineCount * chatLineHeight + displayedLineCount;
					int int_15 = this.scrolledLines * int_17 / lineCount;
					int int_16 = int_17 * int_17 / int_13;
					if(int_13 != int_17) {
						messageTextOpacity = int_15 > 0 ? 170 : 96;
						messageBackgroundOpacity = this.field_2067 ? 13382451 : 3355562;
						fill(0, -int_15, 2, -int_15 - int_16, messageBackgroundOpacity + (messageTextOpacity << 24));
						fill(2, -int_15, 1, -int_15 - int_16, 13421772 + (messageTextOpacity << 24));
					}
				}

				GlStateManager.popMatrix();
			}
		}
	}
}
