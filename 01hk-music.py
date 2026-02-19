import curses
import os
import time
import subprocess
import random
import json

# ================= FOLDERS =================
OLD_TERMUX_FOLDER = os.path.expanduser("~/hk-music")
INTERNAL_FOLDER = "/storage/emulated/0/hk-music"
SDCARD_FOLDER = "/storage/12CE-E41D/hk-music"

SUPPORTED_FORMATS = (".mp3", ".wav", ".flac", ".m4a", ".ogg")

# ================= THEME =================
theme_colors = [
    curses.COLOR_RED,
    curses.COLOR_GREEN,
    curses.COLOR_CYAN,
    curses.COLOR_YELLOW,
    curses.COLOR_MAGENTA,
    curses.COLOR_BLUE,
    curses.COLOR_WHITE,
    202,  # Orange
    21,   # Deep Blue
    13    # Pink
]

theme_index = 0
theme_mode = 0
bass_mode = False
dolby_mode = False

# ================= SAFE PRINT =================
def safe_addstr(stdscr, y, x, text, color_pair=None):
    h, w = stdscr.getmaxyx()
    if 0 <= y < h:
        if x < 0:
            x = 0
        if x >= w:
            return
        if color_pair:
            stdscr.attron(color_pair)
            stdscr.addstr(y, x, text[:w - x])
            stdscr.attroff(color_pair)
        else:
            stdscr.addstr(y, x, text[:w - x])

# ================= BATTERY =================
def get_battery():
    try:
        output = subprocess.check_output("termux-battery-status", shell=True)
        data = json.loads(output.decode())
        return int(data.get("percentage", 0))
    except:
        return 0

def draw_battery_panel(stdscr, percentage):
    h, w = stdscr.getmaxyx()
    panel_width = min(20, w-4)
    bar_width = panel_width - 4
    filled = int((percentage / 100) * bar_width)
    empty = bar_width - filled
    panel_text = f"[{'█'*filled}{' ' * empty}] {percentage}%"
    y, x = 2, (w - panel_width) // 2
    # Red panel border
    stdscr.attron(curses.color_pair(1))
    stdscr.addstr(y-1, x-2, "+" + "-"*(panel_width) + "+")
    stdscr.addstr(y, x-2, "|")
    stdscr.addstr(y, x+panel_width-1, "|")
    stdscr.addstr(y+1, x-2, "+" + "-"*(panel_width) + "+")
    stdscr.attroff(curses.color_pair(1))
    # Battery bar text
    safe_addstr(stdscr, y, x, panel_text, curses.color_pair(1))

# ================= VOLUME =================
def change_system_volume(step):
    try:
        output = subprocess.check_output("termux-volume", shell=True)
        data = json.loads(output.decode())
        for stream in data:
            if "music" in stream.get("stream","").lower():
                current = int(stream.get("volume", 0))
                max_vol = int(stream.get("max_volume", 15))
                new_vol = max(0, min(max_vol, current + step))
                subprocess.run(f"termux-volume music {new_vol}", shell=True)
                return
    except:
        pass

def get_system_volume():
    try:
        output = subprocess.check_output("termux-volume", shell=True)
        data = json.loads(output.decode())
        for stream in data:
            if "music" in stream.get("stream","").lower():
                return int(stream.get("volume", 0))
    except:
        return 0

# ================= SONG SCAN =================
def scan_music():
    songs = []
    folders = [OLD_TERMUX_FOLDER, INTERNAL_FOLDER, SDCARD_FOLDER]
    for folder in folders:
        if os.path.exists(folder):
            for root, dirs, files in os.walk(folder):
                for file in files:
                    if file.lower().endswith(SUPPORTED_FORMATS):
                        songs.append(os.path.join(root, file))
    return sorted(songs)

# ================= DURATION =================
def get_duration(path):
    try:
        cmd = f"ffprobe -v error -show_entries format=duration -of csv=p=0 '{path}'"
        output = subprocess.check_output(cmd, shell=True)
        return float(output.decode().strip())
    except:
        return 1

def format_time(sec):
    sec = int(sec)
    return f"{sec//60:02}:{sec%60:02}"

# ================= START SONG =================
def start_song(path):
    global bass_mode, dolby_mode
    os.system("pkill mpv")
    audio_filter = ""
    if bass_mode:
        audio_filter += "bass=g=8,"
    if dolby_mode:
        audio_filter += "treble=g=5,"
    if audio_filter.endswith(","):
        audio_filter = audio_filter[:-1]
    command = ["mpv", "--no-video", "--really-quiet"]
    if audio_filter:
        command.append(f"--af=lavfi=[{audio_filter}]")
    command.append(path)
    return subprocess.Popen(
        command,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL
    )

# ================= TAG PANEL =================
tag_text = "  ★ HK PRASHANT SINGH | ELITE ALPHA INDIAN HACKER ★"
tag_color_index = 0  # initial color for bottom tag

