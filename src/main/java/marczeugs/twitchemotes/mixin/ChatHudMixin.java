package marczeugs.twitchemotes.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.platform.GlStateManager;

import marczeugs.twitchemotes.ChatLineEmote;
import marczeugs.twitchemotes.TwitchEmotesMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.options.ChatVisibility;
import net.minecraft.util.Identifier;
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
	public void render(int int_1) {
		if (this.client.options.chatVisibility != ChatVisibility.HIDDEN) {
			if(Math.random() < 0.01) System.out.println("test");
			
			int int_2 = this.getVisibleLineCount();
			int int_3 = this.visibleMessages.size();
			if (int_3 > 0) {
				boolean boolean_1 = false;
				if (this.isChatFocused()) {
					boolean_1 = true;
				}

				double double_1 = this.getChatScale();
				int int_4 = MathHelper.ceil((double) this.getWidth() / double_1);
				GlStateManager.pushMatrix();
				GlStateManager.translatef(2.0F, 8.0F, 0.0F);
				GlStateManager.scaled(double_1, double_1, 1.0D);
				double double_2 = this.client.options.chatOpacity * 0.9D + 0.1;
				double double_3 = this.client.options.textBackgroundOpacity;
				int int_5 = 0;

				int int_7;
				int int_8;
				int int_9;
				for (int int_6 = 0; int_6 + this.scrolledLines < this.visibleMessages.size() && int_6 < int_2; ++int_6) {
					ChatHudLine chatHudLine_1 = (ChatHudLine) this.visibleMessages.get(int_6 + this.scrolledLines);
					if (chatHudLine_1 != null) {
						int_7 = int_1 - chatHudLine_1.getTimestamp();
						if (int_7 < 200 || boolean_1) {
							double double_4 = boolean_1 ? 1.0D : method_19348(int_7);
							int_8 = (int) (255.0D * double_4 * double_2);
							int_9 = (int) (255.0D * double_4 * double_3);
							++int_5;
							if (int_8 > 3) {
								int int_11 = -int_6 * 15;
								fill(-2, int_11 - 15, 0 + int_4 + 4, int_11, int_9 << 24);
								
								// CHANGED
								GlStateManager.enableBlend();
								
								String chatLineText = chatHudLine_1.getText().asFormattedString();
								
								List<Object> chatLineParts = new ArrayList<Object>();
								chatLineParts.add(chatLineText);
								
								for(Entry<String, Identifier> entry : TwitchEmotesMod.twitchEmotes.entrySet()) {
									List<Object> nextParts = new ArrayList<Object>();
									
									for(Object nextPart : chatLineParts) {
										if(nextPart instanceof String) {
											String[] stringParts = ((String) nextPart).split("(?:^| )" + entry.getKey() + "(?:$| )", -1);
											
											for(int i = 0; i < stringParts.length; i++) {
												nextParts.add(stringParts[i]);
												
												if(i != stringParts.length - 1) {
													nextParts.add(" ");
													nextParts.add(new ChatLineEmote(entry.getValue()));
													nextParts.add(" ");
												}
											}
										} else {
											nextParts.add(nextPart);
										}
									}
									
									chatLineParts = nextParts;
								}
								
								float xOffset = 0;
								
								for(Object chatLinePart : chatLineParts) {
									if(chatLinePart instanceof String) {
										this.client.textRenderer.drawWithShadow((String) chatLinePart, xOffset, (float) (int_11 - 11), 16777215 + (int_8 << 24));
										xOffset += this.client.textRenderer.getStringWidth((String) chatLinePart);
									} else {
										client.getTextureManager().bindTexture(((ChatLineEmote) chatLinePart).identifier);
									    GlStateManager.color4f(1.0F, 1.0F, 1.0F, (float) int_8 / 255);
									    DrawableHelper.blit((int) xOffset, int_11 - 14, 0, 0.0f, 0.0f, 13, 13, 13, 13);
									      
										xOffset += 14;
									}
								}
								// /CHANGED
								
								GlStateManager.disableAlphaTest();
								GlStateManager.disableBlend();
							}
						}
					}
				}

				if (boolean_1) {
					this.client.textRenderer.getClass();
					int int_12 = 9;
					GlStateManager.translatef(-3.0F, 0.0F, 0.0F);
					int int_13 = int_3 * int_12 + int_3;
					int_7 = int_5 * int_12 + int_5;
					int int_15 = this.scrolledLines * int_7 / int_3;
					int int_16 = int_7 * int_7 / int_13;
					if (int_13 != int_7) {
						int_8 = int_15 > 0 ? 170 : 96;
						int_9 = this.field_2067 ? 13382451 : 3355562;
						fill(0, -int_15, 2, -int_15 - int_16, int_9 + (int_8 << 24));
						fill(2, -int_15, 1, -int_15 - int_16, 13421772 + (int_8 << 24));
					}
				}

				GlStateManager.popMatrix();
			}
		}
	}
}
