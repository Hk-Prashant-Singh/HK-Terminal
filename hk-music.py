import curses
import os
import time
import subprocess
import random
import json

music_dir = os.path.expanduser("~/hk-music")

# ================= SOUND MODES =================
sound_modes = {
    "NORMAL": "",
    "BASS": "lavfi=[bass=g=10]",
    "ROCK": "lavfi=[equalizer=f=1000:width_type=h:width=200:g=5]",
    "DOLBY": "lavfi=[aecho=0.8:0.9:1000:0.3]"
}

speeds = [0.8, 1.0, 1.2, 1.5]

# ================= THEMES =================
themes = [
    curses.COLOR_GREEN,
    curses.COLOR_MAGENTA,
    curses.COLOR_RED,
    curses.COLOR_CYAN,
    curses.COLOR_YELLOW,
]

# ================= DUAL COLOR =================
def get_dynamic_color(theme_color):
    if theme_color == curses.COLOR_GREEN:
        return curses.COLOR_MAGENTA
    elif theme_color == curses.COLOR_MAGENTA:
        return curses.COLOR_GREEN
    elif theme_color == curses.COLOR_RED:
        return curses.COLOR_CYAN
    elif theme_color == curses.COLOR_CYAN:
        return curses.COLOR_YELLOW
    elif theme_color == curses.COLOR_YELLOW:
        return curses.COLOR_BLUE
    else:
        return curses.COLOR_WHITE

# ================= VOLUME =================
def change_volume(step):
    try:
        output = subprocess.check_output("termux-volume", shell=True)
        data = json.loads(output)
        for s in data:
            if s["stream"] == "music":
                new = max(0, min(s["max_volume"], s["volume"] + step))
                subprocess.run(f"termux-volume music {new}", shell=True)
                return new
    except:
        return 0

def get_volume():
    try:
        output = subprocess.check_output("termux-volume", shell=True)
        data = json.loads(output)
        for s in data:
            if s["stream"] == "music":
                return s["volume"]
    except:
        return 0

# ================= PLAYER =================
def start_song(path, mode_filter="", speed=1.0):
    os.system("pkill mpv")
    cmd = ["mpv", "--no-video", "--really-quiet"]
    if mode_filter:
        cmd.append(f"--af={mode_filter}")
    cmd.append(f"--speed={speed}")
    cmd.append(path)
    return subprocess.Popen(cmd,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL)

# ================= DURATION =================
def get_duration(path):
    try:
        cmd = f"ffprobe -v error -show_entries format=duration -of csv=p=0 '{path}'"
        out = subprocess.check_output(cmd, shell=True)
        return float(out.decode().strip())
    except:
        return 1

def format_time(sec):
    sec = int(sec)
    return f"{sec//60:02}:{sec%60:02}"