def draw_tag_panel(stdscr):
    global tag_color_index
    h, w = stdscr.getmaxyx()
    panel_width = min(len(tag_text)+4, w-2)
    x = (w - panel_width) // 2
    y = h - 2
    # border
    stdscr.attron(curses.color_pair(tag_color_index + 1))
    safe_addstr(stdscr, y-1, x-2, "+" + "-"*(panel_width) + "+")
    safe_addstr(stdscr, y, x-2, "|")
    safe_addstr(stdscr, y, x+panel_width-1, "|")
    safe_addstr(stdscr, y+1, x-2, "+" + "-"*(panel_width) + "+")
    stdscr.attroff(curses.color_pair(tag_color_index + 1))
    # tag
    safe_addstr(stdscr, y, x, tag_text, curses.color_pair(tag_color_index + 1))

# ================= MAIN =================
def main(stdscr):
    global theme_index, theme_mode, bass_mode, dolby_mode, tag_color_index

    curses.start_color()
    curses.use_default_colors()
    for i, color in enumerate(theme_colors):
        curses.init_pair(i+1, color, -1)

    curses.curs_set(0)
    stdscr.nodelay(True)
    stdscr.keypad(True)

    songs = scan_music()
    if not songs:
        safe_addstr(stdscr, 2, 2, "No music files found")
        stdscr.refresh()
        time.sleep(2)
        return

    curr = 0
    paused = False
    paused_time = 0

    mpv_process = start_song(songs[curr])
    total_duration = get_duration(songs[curr])
    start_time = time.time()

    while True:
        h, w = stdscr.getmaxyx()
        stdscr.erase()

        # Battery panel
        battery = get_battery()
        draw_battery_panel(stdscr, battery)

        center_y = h // 2 - 6
        # Theme
        curses.init_pair(20, theme_colors[theme_index], -1)
        stdscr.attron(curses.color_pair(20))

        # Visualizer Safe
        waveform_width = w - 6
        start_x = 3
        for i in range(waveform_width):
            height = random.randint(1, 5)
            for j in range(height):
                y = center_y - j
                x = start_x + i
                if 0 <= y < h and 0 <= x < w:
                    stdscr.addstr(y, x, random.choice(["0", "1"]))

        if 0 <= center_y + 1 < h:
            safe_addstr(stdscr, center_y + 1, start_x, "─" * waveform_width)

        # Time
        elapsed = paused_time if paused else time.time() - start_time
        percent = min((elapsed / total_duration) * 100, 100)

        bar_width = w // 2
        filled = int((percent / 100) * bar_width)
        bar = "█"*filled + "░"*(bar_width-filled)
        progress_text = f"{bar}  {format_time(elapsed)}/{format_time(total_duration)}"
        safe_addstr(stdscr, center_y + 3, (w - len(progress_text)) // 2, progress_text)

        # Controls
        controls = "← PREV | SPACE PLAY/PAUSE | NEXT → | + - VOL | B BASS | D DOLBY | T THEME | C COLOR | H TAG COLOR | Q EXIT"
        safe_addstr(stdscr, center_y + 5, (w - len(controls)) // 2, controls)

        # Status
        vol = get_system_volume()
        status = "PLAYING" if not paused else "PAUSED"
        status_text = f"{status} | VOL {vol} | BASS {'ON' if bass_mode else 'OFF'} | DOLBY {'ON' if dolby_mode else 'OFF'}"
        safe_addstr(stdscr, center_y + 7, (w - len(status_text)) // 2, status_text)

        # Song name
        song_name = os.path.basename(songs[curr])
        safe_addstr(stdscr, center_y + 9, (w - len(song_name)) // 2, song_name)

        # Draw bottom tag panel
        draw_tag_panel(stdscr)

        stdscr.refresh()

        # Auto next
        if percent >= 100:
            curr = (curr + 1) % len(songs)
            mpv_process = start_song(songs[curr])
            total_duration = get_duration(songs[curr])
            start_time = time.time()
            paused = False

        key = stdscr.getch()

        if key in (ord('q'), ord('Q')):
            break

        elif key == ord(' '):
            if paused:
                os.system("pkill -CONT mpv")
                start_time = time.time() - paused_time
                paused = False
            else:
                os.system("pkill -STOP mpv")
                paused_time = elapsed
                paused = True

        elif key in (ord('+'), ord('1')):
            change_system_volume(+1)
        elif key in (ord('-'), ord('0')):
            change_system_volume(-1)

        elif key == curses.KEY_RIGHT:
            curr = (curr + 1) % len(songs)
            mpv_process = start_song(songs[curr])
            total_duration = get_duration(songs[curr])
            start_time = time.time()
        elif key == curses.KEY_LEFT:
            curr = (curr - 1) % len(songs)
            mpv_process = start_song(songs[curr])
            total_duration = get_duration(songs[curr])
            start_time = time.time()

        elif key in (ord('b'), ord('B')):
            bass_mode = not bass_mode
            mpv_process = start_song(songs[curr])

        elif key in (ord('d'), ord('D')):
            dolby_mode = not dolby_mode
            mpv_process = start_song(songs[curr])

        elif key in (ord('t'), ord('T')):
            theme_mode = 1 if theme_mode == 0 else 0
            theme_index = (theme_index + 1) % 10

        elif key in (ord('c'), ord('C')):
            theme_index = (theme_index + 1) % 10

        elif key in (ord('h'), ord('H')):
            tag_color_index = (tag_color_index + 1) % len(theme_colors)

        time.sleep(0.08)

    os.system("pkill mpv")


if __name__ == "__main__":
    try:
        curses.wrapper(main)
    except KeyboardInterrupt:
        os.system("pkill mpv")