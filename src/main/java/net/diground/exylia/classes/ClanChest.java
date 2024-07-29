package net.diground.exylia.classes;

public class ClanChest {
    private String clanName;
    private String data;

    public ClanChest(String clanName, String data) {
        this.clanName = clanName;
        this.data = data;
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
