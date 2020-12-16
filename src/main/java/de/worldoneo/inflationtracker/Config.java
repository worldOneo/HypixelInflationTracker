package de.worldoneo.inflationtracker;

public class Config {
    public String apiKey = "00000000-0000-0000-0000-000000000000";
    public long hypixelRequestPeriod = 3600000L;
    public String jdbcurl = "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC";
    public String sqlUsername = "sqluser";
    public String sqlPassword = "password";
    public String sqlTableName = "hypixeldata";
    public String[] basket = new String[]{"ENCHANTED_DIAMOND", "ENCHANTED_IRON", "ENCHANTED_GOLD", "ENCHANTED_LAPIS_LAZULI",
            "DIAMOND", "GOLD_INGOT", "IRON_INGOT", "ENCHANTED_COBBLESTONE", "SAND", "QUARTZ", "CACTUS", "ENCHANTED_CARROT",
            "POTATO_ITEM", "NETHER_STALK"};
    public boolean enableHTTPAPI = true;
    public String httpBindAddress = "localhost";
    public int httpBindPort = 8080;
    public String httpPath = "/data/";
    public long httpAPICrunchPeriod = 60000;
    public boolean enableGUI = true;
}
