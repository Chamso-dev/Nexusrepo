# Grocery POS — Wireframes & Screen Previews

Design language: **Liquid Glass** — frosted translucent surfaces over a soft
gradient backdrop, 24dp corner radii, hairline borders (1dp, 12% white),
multi-layer soft shadows, 200–350ms spring-based motion. Dark and light
variants share the same structure; only the gradient backdrop and glass tint
change.

Legend: `[[ ]]` = frosted glass card/surface, `( )` = pill button, `<>` = icon.

---

## 1. Login

```
┌───────────────────────────────────────────┐
│  gradient backdrop (indigo → slate)        │
│                                             │
│        <storefront glyph, glow>            │
│        "Nexus Grocery POS"                 │
│                                             │
│   [[ Email/Username field            ]]    │
│   [[ Password field             <eye> ]]   │
│                                             │
│        ( Sign In )  — glass pill, tinted   │
│                                             │
│        "Forgot password?"                  │
│                                             │
│   ── or ──                                 │
│   [[ Use PIN instead ]]                     │
└───────────────────────────────────────────┘
```
- Field focus = glow ring + elevation increase.
- Errors surface inline below the field, glass card shakes subtly.

## 2. PIN Lock (re-auth / fast switch user)

```
┌───────────────────────────────────────────┐
│         Avatar + "Hi, Sarah"                │
│                                             │
│            ● ● ● ○                          │
│                                             │
│        1   2   3                           │
│        4   5   6        glass numpad keys  │
│        7   8   9                           │
│            0  <back>                       │
│                                             │
│        "Switch user"                        │
└───────────────────────────────────────────┘
```

## 3. Dashboard

```
┌───────────────────────────────────────────┐
│  "Good afternoon, Sarah"      <profile>     │
│  Today · Tue 22 Jun                         │
│                                             │
│  [[ Revenue Today  ]] [[ Profit Today  ]]   │
│  [[ Sales Count    ]] [[ Low Stock: n  ]]   │
│                                             │
│  [[  Sales trend — 7 day sparkline      ]]  │
│                                             │
│  Quick actions                              │
│  ( New Sale ) ( Add Product ) ( Stock In )  │
│                                             │
│  [[ Inventory alerts list ▸ ]]              │
│  [[ Recent transactions ▸   ]]              │
└───────────────────────────────────────────┘
│  ⌂ Dashboard  🛒 POS  📦 Inventory ▾ ⚙      │ ← glass bottom bar
└───────────────────────────────────────────┘
```
All values are zero/empty until real data exists — no placeholder numbers.

## 4. Products

```
┌───────────────────────────────────────────┐
│  Products                      ( + Add )   │
│  [[ Search bar  <barcode-icon> ]]           │
│  ( All ) ( Category chips… ) scrollable     │
│                                             │
│  [[ Product row: thumb, name, SKU,         │
│      stock qty, price            ⋮ ]]      │
│  [[ Product row …                    ]]    │
│  ...                                       │
│  Empty state: "No products yet —            │
│   tap + to add your first product."         │
└───────────────────────────────────────────┘
```
Add/Edit Product (modal sheet, glass):
```
[[ Name  ]] [[ SKU ]] [[ Barcode <scan> ]]
[[ Category ▾ ]] [[ Brand ▾ ]]
[[ Cost price ]] [[ Selling price ]]  → margin chip auto-computed
[[ Stock qty ]] [[ Low-stock threshold ]]
[[ Unit ▾ (pcs/kg/box) ]]
( Save )  ( Cancel )
```

## 5. POS Checkout

```
┌───────────────────────┬─────────────────────┐
│ [[ Search / barcode ]] │  Cart                │
│ Category rail           [[ item, qty -+ ]]   │
│ [[ Product grid tile ]] [[ item, qty -+ ]]   │
│ [[ tile ]] [[ tile ]]   ...                  │
│ [[ tile ]] [[ tile ]]  ───────────────────   │
│                         Subtotal   Discount   │
│                         Tax        Total       │
│                        ( Charge — big glass    │
│                          pill, primary tint )  │
└───────────────────────┴─────────────────────┘
```
Checkout sheet (rises from bottom, glass):
```
Total: $—
Payment method: ( Cash ) ( Card ) ( Mobile ) ( Split )
[[ Amount tendered ]]   Change due: $—
( Confirm & Print Receipt )
```
Receipt preview: glass card styled like thermal receipt, share/print icons.
On phones the grid and cart are tabs; on tablets they sit side-by-side as
shown.

## 6. Inventory

```
┌───────────────────────────────────────────┐
│  Inventory                  ( Adjust Stock )│
│  ( Stock In ) ( Stock Out ) ( History )     │
│                                             │
│  [[ Low stock alert banner, n items ]]      │
│  [[ Product · current qty · threshold ]]    │
│  ...                                       │
└───────────────────────────────────────────┘
```
History tab: chronological glass timeline of stock movements with reason,
quantity delta, actor, timestamp.

## 7. Customers

```
┌───────────────────────────────────────────┐
│  Customers                    ( + Add )    │
│  [[ Search ]]                               │
│  [[ Customer card: name, phone, balance ]]  │
│  ...                                       │
└───────────────────────────────────────────┘
```
Detail screen: profile header (glass), tabs — Overview / Purchase History /
Balance / Notes.

## 8. Suppliers

```
┌───────────────────────────────────────────┐
│  Suppliers                    ( + Add )    │
│  [[ Supplier card: name, contact, owed ]]   │
└───────────────────────────────────────────┘
```
Detail: contact info, linked purchase orders, outstanding balance.

## 9. Purchases

```
┌───────────────────────────────────────────┐
│  Purchase Orders             ( + New PO )   │
│  ( Draft ) ( Ordered ) ( Received )         │
│  [[ PO #, supplier, items, total, status ]] │
└───────────────────────────────────────────┘
```
PO editor: supplier picker, line items (product, qty, unit cost), totals,
"Receive" action that posts an inventory stock-in transaction.

## 10. Reports

```
┌───────────────────────────────────────────┐
│  Reports         ( Day ) ( Week ) ( Month ) │
│  [[ Revenue   ]] [[ Profit  ]] [[ Margin ]]  │
│  [[  Sales-over-time chart            ]]    │
│  [[ Top selling products ▸            ]]    │
│  [[ Inventory valuation ▸             ]]    │
│  ( Export CSV )  ( Export PDF )             │
└───────────────────────────────────────────┘
```

## 11. Settings

```
┌───────────────────────────────────────────┐
│  Settings                                  │
│  [[ Business info  ▸ ]]                     │
│  [[ Tax settings    ▸ ]]                     │
│  [[ Currency        ▸ ]]                     │
│  [[ Receipt customization ▸ ]]               │
│  [[ Users & roles   ▸ ]]                     │
│  [[ Backup & restore ▸ ]]                    │
│  [[ About            ▸ ]]                    │
└───────────────────────────────────────────┘
```

---

### Navigation shell
Bottom navigation bar (phone) / left rail (tablet, ≥840dp width) — glass,
floating with margin, 5 primary destinations (Dashboard, POS, Inventory,
More-menu fanning to Customers/Suppliers/Purchases/Reports/Settings).
Screen transitions use shared-element + fade-through, 280ms, spring damping
0.8.

This spec is implemented 1:1 in `core/designsystem` (tokens/components) and
the `feature/*` packages described in the root `README.md`.
