import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = "store_assets/screenshots"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Standard Play Store / App Store Screenshot Dimensions
W, H = 1080, 2400

# Color palette from Lumina app
BG_CANVAS = (248, 249, 250)
INK_COLOR = (24, 28, 26)
MUTED_COLOR = (120, 126, 122)
BORDER_COLOR = (226, 230, 228)
CARD_BG = (255, 255, 255)
GREEN_ACCENT = (16, 185, 129)
RED_ACCENT = (239, 68, 68)
CHIP_BG = (238, 241, 239)

def get_font(size, bold=False):
    font_path = "/System/Library/Fonts/Supplemental/Arial Bold.ttf" if bold else "/System/Library/Fonts/Supplemental/Arial.ttf"
    try:
        return ImageFont.truetype(font_path, size)
    except Exception:
        return ImageFont.load_default()

font_headline = get_font(72, bold=True)
font_subtitle = get_font(36, bold=False)
font_title_md = get_font(44, bold=True)
font_body = get_font(32, bold=False)
font_badge = get_font(26, bold=True)
font_icon_text = get_font(28, bold=True)

def generate_procedural_photo(w, h, seed_type):
    img = Image.new("RGB", (w, h))
    draw = ImageDraw.Draw(img)
    if seed_type == "mountain":
        # Sky gradient
        for y in range(h):
            r = int(120 + 80 * (y / h))
            g = int(180 + 50 * (y / h))
            b = int(240 - 20 * (y / h))
            draw.line([(0, y), (w, y)], fill=(r, g, b))
        # Mountains
        draw.polygon([(0, h), (w*0.35, h*0.45), (w*0.7, h), (w, h*0.55), (w, h), (0, h)], fill=(70, 90, 85))
        draw.polygon([(w*0.2, h), (w*0.55, h*0.35), (w*0.9, h)], fill=(95, 120, 110))
        # Sun
        draw.ellipse([w*0.75, h*0.15, w*0.92, h*0.15 + (w*0.17)], fill=(255, 220, 120))
    elif seed_type == "sunset":
        for y in range(h):
            r = int(255 - 60 * (y / h))
            g = int(110 + 60 * (y / h))
            b = int(60 + 120 * (y / h))
            draw.line([(0, y), (w, y)], fill=(r, g, b))
        draw.ellipse([w*0.35, h*0.3, w*0.65, h*0.3 + (w*0.3)], fill=(255, 240, 180))
        draw.rectangle([0, int(h*0.7), w, h], fill=(30, 40, 60))
    elif seed_type == "forest":
        for y in range(h):
            r = int(40 + 40 * (y / h))
            g = int(110 + 60 * (y / h))
            b = int(70 + 40 * (y / h))
            draw.line([(0, y), (w, y)], fill=(r, g, b))
        for x in range(0, w, 40):
            draw.line([(x, h), (x + 20, int(h*0.3))], fill=(25, 60, 40), width=18)
    elif seed_type == "ocean":
        for y in range(h):
            r = int(20 + 30 * (y / h))
            g = int(140 + 60 * (y / h))
            b = int(200 + 40 * (y / h))
            draw.line([(0, y), (w, y)], fill=(r, g, b))
        draw.polygon([(0, h), (w*0.5, h*0.8), (w, h)], fill=(240, 220, 180))
    elif seed_type == "architecture":
        for y in range(h):
            draw.line([(0, y), (w, y)], fill=(210 - int(30*(y/h)), 215 - int(25*(y/h)), 225 - int(20*(y/h))))
        draw.rectangle([int(w*0.15), int(h*0.25), int(w*0.85), h], fill=(245, 245, 248), outline=(180, 185, 195), width=4)
        for row in range(4):
            for col in range(3):
                bx = int(w*0.25 + col * w*0.2)
                by = int(h*0.35 + row * h*0.13)
                draw.rectangle([bx, by, bx + int(w*0.12), by + int(h*0.09)], fill=(120, 160, 190))
    else: # coffee / abstract
        for y in range(h):
            draw.line([(0, y), (w, y)], fill=(235 - int(20*(y/h)), 220 - int(30*(y/h)), 205 - int(30*(y/h))))
        draw.ellipse([int(w*0.2), int(h*0.2), int(w*0.8), int(h*0.2 + w*0.6)], fill=(255, 255, 255))
        draw.ellipse([int(w*0.28), int(h*0.2 + w*0.08), int(w*0.72), int(h*0.2 + w*0.52)], fill=(120, 75, 45))
    return img

