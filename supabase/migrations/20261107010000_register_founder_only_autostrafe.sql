-- Register AutoStrafe as a Founder-only client module and enforce Founder-only
-- access for every module in the Founder category, including against Owner,
-- per-user overrides, and category grants.

INSERT INTO public.client_mods (mod_id, display_name, category_key, description, enabled)
VALUES (
    'auto-strafe',
    'AutoStrafe',
    'founder',
    'Automatically adjusts movement to orbit the nearest visible player while movement input is active.',
    TRUE
)
ON CONFLICT (mod_id) DO UPDATE
SET display_name = EXCLUDED.display_name,
    category_key = EXCLUDED.category_key,
    description = EXCLUDED.description,
    enabled = TRUE,
    updated_at = now();

INSERT INTO public.client_mod_rank_permissions (rank_key, mod_id, enabled)
SELECT rank_key, 'auto-strafe', (rank_key = 'founder')
FROM public.community_ranks
ON CONFLICT (rank_key, mod_id) DO UPDATE
SET enabled = EXCLUDED.enabled,
    updated_at = now();

CREATE OR REPLACE FUNCTION public.get_my_client_mod_permissions()
RETURNS TABLE(mod_id text, display_name text, category_key text, allowed boolean, access_source text)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path TO ''
AS $function$
  SELECT m.mod_id, m.display_name, m.category_key,
    CASE
      WHEN NOT m.enabled THEN false
      WHEN m.category_key = 'founder' THEN EXISTS (
        SELECT 1 FROM public.profiles p
        WHERE p.id = auth.uid()
          AND (
            p.rank_key = 'founder'
            OR p.is_founder IS TRUE
            OR EXISTS (
              SELECT 1 FROM public.profile_ranks pr
              WHERE pr.profile_id = p.id AND pr.rank_key = 'founder'
            )
          )
      )
      WHEN EXISTS (
        SELECT 1 FROM public.profiles p
        WHERE p.id = auth.uid()
          AND (
            p.rank_key IN ('founder', 'owner')
            OR p.is_founder IS TRUE
            OR EXISTS (
              SELECT 1 FROM public.profile_ranks pr
              WHERE pr.profile_id = p.id AND pr.rank_key IN ('founder', 'owner')
            )
          )
      ) THEN true
      WHEN EXISTS (
        SELECT 1 FROM public.client_mod_user_overrides u
        WHERE u.user_id = auth.uid() AND u.mod_id = m.mod_id AND u.effect = 'deny'
          AND u.revoked_at IS NULL AND (u.expires_at IS NULL OR u.expires_at > now())
      ) THEN false
      WHEN EXISTS (
        SELECT 1 FROM public.client_mod_user_overrides u
        WHERE u.user_id = auth.uid() AND u.mod_id = m.mod_id AND u.effect = 'allow'
          AND u.revoked_at IS NULL AND (u.expires_at IS NULL OR u.expires_at > now())
      ) THEN true
      WHEN EXISTS (
        SELECT 1 FROM public.client_mod_category_grants g
        WHERE g.user_id = auth.uid() AND g.category_key = m.category_key
          AND g.revoked_at IS NULL AND (g.expires_at IS NULL OR g.expires_at > now())
      ) THEN true
      WHEN EXISTS (
        SELECT 1 FROM public.profiles p
        JOIN public.client_mod_rank_permissions rp
          ON (rp.rank_key = p.rank_key OR rp.rank_key IN (
            SELECT pr.rank_key FROM public.profile_ranks pr WHERE pr.profile_id = p.id
          ))
        WHERE p.id = auth.uid() AND rp.mod_id = m.mod_id AND rp.enabled IS TRUE
      ) THEN true
      ELSE false
    END AS allowed,
    CASE
      WHEN NOT m.enabled THEN 'disabled'
      WHEN m.category_key = 'founder' THEN
        CASE WHEN EXISTS (
          SELECT 1 FROM public.profiles p
          WHERE p.id = auth.uid()
            AND (
              p.rank_key = 'founder'
              OR p.is_founder IS TRUE
              OR EXISTS (
                SELECT 1 FROM public.profile_ranks pr
                WHERE pr.profile_id = p.id AND pr.rank_key = 'founder'
              )
            )
        ) THEN 'rank' ELSE 'rank' END
      WHEN EXISTS (
        SELECT 1 FROM public.profiles p
        WHERE p.id = auth.uid()
          AND (
            p.rank_key IN ('founder', 'owner')
            OR p.is_founder IS TRUE
            OR EXISTS (
              SELECT 1 FROM public.profile_ranks pr
              WHERE pr.profile_id = p.id AND pr.rank_key IN ('founder', 'owner')
            )
          )
      ) THEN 'rank'
      WHEN EXISTS (
        SELECT 1 FROM public.client_mod_user_overrides u
        WHERE u.user_id = auth.uid() AND u.mod_id = m.mod_id AND u.effect = 'deny'
          AND u.revoked_at IS NULL AND (u.expires_at IS NULL OR u.expires_at > now())
      ) THEN 'individual_deny'
      WHEN EXISTS (
        SELECT 1 FROM public.client_mod_user_overrides u
        WHERE u.user_id = auth.uid() AND u.mod_id = m.mod_id AND u.effect = 'allow'
          AND u.revoked_at IS NULL AND (u.expires_at IS NULL OR u.expires_at > now())
      ) THEN 'individual_grant'
      WHEN EXISTS (
        SELECT 1 FROM public.client_mod_category_grants g
        WHERE g.user_id = auth.uid() AND g.category_key = m.category_key
          AND g.revoked_at IS NULL AND (g.expires_at IS NULL OR g.expires_at > now())
      ) THEN 'category_grant'
      WHEN EXISTS (
        SELECT 1 FROM public.profiles p
        JOIN public.client_mod_rank_permissions rp
          ON (rp.rank_key = p.rank_key OR rp.rank_key IN (
            SELECT pr.rank_key FROM public.profile_ranks pr WHERE pr.profile_id = p.id
          ))
        WHERE p.id = auth.uid() AND rp.mod_id = m.mod_id AND rp.enabled IS TRUE
      ) THEN 'rank'
      ELSE 'rank'
    END AS access_source
  FROM public.client_mods m
  WHERE auth.uid() IS NOT NULL
  ORDER BY m.category_key, m.display_name;
$function$;
