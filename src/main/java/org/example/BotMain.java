package org.example;

import exceptions.RoleNotFoundException;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;


public class BotMain extends ListenerAdapter {
    public static void main(String[] args) {
        String token = System.getenv("DISCORD_TOKEN");
        System.out.println("Token: " + token);
        if (token == null || token.isEmpty()) {
            System.out.println("Error: Token is null or empty.");
            return;
        }

        try{
            JDABuilder.createDefault(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                    .addEventListeners(new BotMain())
                    .build();

            System.out.println("Bot started successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: Provided token is invalid or null.");
        } catch (Exception e){
            System.out.println("An unexpected error has occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){
        System.out.println("New member joined: " + event.getMember().getUser().getName());
        Guild guild = event.getGuild();
        Member member = event.getMember();

        try {
            Role memberRole = guild.getRolesByName("Member", true).stream().findFirst()
                    .orElseThrow(()-> new RoleNotFoundException("Role 'Member' not found!"));

            guild.addRoleToMember(member, memberRole).queue(
                    success -> System.out.println("Assigned 'Member' role to: " + member.getUser().getName()),
                    error -> System.err.println("Failed to assign role: " + error.getMessage())
            );
        } catch(RoleNotFoundException e){
            System.err.println(e.getMessage());;
        } catch (PermissionException e){
            System.out.println("Bot lacks permission to assign roles!");
        }
    }
}