def draw_header_banner(draw, headline, sub):
    # Top promotion text
    draw.text((80, 120), headline, fill=INK_COLOR, font=font_headline)
    draw.text((80, 210), sub, fill=MUTED_COLOR, font=font_subtitle)

def draw_phone_frame(draw, x, y, w, h):
    # Outer device shape
    draw.rounded_rectangle([x, y, x + w, y + h], radius=56, fill=BG_CANVAS, outline=(215, 220, 218), width=8)
    # Status bar
    time_font = get_font(28, bold=True)
    draw.text((x + 50, y + 28), "9:41", fill=INK_COLOR, font=time_font)
    # Battery / wifi indicator dots
    draw.ellipse([x + w - 120, y + 36, x + w - 100, y + 56], fill=INK_COLOR)
    draw.rounded_rectangle([x + w - 85, y + 34, x + w - 45, y + 58], radius=6, outline=INK_COLOR, width=3)
    draw.rectangle([x + w - 80, y + 39, x + w - 55, y + 53], fill=INK_COLOR)

def draw_bottom_bar(draw, px, py, pw, active_tab="photos"):
    # Floating pill bottom bar
    bar_w = int(pw * 0.84)
    bar_h = 100
    bar_x = px + (pw - bar_w) // 2
    bar_y = py - 130
    draw.rounded_rectangle([bar_x, bar_y, bar_x + bar_w, bar_y + bar_h], radius=50, fill=(255, 255, 255), outline=(220, 225, 222), width=3)
    
    tabs = [("Photos", "P"), ("Clean Up", "C"), ("Albums", "A"), ("Search", "S"), ("More", "M")]
    step = bar_w // len(tabs)
    for i, (name, icon) in enumerate(tabs):
        cx = bar_x + i * step + step // 2
        cy = bar_y + bar_h // 2
        is_active = (active_tab.lower() in name.lower())
        color = GREEN_ACCENT if is_active else MUTED_COLOR
        draw.text((cx - 12, cy - 20), icon, fill=color, font=font_icon_text)

# ==========================================
# SCREENSHOT 1: Pictures Grid & Drag to Select
# ==========================================
def create_screenshot_1():
    img = Image.new("RGB", (W, H), (242, 245, 243))
    draw = ImageDraw.Draw(img)
    draw_header_banner(draw, "Fast, Beautiful & Private", "Browse your memories in high resolution with smooth pinch zoom")
    
    px, py, pw, ph = 60, 310, 960, 1980
    draw_phone_frame(draw, px, py, pw, ph)
    
    # App Bar
    draw.text((px + 50, py + 100), "Lumina", fill=INK_COLOR, font=get_font(52, bold=True))
    draw.text((px + 50, py + 165), "Photos & Videos", fill=MUTED_COLOR, font=get_font(26, bold=True))
    
    # Filter Row (New v3.0.0 design!)
    fy = py + 225
    draw.text((px + 50, fy + 12), "1,248 ITEMS", fill=MUTED_COLOR, font=get_font(24, bold=True))
    
    # Filter pills (All, Photos, Videos, Favorites)
    pill_x = px + pw - 420
    draw.rounded_rectangle([pill_x, fy, pill_x + 300, fy + 56], radius=28, fill=CHIP_BG)
    filters = ["All", "Ph", "Vi", "Fav"]
    for i, f in enumerate(filters):
        fx = pill_x + i * 75 + 10
        if i == 0:
            draw.rounded_rectangle([fx, fy + 4, fx + 65, fy + 52], radius=24, fill=(255, 255, 255))
            draw.text((fx + 16, fy + 14), f, fill=GREEN_ACCENT, font=get_font(22, bold=True))
        else:
            draw.text((fx + 16, fy + 14), f, fill=MUTED_COLOR, font=get_font(22, bold=False))
            
    # Sort/Group Icon button
    draw.ellipse([px + pw - 90, fy + 4, px + pw - 42, fy + 52], fill=CHIP_BG)
    draw.text((px + pw - 74, fy + 14), "=", fill=INK_COLOR, font=get_font(24, bold=True))
    
    # Section Header
    draw.text((px + 50, fy + 90), "Today", fill=INK_COLOR, font=get_font(34, bold=True))
    draw.text((px + 160, fy + 95), "6", fill=MUTED_COLOR, font=get_font(24, bold=False))
    
    # Photo Grid (3 columns)
    seeds = ["mountain", "sunset", "forest", "ocean", "architecture", "coffee"]
    gx = px + 40
    gy = fy + 145
    col_w = (pw - 100) // 3
    spacing = 10
    
    for idx, s in enumerate(seeds):
        r = idx // 3
        c = idx % 3
        ix = gx + c * (col_w + spacing)
        iy = gy + r * (col_w + spacing)
        
        photo = generate_procedural_photo(col_w, col_w, s)
        img.paste(photo, (ix, iy))
        
        # Draw video badge on 2nd item
        if idx == 1:
            draw.rounded_rectangle([ix + col_w - 95, iy + col_w - 40, ix + col_w - 10, iy + col_w - 10], radius=8, fill=(0, 0, 0))
            draw.text((ix + col_w - 85, iy + col_w - 38), "0:45", fill=(255, 255, 255), font=font_badge)

    # Section 2: Yesterday
    s2_y = gy + 2 * (col_w + spacing) + 30
    draw.text((px + 50, s2_y), "Yesterday", fill=INK_COLOR, font=get_font(34, bold=True))
    draw.text((px + 220, s2_y + 5), "9", fill=MUTED_COLOR, font=get_font(24, bold=False))
    
    gy2 = s2_y + 55
    seeds2 = ["sunset", "coffee", "ocean", "forest", "mountain", "architecture"]
    for idx, s in enumerate(seeds2):
        r = idx // 3
        c = idx % 3
        ix = gx + c * (col_w + spacing)
        iy = gy2 + r * (col_w + spacing)
        photo = generate_procedural_photo(col_w, col_w, s)
        img.paste(photo, (ix, iy))
        
    draw_bottom_bar(draw, px, py + ph, pw, active_tab="photos")
    img.save(f"{OUTPUT_DIR}/screenshot_1_gallery.png", quality=95)
    print("Screenshot 1 created.")

