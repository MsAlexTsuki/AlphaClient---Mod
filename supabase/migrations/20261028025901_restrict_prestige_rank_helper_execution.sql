-- Keep the internal rank multiplier helper inaccessible through the client REST API.
revoke all on function public.pixel_garden_prestige_rank_multiplier(uuid) from public,anon,authenticated;
