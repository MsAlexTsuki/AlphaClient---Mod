package com.alpharip.staffmod.AlphaClans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClanCommandHandler {
    private final ClanService service;
    private final ClanPlayerDirectory players;
    private final ClanCommandParser parser = new ClanCommandParser();

    public ClanCommandHandler(ClanService service, ClanPlayerDirectory players) {
        if (service == null || players == null) throw new IllegalArgumentException("Service and player directory are required");
        this.service = service;
        this.players = players;
    }

    public List<String> execute(UUID senderId, String senderName, String input) {
        List<String> output = new ArrayList<String>();
        try {
            ClanCommand command = parser.parse(input);
            List<String> args = command.getArguments();
            String targetName;
            UUID targetId;
            ClanResult result;
            switch (command.getName()) {
                case "help":
                    output.add("/clan create <name> <tag>");
                    output.add("/clan invite <player> | accept <clan-id> | decline <clan-id>");
                    output.add("/clan leave | kick <player> | promote <player> | demote <player>");
                    output.add("/clan transfer <player> | rename <name> | tag <tag> | disband");
                    output.add("/clan info | invites | chat <message>");
                    break;
                case "create":
                    if (args.size() < 2) { output.add("Usage: /clan create <name> <tag>"); break; }
                    result = service.createClan(senderId, senderName, command.joinedArguments(0).substring(0,
                            command.joinedArguments(0).lastIndexOf(' ')),
                            args.get(args.size() - 1));
                    output.add(result.getMessage());
                    break;
                case "invite":
                    targetId = resolve(command.argument(0), output);
                    if (targetId != null) output.add(service.invite(senderId, targetId).getMessage());
                    break;
                case "accept":
                    if (command.argument(0) == null) { output.add("Usage: /clan accept <clan-id>"); break; }
                    result = service.acceptInvite(senderId, UUID.fromString(command.argument(0)), senderName);
                    output.add(result.getMessage());
                    break;
                case "decline":
                    if (command.argument(0) == null) { output.add("Usage: /clan decline <clan-id>"); break; }
                    output.add(service.declineInvite(senderId, UUID.fromString(command.argument(0))).getMessage());
                    break;
                case "leave": output.add(service.leave(senderId).getMessage()); break;
                case "kick":
                    targetId = resolve(command.argument(0), output);
                    if (targetId != null) output.add(service.kick(senderId, targetId).getMessage());
                    break;
                case "promote":
                case "demote":
                    targetName = command.argument(0);
                    targetId = resolve(targetName, output);
                    if (targetId != null) output.add(service.setOfficer(senderId, targetId, "promote".equals(command.getName())).getMessage());
                    break;
                case "transfer":
                    targetId = resolve(command.argument(0), output);
                    if (targetId != null) output.add(service.transferOwnership(senderId, targetId).getMessage());
                    break;
                case "rename":
                    output.add(service.rename(senderId, command.joinedArguments(0)).getMessage());
                    break;
                case "tag":
                    output.add(service.changeTag(senderId, command.argument(0)).getMessage());
                    break;
                case "disband": output.add(service.disband(senderId).getMessage()); break;
                case "chat":
                    output.add(service.sendMessage(senderId, senderName, command.joinedArguments(0)).getMessage());
                    break;
                case "info":
                    Clan clan = service.getClanForPlayer(senderId);
                    if (clan == null) output.add("You are not in a clan.");
                    else {
                        output.add(clan.getName() + " [" + clan.getTag() + "] - " + clan.getMembers().size() + "/" + ClanService.MAX_MEMBERS);
                        for (ClanMember member : clan.getMembers().values()) output.add(member.getRole() + " " + member.getPlayerName());
                    }
                    break;
                case "invites":
                    List<ClanInvite> invites = service.getInvitesFor(senderId);
                    if (invites.isEmpty()) output.add("You have no pending clan invites.");
                    else for (ClanInvite invite : invites) {
                        Clan clanForInvite = service.getClan(invite.getClanId());
                        if (clanForInvite != null) output.add(clanForInvite.getName() + " [" + clanForInvite.getTag() + "] - /clan accept " + clanForInvite.getClanId());
                    }
                    break;
                default: output.add("Unknown clan command. Use /clan help.");
            }
        } catch (IllegalArgumentException ex) {
            output.add(ex.getMessage() == null ? "Invalid command." : ex.getMessage());
        } catch (IOException ex) {
            output.add("Clan data could not be saved: " + ex.getMessage());
        }
        return output;
    }

    private UUID resolve(String name, List<String> output) {
        if (name == null || name.trim().isEmpty()) { output.add("A player name is required."); return null; }
        UUID id = players.findPlayerId(name);
        if (id == null) output.add("Player not found: " + name);
        return id;
    }
}
