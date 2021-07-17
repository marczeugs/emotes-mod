package marczeugs.emotes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmotesMod implements ClientModInitializer, SimpleSynchronousResourceReloadListener {
	public static final double CHAT_LINE_BASE_HEIGHT = 12.0d;
	public static final double CHAT_LINE_EMOTE_HEIGHT = 15.0d;

	public static final Map<String, Emote> emotes = new HashMap<>();
	public static Set<String> emoteNames = new HashSet<>();
	public static Pattern emotePattern = Pattern.compile("e{2000}");
	public static Pattern mentionPattern = Pattern.compile("e{2000}");
	public static final long startTimestamp = System.currentTimeMillis();

	public static boolean captureNextNativeImageAsEmote = false;
	public static NativeImage lastEmoteImage;
	
	private static final Logger LOGGER = LogManager.getLogger();

	private final Gson gson = (new GsonBuilder()).create();
	
	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(this);
		EmotesMod.mentionPattern = Pattern.compile(
			"(?:^| )(?:§[0-9a-z])*@?(?i:" +
			MinecraftClient.getInstance().getSession().getUsername() + 
			")(?=(?:§[0-9a-z])*(?:$| ))"
		);
		
		LOGGER.info("[Emotes Mod] Mod loaded.");
	}
	
	@Override
	public Identifier getFabricId() {
		return new Identifier("emotes");
	}

	@Override
	public void reload(ResourceManager resourceManager) {
		EmotesMod.emotes.clear();
		
		try {
			List<Resource> emotePackMetadata;
			
			try {
				emotePackMetadata = resourceManager.getAllResources(new Identifier("emotes", "emotes.json"));
			} catch (FileNotFoundException e) {
				LOGGER.warn("[Emotes Mod] Unable to find any emote data packs, your chat experience will be vanilla.");
				return;
			}
			
				
			for (var emotePackInfo : emotePackMetadata) {
				var reader = new BufferedReader(new InputStreamReader(emotePackInfo.getInputStream(), StandardCharsets.UTF_8));
				var emoteData = JsonHelper.deserialize(gson, reader, JsonObject.class);

				assert emoteData != null;
				for (var entry : emoteData.entrySet()) {
					String fileName;
					var frames = 1;
					var rows = 1;
					var delay = 0;
					var animated = false;
					
					if (entry.getValue().isJsonObject()) {
						fileName = entry.getValue().getAsJsonObject().get("name").getAsString();
						frames = entry.getValue().getAsJsonObject().get("frames").getAsInt();
						rows = entry.getValue().getAsJsonObject().has("rows") ? entry.getValue().getAsJsonObject().get("rows").getAsInt() : frames;
						delay = entry.getValue().getAsJsonObject().get("delay").getAsInt();
						animated = true;
					} else {
						fileName = entry.getValue().getAsString();
					}

					var identifier = new Identifier("emotes", "emotes/" + fileName + ".png");
				
					try {
						EmotesMod.captureNextNativeImageAsEmote = true;

						var texture = new ResourceTexture(identifier);
						texture.load(MinecraftClient.getInstance().getResourceManager());

						EmotesMod.emotes.put(
								entry.getKey(),
								new Emote(
										identifier,
										EmotesMod.lastEmoteImage.getWidth() / ((int) Math.ceil(((float) frames) / ((float) rows))),
										EmotesMod.lastEmoteImage.getHeight() / rows,
										EmotesMod.lastEmoteImage.getWidth(),
										EmotesMod.lastEmoteImage.getHeight(),
										animated,
										frames,
										rows,
										delay
								)
						);
						
						texture.close();
					} catch (IOException e) {
						LOGGER.warn("[Emotes Mod] Unable to load emote {}, reason: ", entry.getKey(), e);
					}
				}

				emotePackInfo.close();
			}
			
			EmotesMod.emoteNames = EmotesMod.emotes.keySet();
			
			
			EmotesMod.emotePattern = Pattern.compile(
				"(?:^|[ (\\[{<])(?:§[0-9a-z])*(" +
				EmotesMod.emotes.keySet().stream().map(name -> "\\Q" + name + "\\E").collect(Collectors.joining("|")) + 
				")(?=(?:§[0-9a-z])*(?:$|[ )\\]}>]))"
			);

			
			LOGGER.info("[Emotes Mod] Loaded {} emote pack(s) containing {} emote(s).", emotePackMetadata.size(), EmotesMod.emotes.size());
		} catch (IOException e) {
			LOGGER.warn("[Emotes Mod] Unable to load any emote data: ", e);
		}
	}
}