# ================= MAIN =================
def main(stdscr):

    curses.start_color()
    curses.use_default_colors()
    curses.curs_set(0)
    stdscr.nodelay(True)
    stdscr.keypad(True)

    theme_index = 0
    curses.init_pair(1, themes[theme_index], curses.COLOR_BLACK)
    curses.init_pair(2, get_dynamic_color(themes[theme_index]), curses.COLOR_BLACK)

    songs = [f for f in os.listdir(music_dir) if f.endswith(".mp3")]
    filtered = songs.copy()

    if not songs:
        stdscr.addstr(0,0,"No Songs Found")
        stdscr.refresh()
        time.sleep(2)
        return

    curr = 0
    paused = False
    mode_names = list(sound_modes.keys())
    mode_index = 0
    speed_index = 1
    search_query = ""

    mpv_process = start_song(os.path.join(music_dir, filtered[curr]))
    total_duration = get_duration(os.path.join(music_dir, filtered[curr]))
    start_time = time.time()

    while True:
        h, w = stdscr.getmaxyx()
        stdscr.erase()

        primary = themes[theme_index]
        secondary = get_dynamic_color(primary)

        curses.init_pair(1, primary, curses.COLOR_BLACK)
        curses.init_pair(2, secondary, curses.COLOR_BLACK)

        stdscr.attron(curses.color_pair(1))

        # SEARCH BAR
        stdscr.addstr(1, 2, f"SEARCH: {search_query}")
        stdscr.addstr(2, 2, "─"*(w-4))

        # VISUALIZER
        center_y = h//2 - 6
        for i in range(w-6):
            for j in range(random.randint(1,4)):
                stdscr.addstr(center_y-j, 3+i, random.choice(["0","1"]))

        stdscr.attroff(curses.color_pair(1))

        # TIME + PROGRESS (Secondary Color)
        elapsed = time.time() - start_time if not paused else 0
        percent = min((elapsed/total_duration)*100,100)

        bar_width = w//2
        filled = int((percent/100)*bar_width)
        bar = "█"*filled + "░"*(bar_width-filled)

        progress = f"{bar} {int(percent)}%"
        time_text = f"{format_time(elapsed)}/{format_time(total_duration)}"

        stdscr.attron(curses.color_pair(2))
        stdscr.addstr(center_y+2,(w-len(progress))//2,progress)
        stdscr.addstr(center_y+3,(w-len(time_text))//2,time_text)

        status = "PLAYING" if not paused else "PAUSED"
        vol = get_volume()
        status_line = f"🎧 {status} | MODE:{mode_names[mode_index]} | SPD:{speeds[speed_index]}x | VOL:{vol}"
        stdscr.addstr(center_y+5,(w-len(status_line))//2,status_line)
        stdscr.attroff(curses.color_pair(2))

        # SONG NAME
        stdscr.attron(curses.color_pair(1))
        song = filtered[curr]
        stdscr.addstr(center_y+7,(w-len(song))//2,song)

        # FOOTER
        footer = "HK PRASHANT SINGH  |  INDIAN HACKER"
        stdscr.addstr(h-2,(w-len(footer))//2,footer)
        stdscr.attroff(curses.color_pair(1))

        stdscr.refresh()

        # AUTO NEXT
        if percent >= 100:
            curr = (curr+1)%len(filtered)
            mpv_process = start_song(os.path.join(music_dir,filtered[curr]),
                                     sound_modes[mode_names[mode_index]],
                                     speeds[speed_index])
            total_duration = get_duration(os.path.join(music_dir,filtered[curr]))
            start_time = time.time()

        key = stdscr.getch()

        if key == ord('q'):
            break
        elif key == ord(' '):
            if paused:
                os.system("pkill -CONT mpv")
                paused=False
                start_time=time.time()
            else:
                os.system("pkill -STOP mpv")
                paused=True
        elif key == ord('+'):
            change_volume(1)
        elif key == ord('-'):
            change_volume(-1)
        elif key == curses.KEY_RIGHT:
            curr=(curr+1)%len(filtered)
            mpv_process=start_song(os.path.join(music_dir,filtered[curr]),
                                   sound_modes[mode_names[mode_index]],
                                   speeds[speed_index])
            total_duration=get_duration(os.path.join(music_dir,filtered[curr]))
            start_time=time.time()
        elif key == curses.KEY_LEFT:
            curr=(curr-1)%len(filtered)
            mpv_process=start_song(os.path.join(music_dir,filtered[curr]),
                                   sound_modes[mode_names[mode_index]],
                                   speeds[speed_index])
            total_duration=get_duration(os.path.join(music_dir,filtered[curr]))
            start_time=time.time()
        elif key == ord('m'):
            mode_index=(mode_index+1)%len(mode_names)
        elif key == ord('p'):
            speed_index=(speed_index+1)%len(speeds)
        elif key == ord('t'):
            theme_index=(theme_index+1)%len(themes)
        elif key == ord('s'):
            curses.echo()
            stdscr.nodelay(False)
            stdscr.addstr(1,10," "*(w-12))
            stdscr.move(1,10)
            search_query=stdscr.getstr().decode()
            curses.noecho()
            stdscr.nodelay(True)
            filtered=[s for s in songs if search_query.lower() in s.lower()]
            if filtered:
                curr=0
                mpv_process=start_song(os.path.join(music_dir,filtered[curr]))
                total_duration=get_duration(os.path.join(music_dir,filtered[curr]))
                start_time=time.time()

        time.sleep(0.08)

    os.system("pkill mpv")

if __name__=="__main__":
    curses.wrapper(main)