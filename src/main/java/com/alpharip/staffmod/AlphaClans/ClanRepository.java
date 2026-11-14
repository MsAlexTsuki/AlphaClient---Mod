package com.alpharip.staffmod.AlphaClans;

import java.io.IOException;
import java.util.List;

public interface ClanRepository {
    List<Clan> loadAll() throws IOException;
    void saveAll(List<Clan> clans) throws IOException;
}
