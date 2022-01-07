package com.example.discordbot;

import com.example.discordbot.eventListener.EventListener;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.Event;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import java.util.List;


@Configuration
@ComponentScan({"com.example"})
public class BotConfig {

    @Value("${token}")
    private String token;

    @Bean
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient(List<EventListener<T>> eventListeners) {
        GatewayDiscordClient client = DiscordClientBuilder.create(token)
                .build()
                .login()
                .block();

        for(EventListener<T> listener : eventListeners) {
            client.on(listener.getEventType())
                    .flatMap(listener::execute)
                    .onErrorResume(listener::handleError)
                    .subscribe();
        }

        // Get our application's ID
        long applicationId = client.getRestClient().getApplicationId().block();

        // Build our command's definition
        ApplicationCommandRequest greetCmdRequest = ApplicationCommandRequest.builder()
                .name("greet")
                .description("Greets You")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("name")
                        .description("Your name")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                ).build();

        // application ID and command definition are the same as the global command

        long guildId = 208023865127862272L; //Discord4J's server ID.

        client.getRestClient().getApplicationService()
                .createGuildApplicationCommand(applicationId, guildId, greetCmdRequest)
                .subscribe();

        return client;
    }

}
