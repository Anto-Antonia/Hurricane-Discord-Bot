package database;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class WarningTracker {
    private static final Map<String, Map<String, WarningInfo>> warnings = new HashMap<>();

    public static class WarningInfo{
        public int count;
        public LocalDate date;

        public WarningInfo(){
            this.count = 0;
            this.date = LocalDate.now();
        }
    }

    public static int addWarning(String guildId, String userId){
        warnings.putIfAbsent(guildId, new HashMap<>());
        Map<String, WarningInfo> guildWarnings = warnings.get(guildId);

        WarningInfo info = guildWarnings.getOrDefault(userId, new WarningInfo());

        // Resetting the count if it's a new day
        if(!info.date.equals(LocalDate.now())){
            info.count = 0;
            info.date = LocalDate.now();
        }

        info.count++;
        guildWarnings.put(userId, info);

        return info.count;
    }

    public static int getWarning(String guildId, String userId){
        Map<String, WarningInfo> guildWarnings = warnings.get(guildId);
        if(guildWarnings == null) return 0;

        WarningInfo info = guildWarnings.get(userId);
        if(info == null || !info.date.equals(LocalDate.now())) return 0;

        return info.count;
    }

    public static void resetWarning(String guildId, String userId){
        Map<String, WarningInfo> guildWarnings = warnings.get(guildId);
        if(guildWarnings == null){
            guildWarnings.remove(userId);
        }
    }
}
