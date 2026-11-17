package com.alpharip.staffmod.AlphaCrashReporter;
import java.io.IOException;import java.util.List;
public interface CrashReportRepository {void save(CrashReport report)throws IOException;List<CrashReport> loadAll()throws IOException;void delete(String id)throws IOException;}
