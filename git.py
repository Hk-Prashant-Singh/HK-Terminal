import os, sys, shutil, time, subprocess

def get_terminal_size():
    # Automatic screen size selection
    columns, _ = shutil.get_terminal_size()
    return columns

def print_banner():
    cols = get_terminal_size()
    banner = f"""
    \033[1;35m{'━' * cols}
    {'HK TECH WIZARD - CUSTOM GATE CONTROL'.center(cols)}
    {'OPERATOR: PRASHANT BHAI | LOC: ADRA'.center(cols)}
    {'━' * cols}\033[0m
    """
    print(banner)

def check_and_install():
    packages = ["git", "gh", "python"]
    for pkg in packages:
        if subprocess.getstatusoutput(f"command -v {pkg}")[0] != 0:
            os.system(f"pkg install {pkg} -y")

def main_menu():
    while True:
        os.system('clear')
        print_banner()
        print("\033[1;36m[+] SELECT GATE OPERATION:\033[0m")
        print("  [1] 🛠️  Git Config (Set Email & Name)")
        print("  [2] 🖋️  Git Init & Add (Initialize)")
        print("  [3] 📝 Git Commit (Custom Message)")
        print("  [4] 🔗 Git Remote (Link Repo)")
        print("  [5] 🚀 Git Push (Deploy to Cloud)")
        print("  [0] ❌ Exit")

        choice = input(f"\n\033[1;33m[+] Prashant Bhai, enter option: \033[0m")

        if choice == '1':
            # User input for Git Config
            u_email = input("\033[1;34m[?] Enter GitHub Email: \033[0m")
            u_name = input("\033[1;34m[?] Enter GitHub Name: \033[0m")
            os.system(f'git config --global user.email "{u_email}"')
            os.system(f'git config --global user.name "{u_name}"')
            print("\033[1;32m[✔] Config Updated!\033[0m")
            
        elif choice == '2':
            os.system("git init")
            os.system("git add .")
            print("\033[1;32m[✔] Repo Initialized & Files Added.\033[0m")
            
        elif choice == '3':
            # User input for Commit Message
            msg = input("\033[1;34m[?] Enter Commit Message (e.g., First Commit): \033[0m")
            os.system(f'git commit -m "{msg}"')
            os.system("git branch -M main")
            
        elif choice == '4':
            # Flexible repo linking
            repo = "https://github.com/Hk-Prashant-Singh/HK-Terminal.git"
            os.system(f"git remote add origin {repo} || git remote set-url origin {repo}")
            print(f"\033[1;32m[✔] Linked to {repo}\033[0m")
            
        elif choice == '5':
            print("\033[1;31m[!] Use your Token as Password if asked.\033[0m")
            os.system("git push -u origin main")
            
        elif choice == '0':
            break
        input("\nPress Enter to continue...")

if __name__ == "__main__":
    check_and_install()
    main_menu()
