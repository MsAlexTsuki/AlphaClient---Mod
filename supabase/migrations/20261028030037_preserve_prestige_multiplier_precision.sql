-- Preserve up to three decimal places for personal Prestige multiplier overrides.
alter table public.pixel_garden_prestige alter column multiplier_override type numeric(5,3) using multiplier_override::numeric(5,3);