# ==========================================
# SCREENSHOT 2: Swipe Clean Up Mode
# ==========================================
def create_screenshot_2():
    img = Image.new("RGB", (W, H), (242, 245, 243))
    draw = ImageDraw.Draw(img)
    draw_header_banner(draw, "Swipe to Clean & Declutter", "Keep what matters with a quick swipe. Reclaim your device storage")
    
    px, py, pw, ph = 60, 310, 960, 1980
    draw_phone_frame(draw, px, py, pw, ph)
    
    # Clean Up Top Bar
    draw.text((px + 50, py + 100), "Clean Up", fill=INK_COLOR, font=get_font(52, bold=True))
    draw.text((px + 50, py + 165), "SWIPE CARDS TO SORT", fill=MUTED_COLOR, font=get_font(24, bold=True))
    
    # Progress indicator
    draw.text((px + pw - 220, py + 115), "18 / 142", fill=GREEN_ACCENT, font=get_font(36, bold=True))
    
    # Swiping Card (Large photo card in center)
    card_w = pw - 120
    card_h = int(ph * 0.58)
    cx = px + 60
    cy = py + 230
    
    # Background shadow/border
    draw.rounded_rectangle([cx - 4, cy - 4, cx + card_w + 4, cy + card_h + 4], radius=32, fill=(210, 215, 212))
    draw.rounded_rectangle([cx, cy, cx + card_w, cy + card_h], radius=28, fill=(255, 255, 255))
    
    # Inside Card Photo
    photo_h = card_h - 180
    card_photo = generate_procedural_photo(card_w, photo_h, "architecture")
    img.paste(card_photo, (cx, cy))
    
    # Card Info Footer
    info_y = cy + photo_h + 25
    draw.text((cx + 30, info_y), "IMG_20261127_084210.jpg", fill=INK_COLOR, font=get_font(32, bold=True))
    draw.text((cx + 30, info_y + 45), "Nov 27, 2026 · 4.2 MB · 4032 x 3024", fill=MUTED_COLOR, font=get_font(24, bold=False))
    
    # Action Buttons: Keep & Trash
    btn_y = cy + card_h + 70
    # Trash button (Left)
    draw.ellipse([cx + 100, btn_y, cx + 240, btn_y + 140], fill=(254, 242, 242), outline=RED_ACCENT, width=4)
    draw.text((cx + 148, btn_y + 45), "X", fill=RED_ACCENT, font=get_font(50, bold=True))
    draw.text((cx + 130, btn_y + 155), "Trash", fill=RED_ACCENT, font=get_font(28, bold=True))
    
    # Keep button (Right)
    draw.ellipse([cx + card_w - 240, btn_y, cx + card_w - 100, btn_y + 140], fill=(236, 253, 245), outline=GREEN_ACCENT, width=4)
    draw.text((cx + card_w - 192, btn_y + 45), "V", fill=GREEN_ACCENT, font=get_font(50, bold=True))
    draw.text((cx + card_w - 210, btn_y + 155), "Keep", fill=GREEN_ACCENT, font=get_font(28, bold=True))

    draw_bottom_bar(draw, px, py + ph, pw, active_tab="clean up")
    img.save(f"{OUTPUT_DIR}/screenshot_2_cleanup.png", quality=95)
    print("Screenshot 2 created.")

