package marczeugs.twitchemotes.mixin;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import marczeugs.twitchemotes.TwitchEmotesMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.server.command.CommandSource;

@Mixin(CommandSuggestor.class)
public class CommandSuggestorMixin {
	@Shadow @Final private MinecraftClient client;
	
	@Redirect(method = "refresh()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandSource;suggestMatching(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;"))
	public CompletableFuture<?> redirectSuggestMatching(Iterable<String> collection, SuggestionsBuilder builder) {
		Collection<String> playerNames = this.client.player.networkHandler.getCommandSource().getPlayerNames();
		Collection<String> allSuggestions = Stream.concat(playerNames.stream(), TwitchEmotesMod.twitchEmoteNames.stream()).collect(Collectors.toList());
		return CommandSource.suggestMatching(allSuggestions, builder);
	}
}
