package marczeugs.emotes;

import net.minecraft.util.Identifier;

public class Emote {
	public Identifier identifier;
	public int width;
	public int height;
	public int textureWidth;
	public int textureHeight;
	public boolean animated;
	public int frames;
	public int rows;
	public int delay;
	
	public Emote(Identifier identifier, int width, int height, int textureWidth, int textureHeight, boolean animated, int frames, int rows, int delay) {
		this.identifier = identifier;
		this.width = width;
		this.height = height;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.animated = animated;
		this.frames = frames;
		this.rows = rows;
		this.delay = delay;
	}
}
