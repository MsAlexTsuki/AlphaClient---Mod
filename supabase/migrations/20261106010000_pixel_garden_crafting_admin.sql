create or replace function public.founder_pixel_garden_craft_manage(
    p_action text,
    p_target_alpha_name text,
    p_product text default null,
    p_amount integer default 0
)
returns jsonb
language plpgsql
security definer
set search_path = public, auth
as $function$
declare
    actor uuid;
    target uuid;
    save_data jsonb;
    products jsonb;
    active_orders jsonb;
    ready_orders jsonb;
    order_item jsonb;
    current_amount integer;
    i integer;
    target_name text;
begin
    actor := public.pixel_garden_require_founder();
    target := public.pixel_garden_find_target(p_target_alpha_name);

    if p_action not in ('give', 'finish', 'clear', 'setcapacity', 'setqueue', 'reset') then
        raise exception 'Unsupported crafting administration action.' using errcode = '22023';
    end if;

    if p_action = 'give' then
        if p_product is null or p_product not in (
            'bread', 'chips', 'potato_mush', 'carrot_juice', 'pumpkin_head',
            'melon_slices', 'cactus_juice', 'sugar', 'basket_of_berries',
            'camberry_bread', 'sugar_bread'
        ) then
            raise exception 'Choose a valid crafted product ID.' using errcode = '22023';
        end if;
        if p_amount < 1 or p_amount > 1000000 then
            raise exception 'Product amount must be between 1 and 1,000,000 batches.' using errcode = '22023';
        end if;
    elsif p_action = 'finish' then
        if p_amount < 0 then
            raise exception 'Order number cannot be negative.' using errcode = '22023';
        end if;
    elsif p_action in ('setcapacity', 'setqueue') then
        if p_amount < 0 or p_amount > 4 then
            raise exception 'Crafting upgrade level must be between 0 and 4.' using errcode = '22023';
        end if;
    end if;

    select s.save_data
      into save_data
      from public.pixel_garden_saves s
     where s.user_id = target
     for update;

    if save_data is null then
        save_data := public.pixel_garden_default_save();
    end if;

    if p_action = 'give' then
        products := coalesce(save_data->'crafted_product_batches', '{}'::jsonb);
        if jsonb_typeof(products) is distinct from 'object' then
            products := '{}'::jsonb;
        end if;
        current_amount := greatest(0, coalesce((products->>p_product)::integer, 0));
        if current_amount > 2147483647 - p_amount then
            raise exception 'Crafted product inventory would exceed the supported limit.' using errcode = '22003';
        end if;
        products := jsonb_set(products, array[p_product], to_jsonb(current_amount + p_amount), true);
        save_data := jsonb_set(save_data, '{crafted_product_batches}', products, true);

    elsif p_action = 'finish' then
        active_orders := coalesce(save_data->'crafting_active_orders', '[]'::jsonb);
        ready_orders := coalesce(save_data->'crafting_ready_orders', '[]'::jsonb);
        if jsonb_typeof(active_orders) is distinct from 'array' then active_orders := '[]'::jsonb; end if;
        if jsonb_typeof(ready_orders) is distinct from 'array' then ready_orders := '[]'::jsonb; end if;

        if p_amount = 0 then
            if jsonb_array_length(active_orders) > 0 then
                for i in 0..jsonb_array_length(active_orders) - 1 loop
                    order_item := active_orders->i;
                    if jsonb_typeof(order_item) = 'object' and order_item ? 'recipe_id' then
                        ready_orders := ready_orders || jsonb_build_array(order_item->>'recipe_id');
                    end if;
                end loop;
            end if;
            active_orders := '[]'::jsonb;
        else
            if p_amount > jsonb_array_length(active_orders) then
                raise exception 'That active crafting order does not exist.' using errcode = 'P0002';
            end if;
            order_item := active_orders->(p_amount - 1);
            if jsonb_typeof(order_item) = 'object' and order_item ? 'recipe_id' then
                ready_orders := ready_orders || jsonb_build_array(order_item->>'recipe_id');
            end if;
            active_orders := active_orders - (p_amount - 1);
        end if;

        save_data := jsonb_set(save_data, '{crafting_active_orders}', active_orders, true);
        save_data := jsonb_set(save_data, '{crafting_ready_orders}', ready_orders, true);

    elsif p_action = 'clear' then
        save_data := jsonb_set(save_data, '{crafting_active_orders}', '[]'::jsonb, true);
        save_data := jsonb_set(save_data, '{crafting_queued_orders}', '[]'::jsonb, true);
        save_data := jsonb_set(save_data, '{crafting_ready_orders}', '[]'::jsonb, true);

    elsif p_action = 'setcapacity' then
        save_data := jsonb_set(save_data, '{crafting_capacity_level}', to_jsonb(p_amount), true);

    elsif p_action = 'setqueue' then
        save_data := jsonb_set(save_data, '{crafting_queue_level}', to_jsonb(p_amount), true);

    elsif p_action = 'reset' then
        save_data := jsonb_set(save_data, '{crafting_capacity_level}', '0'::jsonb, true);
        save_data := jsonb_set(save_data, '{crafting_queue_level}', '0'::jsonb, true);
        save_data := jsonb_set(save_data, '{crafting_active_orders}', '[]'::jsonb, true);
        save_data := jsonb_set(save_data, '{crafting_queued_orders}', '[]'::jsonb, true);
        save_data := jsonb_set(save_data, '{crafting_ready_orders}', '[]'::jsonb, true);
        save_data := jsonb_set(save_data, '{crafted_product_batches}', '{}'::jsonb, true);
    end if;

    insert into public.pixel_garden_saves(user_id, save_data, saved_at)
    values (target, save_data, now())
    on conflict (user_id) do update
       set save_data = excluded.save_data,
           saved_at = now();

    select p.username into target_name
      from public.profiles p
     where p.id = target;

    insert into public.audit_logs(
        actor_id, actor_label, product, category, action,
        target_user_id, target_label, outcome, details
    )
    values (
        actor,
        (select p.username from public.profiles p where p.id = actor),
        'client',
        'pixel_garden_crafting',
        p_action,
        target,
        target_name,
        'success',
        jsonb_build_object('product', p_product, 'amount', p_amount)
    );

    return jsonb_build_object(
        'success', true,
        'action', p_action,
        'target_alpha_name', target_name,
        'save_data', save_data
    );
end;
$function$;

revoke all on function public.founder_pixel_garden_craft_manage(text, text, text, integer) from public, anon;
grant execute on function public.founder_pixel_garden_craft_manage(text, text, text, integer) to authenticated;
