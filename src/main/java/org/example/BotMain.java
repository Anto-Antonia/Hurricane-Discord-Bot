package org.example;

import database.ProfanityFilterDatabase;
import database.RoleDatabase;
import database.WarningTracker;
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

import java.util.List;


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
        String guildId = event.getGuild().getId();
//
//        boolean isFiltered = ProfanityFilterDatabase.isFilterEnabled(guildId);
////        if(isFiltered){
////            java.util.List<String> bannedWords = ProfanityFilterDatabase.getBannedWords(guildId);
////
////            for(String bannedWord : bannedWords){
////                if(message.contains(bannedWord)){
////                    event.getChannel().sendMessage(
////                            event.getAuthor().getAsMention() + " please avoid using inappropriate language. This is your warning."
////                    ).queue();
////
////                    return;
////                }
////            }
////        }


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

        // profanity filter toggle
        if(message.startsWith("!toggleProfanityFilter")){
            if(!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)){
                event.getChannel().sendMessage("Only admins can toggle the curse filter!").queue();
                return;
            }

            boolean currentStatus = ProfanityFilterDatabase.isFilterEnabled(event.getGuild().getId());
            boolean newStatus = !currentStatus;

            ProfanityFilterDatabase.toggleFilter(event.getGuild().getId(), newStatus);

            String statusMsg = newStatus ? "enabled" : "disabled";
            event.getChannel().sendMessage("The profanity filter has been **" + statusMsg + "**.").queue();
        }

        // adding banned words
        if(message.startsWith("!addBadWord")){
            if(!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)){
                event.getChannel().sendMessage("Only admins can add curse words! Ask an admin to help you out.").queue();
            }

            String word = message.substring("!addBadWord".length()).trim().toLowerCase();
            if(word.isEmpty()){
                event.getChannel().sendMessage("Please specify a word to add.").queue();
            }

            ProfanityFilterDatabase.addBannedWord(event.getGuild().getId(), word);
            event.getChannel().sendMessage("Word `" + word + "` added to the profanity filter.").queue();
        }

        if(message.startsWith("!removeBadWord")){
            if(!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)){
                event.getChannel().sendMessage("Only admins can remove curse words!").queue();
            }

            String word = message.substring("!removeBadWord".length()).trim().toLowerCase();
            if(word.isEmpty()){
                event.getChannel().sendMessage("Please specify a curse word to remove.").queue();
            }
            ProfanityFilterDatabase.removeBannedWord(event.getGuild().getId(), word);
            event.getChannel().sendMessage("Word `" + word + "` has been removed from the profanity filter.").queue();
        }

        if(message.startsWith("!listBannedWords")){
            if(!event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)){
                event.getChannel().sendMessage("only admins can view the banned word list.").queue();
                return;
            }
            List<String> bannedWords = ProfanityFilterDatabase.getBannedWords(event.getGuild().getId());
            if(bannedWords.isEmpty()){
                event.getChannel().sendMessage("No banned words set for this server.").queue();
            } else{
                String list = String.join(", ", bannedWords);
                event.getChannel().sendMessage("Banned words for this server are: `" + list + "`").queue();
            }
        }

        if(message.equalsIgnoreCase("!help")){
            String helpMessage = "Here are all the available commands:\n";

            // for the future, here will be the member commands

//            helpMessage += "**!setRole <role_name>** - Assign a custom role to new members.\n";
//            helpMessage += "**!removeRole <role_name>** - Remove the custom auto-assigned role.\n";
//            helpMessage += "**!addBadWord <word>** - Add a word to the server's profanity filter.\n";
//            helpMessage += "**!removeBadWord <word>** - Remove a word from the profanity filter.\n";
//            helpMessage += "**!toggleProfanityFilter** - Toggle the profanity filter on/off.\n";
//            helpMessage += "**!listBannedWords** - List currently banned words.\n";

            if(event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)){
                helpMessage += "\n**Admin Commands**:\n";
                helpMessage += "**!setRole <role_name>** - Assign a custom role to new members.\n";
                helpMessage += "**!removeRole <role_name>** - Remove the custom role.\n";
                helpMessage += "**!addBadWord <word>** - Add a word to the banned list.\n";
                helpMessage += "**!removeBadWord <word>** - Remove a word from the banned list.\n";
                helpMessage += "**!toggleProfanityFilter** - Turn the profanity filter on or off.\n";
                helpMessage += "**!listBannedWords** - Show the list of banned words.\n";
            }
            event.getChannel().sendMessage(helpMessage).queue();
        }

        // outside of the commands, adding the timeout functionality
        if(!event.getAuthor().isBot() && !message.startsWith("!")){
            String userId = event.getAuthor().getId();
            String content = message.toLowerCase();

            if(ProfanityFilterDatabase.isFilterEnabled(guildId)){
                for(String bannedWord : ProfanityFilterDatabase.getBannedWords(guildId)){
                    if(content.contains(bannedWord)){
                        int warning = WarningTracker.addWarning(guildId, userId);
                        event.getChannel().sendMessage(
                                String.format("%s, watch you language! ⚠\uFE0F Warning %d/3.",
                                        event.getAuthor().getAsMention(), warning)
                        ).queue();

                        if(warning >=3){
                            event.getGuild().timeoutFor(event.getMember(), java.time.Duration.ofMinutes(1))
                                    .queue(
                                            success -> {
                                                WarningTracker.resetWarning(guildId, userId);
                                                event.getChannel().sendMessage(
                                                        String.format("%s has been timed out for repeated bad language. ⛔", event.getAuthor().getAsMention())
                                                ).queue();
                                            },
                                            error -> {
                                                event.getChannel().sendMessage("Couldn't timeout the user. Do I have permissions?").queue();
                                            }
                                    );
                        }
                        break;
                    }
                }
            }
        }
    }
}