package marczeugs.twitchemotes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import marczeugs.twitchemotes.mixin.TextureManagerAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class TwitchEmotesMod implements ClientModInitializer, SimpleSynchronousResourceReloadListener {
	public static final double CHAT_LINE_BASE_HEIGHT = 11.0d;
	public static final double CHAT_LINE_EMOTE_HEIGHT = 15.0d;

	public static Map<String, Emote> twitchEmotes = new HashMap<String, Emote>();
	public static Set<String> twitchEmoteNames = new HashSet<String>();
	public static Pattern emotePattern = Pattern.compile("e{2000}");
	public static Pattern mentionPattern = Pattern.compile("e{2000}");
	public static long startTimestamp = System.currentTimeMillis();
	
	private static final Logger LOGGER = LogManager.getLogger();

	private Gson gson = (new GsonBuilder()).create();
	
	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
		TwitchEmotesMod.mentionPattern = Pattern.compile(
			"(?:^| )(?:§[0-9a-z])*(?:@){0,1}(?i:" + 
			MinecraftClient.getInstance().getSession().getUsername() + 
			")(?=(?:§[0-9a-z])*(?:$| ))"
		);
		
		LOGGER.info("[TwitchEmotes] Mod loaded.");
	}
	
	@Override
	public Identifier getFabricId() {
		return new Identifier("twitchemotes");
	}

	@Override
	public void apply(ResourceManager resourceManager) {
		twitchEmotes.clear();
		
		try {
			List<Resource> emotePackMetadata = null;
			
			try {
				emotePackMetadata = resourceManager.getAllResources(new Identifier("twitchemotes", "emotes.json"));
			} catch(FileNotFoundException e) {
				LOGGER.warn("[TwitchEmotes] Unable to find any emote data packs, your chat experience will be vanilla.");
				return;
			}
			
				
			for(Resource emotePackInfo : emotePackMetadata) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(emotePackInfo.getInputStream(), StandardCharsets.UTF_8));
				JsonObject emoteData = JsonHelper.deserialize(gson, reader, JsonObject.class);
				
				for(Entry<String, JsonElement> entry : emoteData.entrySet()) {
					String fileName;
					int frames = 1;
					int rows = 1;
					int delay = 0;
					boolean animated = false;
					
					if(entry.getValue().isJsonObject()) {
						fileName = entry.getValue().getAsJsonObject().get("name").getAsString();
						frames = entry.getValue().getAsJsonObject().get("frames").getAsInt();
						rows = entry.getValue().getAsJsonObject().has("rows") ? entry.getValue().getAsJsonObject().get("rows").getAsInt() : frames;
						delay = entry.getValue().getAsJsonObject().get("delay").getAsInt();
						animated = true;
					} else {
						fileName = entry.getValue().getAsString();
					}
					
					Identifier identifier = new Identifier("twitchemotes", "emotes/" + fileName + ".png");
				
					try {
						NativeImage texture = ResourceTexture.TextureData.load(
							((TextureManagerAccessor) MinecraftClient.getInstance().getTextureManager()).getResourceContainer(), 
							identifier
						).getImage();

						TwitchEmotesMod.twitchEmotes.put(entry.getKey(), new Emote(
							identifier,
							texture.getWidth() / ((int) Math.ceil(((float) frames) / ((float) rows))),
							texture.getHeight() / rows,
							texture.getWidth(),
							texture.getHeight(),
							animated,
							frames,
							rows,
							delay
						));
						
						texture.close();
					} catch(IOException e) {
						LOGGER.warn("[TwitchEmotes] Unable to load emote {}.", entry.getKey());
					}
				}
			}
			
			TwitchEmotesMod.twitchEmoteNames = TwitchEmotesMod.twitchEmotes.keySet();
			
			
			TwitchEmotesMod.emotePattern = Pattern.compile(
				"(?:^|[ \\(\\[\\{<])(?:§[0-9a-z])*(" + 
				TwitchEmotesMod.twitchEmotes.keySet().stream().map(name -> "\\Q" + name + "\\E").collect(Collectors.joining("|")) + 
				")(?=(?:§[0-9a-z])*(?:$|[ \\)\\]\\}>]))"
			);

			
			LOGGER.info("[TwitchEmotes] Loaded {} emote pack(s) containing {} emote(s).", emotePackMetadata.size(), TwitchEmotesMod.twitchEmotes.size());
		} catch(IOException e) {
			LOGGER.warn("[TwitchEmotes] Unable to load any emote data:", e);
		}
	}
}
