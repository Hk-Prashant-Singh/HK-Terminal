import os, shutil, getpass, subprocess, json

def get_width():
    return shutil.get_terminal_size((80, 20)).columns

def print_header(text, color="\033[1;35m"):
    width = get_width()
    print(f"{color}" + "━" * width)
    print(text.center(width))
    print("━" * width + "\033[0m")

def setup_vault():
    if not os.path.exists("hk-s"): os.makedirs("hk-s")
    return "hk-s"

def hk_engine():
    os.system('clear')
    vault = setup_vault()
    print_header("HK TECH WIZARD - SIGNATURE ENGINE")
    
    # Locked Credentials
    details = {
        "NAME": "HK Prashant Singh",
        "ORG": "HK Tech",
        "LOC": "Adra, West Bengal",
        "DNAME": "CN=HK Prashant Singh, OU=Cybersecurity, O=HK Tech, L=Adra, ST=West Bengal, C=IN"
    }

    print(f"\033[1;34m[!] OPERATOR: {details['NAME']} | {details['LOC']}\033[0m\n")
    print("  [1] 🔑 Full Auto: Sign APK + Verify Fingerprint")
    print("  [2] 🛡️  Create GitHub Identity Proof (.json)")
    print("  [3] 📂 Cleanup Workspace")
    print("  [4] ❌ Exit")

    choice = input("\n[+] Prashant Bhai, select process: ")

    if choice == '1':
        print_header("SIGNING & VERIFICATION PROCESS")
        p1 = getpass.getpass("[+] Set Master Password: ")
        p2 = getpass.getpass("[+] Confirm Password: ")

        if p1 != p2:
            print("\033[1;31m[!] Password Mismatch!\033[0m")
            return

        ks_path = os.path.join(vault, "hk_terminal_auto.jks")
        alias = "hk_auto_alias"

        # 1. Generate JKS if missing
        if not os.path.exists(ks_path):
            os.system(f'keytool -genkey -v -keystore {ks_path} -alias {alias} -keyalg RSA -keysize 2048 -validity 10000 -dname "{details["DNAME"]}" -storepass {p1} -keypass {p1} -noprompt')

        # 2. Extract Fingerprint for GitHub Verification
        print("\n[*] Extracting SHA-256 Fingerprint for Repo...")
        cmd = f'keytool -list -v -keystore {ks_path} -alias {alias} -storepass {p1}'
        try:
            output = subprocess.check_output(cmd, shell=True).decode()
            sha256 = [line.strip() for line in output.split('\n') if "SHA256:" in line][0]
            print(f"\033[1;32m[✔] VERIFIED SHA256: {sha256}\033[0m")
        except: print("[!] Keytool Error.")

        # 3. Sign APK
        target = next((f for f in os.listdir(".") if f.endswith(".apk") and "-HK-SIGNED" not in f), None)
        if target:
            out_apk = os.path.join(vault, target.replace(".apk", "-HK-SIGNED.apk"))
            os.system(f"apksigner sign --ks {ks_path} --out {out_apk} --ks-pass pass:{p1} {target}")
            print(f"\n\033[1;32m[✔] DONE: Signed APK and Fingerprint ready in /hk-s/\033[0m")
        else:
            print("\033[1;31m[!] No APK found to sign.\033[0m")

    elif choice == '2':
        # GitHub Identity Proof
        id_file = os.path.join(vault, "github_signature.json")
        with open(id_file, "w") as f:
            json.dump(details, f, indent=4)
        print(f"\033[1;32m[✔] Identity Proof created in /hk-s/\033[0m")

if __name__ == "__main__":
    hk_engine()
