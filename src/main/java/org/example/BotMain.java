package org.example;

import database.RoleDatabase;
import exceptions.RoleNotFoundException;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
            JDABuilder.createDefault(token, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
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
            // Assign the "Member role first"
            Role memberRole = guild.getRolesByName("Member", true).stream().findFirst()
                    .orElseThrow(()-> new RoleNotFoundException("Role 'Member' not found!"));

            guild.addRoleToMember(member, memberRole).queue(
                    success -> System.out.println("Assigned 'Member' role to: " + member.getUser().getName()),
                    error -> System.err.println("Failed to assign role: " + error.getMessage())
            );

            // Retrieve the custom role name for this guild from the database
            String roleName = RoleDatabase.getRoleForGuild(guild.getId());
            if (roleName == null) {
                System.out.println("No custom role set for this guild.");
                return; // If no custom role is set, do nothing
            }

            // Retrieve the custom role by its name from the guild
            Role customRole = guild.getRolesByName(roleName, true).stream().findFirst()
                    .orElseThrow(() -> new RoleNotFoundException("Custom role '" + roleName + "' not found!"));

            // Assign the custom role to the new member
            guild.addRoleToMember(member, customRole).queue(
                    success -> System.out.println("Assigned custom role '" + roleName + "' to: " + member.getUser().getName()),
                    error -> System.err.println("Failed to assign custom role: " + error.getMessage())
            );

        } catch(RoleNotFoundException e){
            System.err.println(e.getMessage());;
        } catch (PermissionException e){
            System.out.println("Bot lacks permission to assign roles!");
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        String message = event.getMessage().getContentRaw();

        if(message.startsWith("!setRole")){
            if(!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)){
                event.getChannel().sendMessage("You must be an admin to set the role!").queue();
                return;
            }

            String roleName = message.substring(9).trim();
            if(roleName.isEmpty()){
                event.getChannel().sendMessage("Please specify a role name.").queue();
                return;
            }

            // Retrieve the role by its name
            Role role = event.getGuild().getRolesByName(roleName, true).stream().findFirst().orElse(null);
            if (role == null) {
                event.getChannel().sendMessage("Role not found!").queue();
                return;
            }

            // Saving the role's ID and name to the db
            RoleDatabase.SaveRoleForGuild(event.getGuild().getId(), role.getId(), roleName);
            event.getChannel().sendMessage("Custom role set to: " + roleName).queue();
        }

        if(message.startsWith("!removeRole")){
            if(!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)){
                event.getChannel().sendMessage("You must be an admin to remove the custom role!").queue();
                return;
            }

            RoleDatabase.removeRoleFromGuild(event.getGuild().getId());
            event.getChannel().sendMessage("Custom role assignment has been removed. New members will not get the custom role").queue();
        }
    }
}