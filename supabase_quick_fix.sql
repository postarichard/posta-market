-- ============================================================================
-- Posta Market 2026 - Minimal Quick Fix for Products & Admin Image/Description
-- Run this in Supabase SQL Editor (pkfbqoqfisxmjkhrnhpp)
-- ============================================================================

-- 1. Ensure products table exists with image_url and description
create table if not exists public.products (
    id text primary key,
    name text not null,
    category text not null,
    price numeric(10, 2) not null check (price >= 0),
    original_price numeric(10, 2),
    rating numeric(3, 2) default 4.8,
    review_count integer default 100,
    description text not null,
    specs jsonb default '[]'::jsonb,
    tag text,
    image_url text,
    is_featured boolean default false,
    is_popular boolean default false,
    is_new_arrival boolean default false,
    is_special_offer boolean default false,
    discount_percent integer default 0,
    stock_left integer default 20,
    total_stock integer default 50,
    latitude double precision default 0.3476,
    longitude double precision default 32.5825,
    location_name text default 'Kampala Central Hub',
    distance_km double precision default 1.2,
    created_at timestamptz default now() not null
);

-- 2. Enable Row Level Security & Allow Access
alter table public.products enable row level security;

drop policy if exists "Products are viewable by everyone" on public.products;
drop policy if exists "Allow update product description and image_url" on public.products;
drop policy if exists "Allow insert products from admin" on public.products;

create policy "Products are viewable by everyone" on public.products for select using (true);
create policy "Allow update product description and image_url" on public.products for update using (true) with check (true);
create policy "Allow insert products from admin" on public.products for insert with check (true);

-- 3. Dedicated RPC function to update product description and image_url
create or replace function public.admin_update_product_description_and_image(
    p_product_id text,
    p_description text,
    p_image_url text
) returns jsonb
language plpgsql
security definer
as $$
declare
    v_updated record;
begin
    update public.products
    set description = p_description,
        image_url = p_image_url
    where id = p_product_id
    returning * into v_updated;

    if not found then
        return jsonb_build_object('success', false, 'message', 'Product not found');
    end if;

    return jsonb_build_object('success', true, 'product', row_to_json(v_updated));
end;
$$;

grant execute on function public.admin_update_product_description_and_image(text, text, text) to anon, authenticated, service_role;
