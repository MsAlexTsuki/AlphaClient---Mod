-- Keep public name-tag rank priority aligned with the highest assigned community rank.
-- The Founder rank remains an explicit top-priority exception.
create or replace function public.get_public_ranks(uuids uuid[])
returns table(mc_uuid uuid, rank text)
language sql
stable
security definer
set search_path = ''
as $function$
  select distinct on (requested.mc_uuid)
         requested.mc_uuid,
         case
           when coalesce(p.is_founder, false) or p.rank_key = 'founder' then 'founder'
           else coalesce(
             (
               select pr.rank_key
               from public.profile_ranks pr
               join public.community_ranks cr on cr.rank_key = pr.rank_key
               where pr.profile_id = p.id
               order by cr.sort_order desc
               limit 1
             ),
             nullif(p.rank_key, ''),
             'member'
           )
         end as rank
  from unnest(coalesce(uuids[1:100], array[]::uuid[])) as requested(mc_uuid)
  join public.alpha_minecraft_accounts a on a.mc_uuid = requested.mc_uuid
  join public.profiles p on p.id = a.profile_id
  order by requested.mc_uuid, a.linked_at desc, a.profile_id;
$function$;

grant execute on function public.get_public_ranks(uuid[]) to anon, authenticated;
