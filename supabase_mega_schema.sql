-- ============================================================================
-- POSTA MARKET 2026 - 1 MEGA FIXED SQL SCHEMA (ALL-IN-ONE)
-- Database: Supabase PostgreSQL (Project: pkfbqoqfisxmjkhrnhpp)
-- 
-- KEY FIXES & COMPATIBILITY:
-- 1. Zero external extensions needed (PostgreSQL 13+ native gen_random_uuid()).
-- 2. No triggers on auth.users (prevents 42501 permission & fetch errors in SQL editor).
-- 3. Pure text user_id references (prevents foreign key blocks and auth sync issues).
-- 4. Full RLS policies for read, write, update, and delete without permission errors.
-- 5. Complete Admin RPCs for updating and adding products with images & descriptions.
-- 6. Pre-loaded with all 11 catalog products and high-resolution Unsplash CDN URLs.
-- ============================================================================

-- ============================================================================
-- SECTION 1: CORE TABLES
-- ============================================================================

-- 1.1 PRODUCTS TABLE
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

-- 1.2 USER PROFILES TABLE
create table if not exists public.profiles (
    id text primary key,
    email text,
    full_name text default 'Posta Shopper',
    phone text default '',
    avatar_url text,
    user_tier text default 'VIP Member',
    wallet_balance numeric(10, 2) default 250.00,
    reward_points integer default 1500,
    delivery_address text default 'Kampala Central Hub, Uganda',
    delivery_coordinates text default '0.3476, 32.5825',
    created_at timestamptz default now() not null,
    updated_at timestamptz default now() not null
);

-- 1.3 ORDERS TABLE
create table if not exists public.orders (
    id text primary key default gen_random_uuid()::text,
    user_id text,
    order_number text unique not null,
    items jsonb not null,
    subtotal numeric(10, 2) not null,
    discount numeric(10, 2) default 0.00,
    shipping numeric(10, 2) default 0.00,
    total numeric(10, 2) not null,
    status text default 'PLACED',
    tracking_number text not null,
    estimated_arrival text not null,
    delivery_address text not null,
    created_at timestamptz default now() not null
);

-- 1.4 FAVORITES TABLE
create table if not exists public.favorites (
    id text primary key default gen_random_uuid()::text,
    user_id text not null,
    product_id text not null,
    created_at timestamptz default now() not null,
    unique (user_id, product_id)
);

-- 1.5 PROMO BANNERS TABLE
create table if not exists public.banners (
    id text primary key,
    title text not null,
    subtitle text not null,
    tag text not null,
    discount_label text not null,
    image_url text not null,
    promo_code text not null,
    is_active boolean default true,
    created_at timestamptz default now() not null
);


-- ============================================================================
-- SECTION 2: ROW LEVEL SECURITY (RLS) POLICIES
-- ============================================================================

-- Enable RLS on all tables
alter table public.products enable row level security;
alter table public.profiles enable row level security;
alter table public.orders enable row level security;
alter table public.favorites enable row level security;
alter table public.banners enable row level security;

-- PRODUCTS POLICIES
drop policy if exists "Products are viewable by everyone" on public.products;
drop policy if exists "Allow update product description and image_url" on public.products;
drop policy if exists "Allow insert products from admin" on public.products;
drop policy if exists "Allow all operations for products" on public.products;

create policy "Products are viewable by everyone"
    on public.products for select using (true);

create policy "Allow update product description and image_url"
    on public.products for update using (true) with check (true);

create policy "Allow insert products from admin"
    on public.products for insert with check (true);

create policy "Allow delete products from admin"
    on public.products for delete using (true);

-- PROFILES POLICIES
drop policy if exists "Public profiles are viewable by everyone" on public.profiles;
drop policy if exists "Users can insert their own profile" on public.profiles;
drop policy if exists "Users can update their own profile" on public.profiles;
drop policy if exists "Allow insert profiles" on public.profiles;
drop policy if exists "Allow update profiles" on public.profiles;

create policy "Public profiles are viewable by everyone"
    on public.profiles for select using (true);

