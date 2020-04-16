package BattleEye.Command;

public enum BattlEyeCommandType {
    Restart("#restart"),
    Reassign("#reassign"),
    Shutdown("#shutdown"),
    Lock("#lock"),
    Unlock("#unlock"),
    Mission("#mission"),
    Missions("missions"),
    Players("players"),
    Say("say"),
    Kick("kick"),
    RConPassword("RConPassword"),
    MaxPing("maxPing"),
    LoadScripts("loadScripts"),
    LoadEvents("loadEvents"),
    LoadBans("loadBans"),
    Bans("bans"),
    Ban("ban"),
    AddBan("addBan"),
    RemoveBan("removeBan"),
    WriteBans("writeBans");

    private String commandString;

    BattlEyeCommandType(String command)
    {
        commandString = command;
    }

    public String getCommandString()
    {
        return commandString;
    }
}
