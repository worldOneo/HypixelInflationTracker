package de.worldoneo.inflationtracker.sql;

import net.hypixel.api.reply.skyblock.BazaarReply;

import java.sql.Timestamp;

public class SQLEntry {
    public final long date;
    public final BazaarReply.Product product;

    @Override
    public String toString() {
        return "SQLEntry{" +
                "date=" + date +
                ", products=" + product +
                '}';
    }

    public SQLEntry(Timestamp date, BazaarReply.Product product) {
        this.date = date.getTime();
        this.product = product;
    }
}