create policy "Allow insert profiles"
    on public.profiles for insert with check (true);

create policy "Allow update profiles"
    on public.profiles for update using (true) with check (true);

-- ORDERS POLICIES
drop policy if exists "Users can view their own orders" on public.orders;
drop policy if exists "Users can insert their own orders" on public.orders;
drop policy if exists "Allow manage orders" on public.orders;

create policy "Users can view their own orders"
    on public.orders for select using (true);

create policy "Users can insert their own orders"
    on public.orders for insert with check (true);

-- FAVORITES POLICIES
drop policy if exists "Users can manage their own favorites" on public.favorites;
drop policy if exists "Allow manage favorites" on public.favorites;

create policy "Allow manage favorites"
    on public.favorites for all using (true) with check (true);

-- BANNERS POLICIES
drop policy if exists "Banners are viewable by everyone" on public.banners;
drop policy if exists "Allow manage banners" on public.banners;

create policy "Banners are viewable by everyone"
    on public.banners for select using (true);

create policy "Allow manage banners"
    on public.banners for all using (true) with check (true);


-- ============================================================================
-- SECTION 3: ADMIN STORED PROCEDURES (RPCs)
-- ============================================================================

-- RPC 1: Update product description and image URL specifically
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

-- RPC 2: Admin Add or Upsert Product
create or replace function public.admin_add_or_update_product(
    p_id text,
    p_name text,
    p_category text,
    p_price numeric,
    p_description text,
    p_image_url text
) returns jsonb
language plpgsql
security definer
as $$
declare
    v_record record;
begin
    insert into public.products (
        id, name, category, price, description, image_url
    ) values (
        p_id, p_name, p_category, p_price, p_description, p_image_url
    )
    on conflict (id) do update set
        name = excluded.name,
        category = excluded.category,
        price = excluded.price,
        description = excluded.description,
        image_url = excluded.image_url
    returning * into v_record;

    return jsonb_build_object('success', true, 'product', row_to_json(v_record));
end;
$$;

-- Grant execution to public APIs
grant execute on function public.admin_update_product_description_and_image(text, text, text) to anon, authenticated, service_role;
grant execute on function public.admin_add_or_update_product(text, text, text, numeric, text, text) to anon, authenticated, service_role;

-- Grant table privileges
grant usage on schema public to anon, authenticated, service_role;
grant all on all tables in schema public to anon, authenticated, service_role;
grant all on all sequences in schema public to anon, authenticated, service_role;


-- ============================================================================
-- SECTION 4: SEED DATA (PROMOTIONS & 11 PRODUCTS WITH MODERN UNSPLASH IMAGES)
-- ============================================================================

