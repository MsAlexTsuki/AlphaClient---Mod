# Base Storage Ledger

Standalone AlphaClient 1.8.9 prototype in the `mod-ideas` branch.

## Purpose

Aggregate the latest remembered contents of known chests into one searchable stock overview. Users can set local minimum-stock thresholds and see which remembered totals are below those thresholds.

## Reuses existing functionality

- Reads snapshots from `com.alpharip.staffmod.rememberchestinventory.RememberChestInventory`.
- Uses the existing chest snapshot model and manager.
- Does not add another chest scanner or change the existing Remember Chest Inventory implementation.

## Included prototype files

- `BaseStorageLedger.java` — module entry point and local settings lifecycle.
- `StorageLedgerIndex.java` — aggregates item totals across remembered chests.
- `StorageLedgerItem.java` — immutable aggregated item row.
- `StorageLowAlert.java` — minimum-stock thresholds with local Properties persistence.
- `BaseStorageLedgerGui.java` — searchable stock overview and threshold controls.

## Data and limitations

- Local-only settings at `config/alphaclient/base-storage-ledger.properties`.
- No Supabase, remote service, or server support.
- Stock values are only as current as the last chest snapshots.
- A chest changing after its last snapshot is not detected by this module.
- A future integration point must call `BaseStorageLedger.getInstance().openGui(parent)`.
- This prototype is not registered in AlphaClient Core and is not claimed to be build- or runtime-tested.

## Suggested next integration step

Connect the existing chest snapshot event flow to the module refresh lifecycle, then register the GUI entry point through an explicitly approved AlphaClient integration change.
