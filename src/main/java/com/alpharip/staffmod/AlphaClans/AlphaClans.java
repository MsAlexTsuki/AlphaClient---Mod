package com.alpharip.staffmod.AlphaClans;

import java.io.IOException;
import java.nio.file.Path;

/** Entry point for the standalone Alpha Clans prototype. */
public final class AlphaClans {
    public static final String FEATURE_NAME = "Alpha Clans";
    private final ClanService clanService;
    private final ClanScreenModel screenModel;

    private AlphaClans(Path storageFile) throws IOException {
        this.clanService = new ClanService(new FileClanRepository(storageFile));
        this.screenModel = new ClanScreenModel(clanService);
    }

    public static AlphaClans open(Path storageFile) throws IOException {
        return new AlphaClans(storageFile);
    }

    public ClanService getClanService() { return clanService; }
    public ClanScreenModel getScreenModel() { return screenModel; }
}