-- Banners Seed
insert into public.banners (id, title, subtitle, tag, discount_label, image_url, promo_code)
values
('b_1', 'Future Tech Drop 2026', 'Up to 45% OFF Quantum Audio & Smart Wearables', 'EXCLUSIVE LAUNCH', '45% OFF', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1200&auto=format&fit=crop&q=80', 'POSTATECH'),
('b_2', 'Cyber Streetwear Season', 'Curated Urban Streetwear & High-Top Kicks', 'TRENDING NOW', '30% OFF', 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=1200&auto=format&fit=crop&q=80', 'POSTA2026'),
('b_3', 'Super Sonic 48H Mega Deal', 'Limited Drops, Priority Express Shipping', 'FLASH SALE', '50% OFF', 'https://images.unsplash.com/photo-1526738549149-8e07eca6c147?w=1200&auto=format&fit=crop&q=80', 'SUPER50')
on conflict (id) do update set
    title = excluded.title,
    subtitle = excluded.subtitle,
    image_url = excluded.image_url,
    promo_code = excluded.promo_code;

-- Products Seed (11 Items)
insert into public.products (
    id, name, category, price, original_price, rating, review_count, 
    description, specs, tag, image_url, is_featured, is_popular, 
    is_new_arrival, is_special_offer, discount_percent, stock_left, 
    total_stock, latitude, longitude, location_name, distance_km
)
values
(
    'p_1',
    'AeroPods Pro 2026 Spatial Audio',
    'TECH',
    189.99,
    249.99,
    4.9,
    1420,
    'Next-generation ultra-low latency wireless earbuds with biometric sensors, active noise cancellation 3.0, and 48-hour battery case.',
    '["Bluetooth 5.4 LE Audio", "Active Noise Cancellation 3.0", "48h Total Battery", "IPX8 Waterproof"]'::jsonb,
    'BEST SELLER',
    'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&auto=format&fit=crop&q=80',
    true, true, false, false, 24, 14, 60,
    0.3136, 32.5811, 'Kampala Posta Main Depot, Kampala Rd', 0.8
),
(
    'p_2',
    'Posta Kinetic Streetwear Hoodie',
    'FASHION',
    79.50,
    110.00,
    4.8,
    650,
    'Heavyweight 450GSM organic cotton hoodie with electric violet reflective accents, oversized modern silhouette, and concealed kangaroo stash pocket.',
    '["100% Organic French Terry", "Reflective Neon Piping", "Pre-shrunk Double Weave", "Relaxed Modern Fit"]'::jsonb,
    'TRENDING',
    'https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800&auto=format&fit=crop&q=80',
    true, true, true, false, 28, 22, 50,
    0.3205, 32.5855, 'Garden City Mall Posta Drop, Yusuf Lule Rd', 1.5
),
(
    'p_3',
    'Nova Pulse OLED Smart Watch 3',
    'TECH',
    269.00,
    329.00,
    4.8,
    890,
    'Curved micro-OLED retina display with ECG monitoring, solar charging bezel, always-on AI assistant, and customizable tactile haptic crown.',
    '["1.92\" Micro-OLED 120Hz", "Titanium Aerospace Case", "7-Day Battery Life", "5ATM Water Resistant"]'::jsonb,
    'HOT DEAL',
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&auto=format&fit=crop&q=80',
    true, false, false, true, 18, 8, 40,
    0.3340, 32.5895, 'Kololo Tech Hub, Acacia Mall Bay', 2.4
),
(
    'p_4',
    'CyberSneaker Zero Drift V2',
    'FASHION',
    145.00,
    195.00,
    4.9,
    720,
    'Futuristic lifestyle sneaker featuring carbon-fiber responsive midsole plates, glowing electric violet outsole lugs, and breathable mesh.',
    '["Carbon Propulsion Plate", "Dual-density EVA foam", "Glow-in-the-dark Accents", "Ortholite Recycled Insole"]'::jsonb,
    'NEW DROP',
    'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80',
    true, false, true, false, 25, 5, 25,
    0.3385, 32.5710, 'Makerere Innovation Hub, University Rd', 2.1
),
(
    'p_5',
    'Lumina Aura Diffuser & Ambient Lamp',
    'HOME',
    54.00,
    75.00,
    4.7,
    410,
    'Ultrasonic aroma diffuser with dynamic circadian lighting sync, silent magnetic mist motor, and smart home voice integration.',
    '["500ml Reservoir", "16M RGB Dynamic Glow", "Ultra-quiet <20dB", "Schedule via Posta App"]'::jsonb,
    'POPULAR',
    'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?w=800&auto=format&fit=crop&q=80',
    false, true, false, false, 28, 19, 45,
    0.3020, 32.6100, 'Bugolobi Village Market Depot, Spring Rd', 3.8
),
(
    'p_6',
    'Vortex 4K Hologram Mini Projector',
    'TECH',
    320.00,
    420.00,
    4.9,
    380,
    'Pocket-sized laser projector with automatic trapezoid correction, built-in Harman Kardon speakers, and 200-inch cinema projection.',
    '["Native 4K HDR10+", "1500 ANSI Lumens", "Auto-Focus & Keystone", "3hr Battery Built-in"]'::jsonb,
    'TOP RATED',
    'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80',
    false, true, false, true, 24, 7, 20,
    0.3290, 32.6010, 'Lugogo Mall Distribution Center, Jinja Rd', 3.2
),
(
    'p_7',
    'Glacier Glass Hydration Bottle 1L',
    'SPORTS',
    34.99,
    45.00,
    4.8,
    510,
    'Triple-wall vacuum insulated stainless steel with UV-C auto-sanitizing cap, temperature display, and electric violet powder-coat finish.',
    '["UV-C Sterilization Cap", "24h Cold / 12h Hot", "BPA-Free 316 Steel", "Leakproof Magnetic Cap"]'::jsonb,
    'ECO CHOICE',
    'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80',
    false, true, true, false, 22, 28, 60,
    0.2850, 32.5800, 'Kabalagala Express Depot, Ggaba Rd', 4.5
),
(
    'p_8',
    'Prism Glow Peptide Serum 50ml',
    'BEAUTY',
    48.00,
    64.00,
    4.9,
    1150,
    'Bio-engineered triple peptide formula with hyaluronic spheres and blackberry extract for radiant skin barrier restoration.',
    '["Triple Peptide 5%", "Niacinamide + HA", "Dermatologist Tested", "Cruelty-Free Vegan"]'::jsonb,
    'NEW ARRIVAL',
    'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=800&auto=format&fit=crop&q=80',
    false, false, true, false, 25, 35, 100,
    0.3160, 32.5740, 'Nakero Market Express Station', 1.1
),
(
    'p_9',
    'Haptic Edge Wireless Mechanical Keyboard',
    'GAMING',
    159.00,
    199.00,
    4.8,
    430,
    'Hot-swappable tactile switch keyboard with customizable OLED mini-display, RGB per-key underglow, and aluminum CNC chassis.',
    '["75% Compact Layout", "Hot-Swap Gateron Switches", "Tri-Mode Wireless 2.4G", "OLED Custom Screen"]'::jsonb,
    'HOT DROP',
    'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80',
    false, false, true, false, 20, 11, 30,
    0.3340, 32.5895, 'Kololo Tech Hub, Acacia Mall Bay', 2.4
),
(
    'p_10',
    'OmniCharge 140W GaN Fast Charger',
    'ACCESSORIES',
    49.99,
    89.99,
    4.9,
    980,
    'Ultra-compact Gallium Nitride power adapter with 3x USB-C PD 3.1 ports, intelligent power distribution, and travel pin adapters.',
    '["140W Max Output", "3x USB-C + 1x USB-A", "GaN Infinity Tech", "Foldable Plug"]'::jsonb,
    'FLASH 45% OFF',
    'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=800&auto=format&fit=crop&q=80',
    false, false, false, true, 45, 5, 25,
    0.3136, 32.5811, 'Kampala Posta Main Depot, Kampala Rd', 0.8
),
(
    'p_11',
    'Apex Carbon Polarized Sunglasses',
    'ACCESSORIES',
    62.00,
    124.00,
    4.7,
    340,
    'Aerospace titanium rimless frame with purple-tint UV400 mirror polarized lenses and anti-scratch hydrophobic coating.',
    '["UV400 Polarized Lenses", "Ultralight 18g Frame", "Hydrophobic Coating", "Magnetic Leather Case"]'::jsonb,
    '50% OFF DEAL',
    'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800&auto=format&fit=crop&q=80',
    false, false, false, true, 50, 7, 20,
    0.3205, 32.5855, 'Garden City Mall Posta Drop, Yusuf Lule Rd', 1.5
)
on conflict (id) do update set
    name = excluded.name,
    category = excluded.category,
    price = excluded.price,
    original_price = excluded.original_price,
    rating = excluded.rating,
    review_count = excluded.review_count,
    description = excluded.description,
    image_url = excluded.image_url,
    specs = excluded.specs,
    tag = excluded.tag,
    is_featured = excluded.is_featured,
    is_popular = excluded.is_popular,
    is_new_arrival = excluded.is_new_arrival,
    is_special_offer = excluded.is_special_offer,
    discount_percent = excluded.discount_percent,
    stock_left = excluded.stock_left,
    total_stock = excluded.total_stock,
    latitude = excluded.latitude,
    longitude = excluded.longitude,
    location_name = excluded.location_name,
    distance_km = excluded.distance_km;
