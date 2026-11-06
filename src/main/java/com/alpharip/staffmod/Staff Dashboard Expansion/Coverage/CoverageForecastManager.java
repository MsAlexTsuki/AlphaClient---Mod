package com.alpharip.staffdashboardexpansion.coverage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class CoverageForecastManager {
 private final StaffCoverageForecast forecast;
 public CoverageForecastManager(StaffCoverageForecast forecast) { if (forecast == null) throw new IllegalArgumentException("forecast cannot be null"); this.forecast = forecast; }
 public CoverageRequirement addRequirement(String name, LocalDateTime start, LocalDateTime end, int minimumStaff) { return forecast.addRequirement(name, start, end, minimumStaff); }
 public boolean removeRequirement(String id) { return forecast.removeRequirement(id); }
 public List<CoverageRequirement> getRequirements() { return forecast.getRequirements(); }
 public List<CoverageResult> getResults(LocalDate date) { return forecast.forecast(date); }
 public int countUnderstaffed(LocalDate date) { int n=0; for (CoverageResult r:getResults(date)) if(r.isUnderstaffed()) n++; return n; }
 public int countCovered(LocalDate date) { int n=0; for (CoverageResult r:getResults(date)) if(r.isCovered()) n++; return n; }
}