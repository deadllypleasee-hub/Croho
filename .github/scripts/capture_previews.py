"""Capture real app screens on the CI emulator after the instrumentation checks."""
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

OUTPUT = Path("app/build/reports/screenshots")
OUTPUT.mkdir(parents=True, exist_ok=True)


def adb(*args):
    return subprocess.run(["adb", *args], check=True, stdout=subprocess.PIPE,
                          stderr=subprocess.PIPE, timeout=60).stdout


def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/chronos-preview.xml")
    return ET.fromstring(adb("exec-out", "cat", "/sdcard/chronos-preview.xml"))


def screenshot(name):
    nodes()  # Wait for the current window and layout to become idle.
    (OUTPUT / f"{name}.png").write_bytes(adb("exec-out", "screencap", "-p"))
    print(f"Captured {name}", flush=True)


def tap(value):
    for _ in range(7):
        tree = nodes()
        for node in tree.iter("node"):
            if value not in (node.get("text"), node.get("content-desc")):
                continue
            bounds = [int(n) for n in re.findall(r"\d+", node.get("bounds", ""))]
            if len(bounds) == 4:
                left, top, right, bottom = bounds
                if right > left and bottom > top:
                    adb("shell", "input", "tap", str((left + right) // 2), str((top + bottom) // 2))
                    return
        size = adb("shell", "wm", "size").decode()
        width, height = [int(n) for n in re.findall(r"(\d+)x(\d+)", size)[-1]]
        adb("shell", "input", "swipe", str(width // 2), str(height * 4 // 5),
            str(width // 2), str(height // 3), "250")
    raise RuntimeError(f"Visible app control not found: {value}")


adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk")
adb("shell", "pm", "clear", "ru.chronos.history")
adb("shell", "am", "start", "-W", "-n", "ru.chronos.history/.MainActivity")
screenshot("01-home")
tap("Начать путешествие")
screenshot("02-question")
answers = ["Тигр и Евфрат", "Иероглифы", "Хаммурапи", "Индская", "Сплав меди с оловом"]
for index, answer in enumerate(answers):
    tap(answer)
    if index == 0:
        screenshot("03-feedback")
    tap("Показать результат" if index == 4 else "Следующий вопрос")
screenshot("04-result")
tap("К ленте времени")
screenshot("05-rewards")
