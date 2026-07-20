import lgpio
import socket
import time
import threading

# socket setup
mac_ip = "192.168.68.201"
port = 5050
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((mac_ip, port))
print("Connected to Mac!")

# button and light setup
h = lgpio.gpiochip_open(0)

buttons = {
    5: "green",
    22: "red",
    27: "yellow",
    17: "blue",
    25: "enter"
}

yellow_lights = [12, 16, 20, 21]  # correct position
red_lights = [24, 13, 19, 26]     # wrong position

for pin in buttons:
    lgpio.gpio_claim_input(h, pin, lgpio.SET_PULL_DOWN)
for pin in yellow_lights + red_lights:
    lgpio.gpio_claim_output(h, pin)

def handle_hints(hint_string):
    print(f"Received hints: {hint_string}")
    hints = hint_string.replace("hints:", "").split(",")
    print(f"Parsed hints: {hints}")
    for pin in yellow_lights + red_lights:
        lgpio.gpio_write(h, pin, 0)
    for i, hint in enumerate(hints):
        print(f"Hint {i}: {hint}")
        if hint == "yellow":
            lgpio.gpio_write(h, yellow_lights[i], 1)
        elif hint == "red":
            print(f"Turning on red light at index {i}, pin {red_lights[i]}")
            lgpio.gpio_write(h, red_lights[i], 1)

running = True

def receive_messages():
    while running:
        try:
            data = s.recv(1024).decode()
            print(f"Raw received: {data}")
            if data.startswith("hints:"):
                handle_hints(data.strip())
        except:
            break
# start receiver thread
receiver = threading.Thread(target=receive_messages)
receiver.daemon = True
receiver.start()

print("Ready! Press buttons...")

try:
    while True:
        for pin, name in buttons.items():
            if lgpio.gpio_read(h, pin) == 1:
                print(f"Sending: {name}")
                s.sendall(f"{name}\n".encode())
                time.sleep(0.3)

except KeyboardInterrupt:
    print("Done!")
finally:
    running = False
    s.close()
    lgpio.gpiochip_close(h)