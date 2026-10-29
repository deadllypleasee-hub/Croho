"""Capture real app screens on the CI emulator after the instrumentation checks."""
from pathlib import Path
import json
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


def locate(value):
    for _ in range(7):
        tree = nodes()
        for node in tree.iter("node"):
            if value not in (node.get("text"), node.get("content-desc")):
                continue
            bounds = [int(n) for n in re.findall(r"\d+", node.get("bounds", ""))]
            if len(bounds) == 4:
                left, top, right, bottom = bounds
                if right > left and bottom > top:
                    return str((left + right) // 2), str((top + bottom) // 2)
        size = adb("shell", "wm", "size").decode()
        width, height = [int(n) for n in re.findall(r"(\d+)x(\d+)", size)[-1]]
        adb("shell", "input", "swipe", str(width // 2), str(height * 4 // 5),
            str(width // 2), str(height // 3), "250")
    raise RuntimeError(f"Visible app control not found: {value}")


def tap(value):
    x, y = locate(value)
    adb("shell", "input", "tap", x, y)


def run():
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
            # Input injection can return before the app runs its click handler.
            # First observe the acknowledged answer, then kill the actual process.
            locate(answer + ". Правильный ответ")
            adb("shell", "am", "force-stop", "ru.chronos.history")
            adb("shell", "am", "start", "-W", "-n", "ru.chronos.history/.MainActivity")
            locate(answer + ". Правильный ответ")
            screenshot("03-feedback")
        tap("Показать результат" if index == 4 else "Следующий вопрос")
    screenshot("04-result")
    tap("К ленте времени")
    screenshot("05-rewards")
    locate("Открыть главу")
    screenshot("06-epochs")


if __name__ == "__main__":
    try:
        run()
    except Exception:
        # Keep the UI and this synthetic quiz's state for diagnosis on either Android version.
        try:
            (OUTPUT / "failure-ui.xml").write_bytes(adb("exec-out", "cat", "/sdcard/chronos-preview.xml"))
            (OUTPUT / "failure.png").write_bytes(adb("exec-out", "screencap", "-p"))
            root = ET.fromstring(adb("exec-out", "run-as", "ru.chronos.history", "cat",
                                     "shared_prefs/chronos_progress_v1.xml"))
            state = {node.get("name"): node.get("value", node.text)
                     for node in root if node.get("name", "").startswith("best_")
                     or node.get("name") in ("session", "showing_quiz")}
            (OUTPUT / "failure-progress.json").write_text(json.dumps(state, ensure_ascii=False, indent=2))
        except Exception as diagnostic_error:
            print(f"Additional diagnostics unavailable: {diagnostic_error}", flush=True)
        raise