# ==========================================
# SCREENSHOT 3: Private Vault & Safe Offline Privacy
# ==========================================
def create_screenshot_3():
    img = Image.new("RGB", (W, H), (242, 245, 243))
    draw = ImageDraw.Draw(img)
    draw_header_banner(draw, "100% Offline & Private Vault", "Zero internet permissions. Keep confidential photos locked with PIN")
    
    px, py, pw, ph = 60, 310, 960, 1980
    draw_phone_frame(draw, px, py, pw, ph)
    
    # Vault Top Bar
    draw.text((px + 50, py + 100), "Hidden Vault", fill=INK_COLOR, font=get_font(52, bold=True))
    draw.text((px + 50, py + 165), "CONFIDENTIAL & ENCRYPTED", fill=MUTED_COLOR, font=get_font(24, bold=True))
    
    # Vault Banner
    vy = py + 240
    draw.rounded_rectangle([px + 50, vy, px + pw - 50, vy + 280], radius=28, fill=(236, 253, 245), outline=GREEN_ACCENT, width=3)
    draw.ellipse([px + 90, vy + 75, px + 210, vy + 195], fill=GREEN_ACCENT)
    draw.text((px + 130, vy + 105), "L", fill=(255, 255, 255), font=get_font(60, bold=True))
    draw.text((px + 240, vy + 85), "Protected Space", fill=INK_COLOR, font=get_font(38, bold=True))
    draw.text((px + 240, vy + 140), "Items here are hidden from all other apps\nand secured locally on this device.", fill=MUTED_COLOR, font=get_font(24, bold=False))
    
    # PIN Security Visual
    py_num = vy + 360
    draw.text((px + pw//2 - 140, py_num), "ENTER PIN TO UNLOCK", fill=MUTED_COLOR, font=get_font(26, bold=True))
    
    # PIN Dots
    dot_y = py_num + 60
    for i in range(4):
        dx = px + pw//2 - 120 + i * 80
        draw.ellipse([dx, dot_y, dx + 30, dot_y + 30], fill=GREEN_ACCENT if i < 2 else (200, 205, 202))
        
    # Keypad Grid
    ky = dot_y + 110
    keys = [["1", "2", "3"], ["4", "5", "6"], ["7", "8", "9"], ["", "0", "<"]]
    btn_size = 130
    for r_idx, row in enumerate(keys):
        for c_idx, val in enumerate(row):
            if not val:
                continue
            kx = px + pw//2 - 240 + c_idx * 170
            ky_pos = ky + r_idx * 160
            draw.ellipse([kx, ky_pos, kx + btn_size, ky_pos + btn_size], fill=(255, 255, 255), outline=(225, 230, 228), width=3)
            draw.text((kx + 48, ky_pos + 38), val, fill=INK_COLOR, font=get_font(46, bold=True))
            
    # Privacy badge at bottom
    priv_y = ky + 4 * 160 + 20
    draw.rounded_rectangle([px + 120, priv_y, px + pw - 120, priv_y + 80], radius=40, fill=CHIP_BG)
    draw.text((px + 190, priv_y + 26), "Zero Internet Access · 100% Private", fill=GREEN_ACCENT, font=get_font(26, bold=True))

    draw_bottom_bar(draw, px, py + ph, pw, active_tab="more")
    img.save(f"{OUTPUT_DIR}/screenshot_3_vault.png", quality=95)
    print("Screenshot 3 created.")

if __name__ == "__main__":
    create_screenshot_1()
    create_screenshot_2()
    create_screenshot_3()
    print("All screenshots generated successfully.")
