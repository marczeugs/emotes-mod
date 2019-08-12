package marczeugs.twitchemotes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class TwitchEmotesMod implements ModInitializer, SimpleSynchronousResourceReloadListener {
	public static Map<String, Identifier> twitchEmotes = new HashMap<String, Identifier>();
	private static final Logger LOGGER = LogManager.getLogger();
	private Gson gson = (new GsonBuilder()).create();
	
	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
		System.out.println("[TwitchEmotes] [INFO] Mod loaded.");
	}
	
	@Override
	public Identifier getFabricId() {
		return new Identifier("twitchemotes");
	}

	@Override
	public void apply(ResourceManager resourceManager) {
		twitchEmotes.clear();
		
		try {
			List<Resource> emotePackMetadata = resourceManager.getAllResources(new Identifier("twitchemotes", "emotes.json"));
			
			for(Resource emotePackInfo : emotePackMetadata) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(emotePackInfo.getInputStream(), StandardCharsets.UTF_8));
				JsonObject emoteData = JsonHelper.deserialize(gson, reader, JsonObject.class);
				
				for(Entry<String, JsonElement> entry : emoteData.entrySet())
					TwitchEmotesMod.twitchEmotes.put(entry.getKey(), new Identifier("twitchemotes", "emotes/" + entry.getValue().getAsString() + ".png"));
			}
			
			LOGGER.info("[TwitchEmotes] Loaded {} emote pack(s) containing {} emote(s).", emotePackMetadata.size(), TwitchEmotesMod.twitchEmotes.size());
		} catch (FileNotFoundException e) {
			LOGGER.warn("[TwitchEmotes] Unable to find any emote data packs, your chat experience will be vanilla.");
		} catch (IOException e) {
			LOGGER.warn("[TwitchEmotes] Unable to load any emote data:", e);
		}
	}
}
