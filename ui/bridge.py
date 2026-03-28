# bridge.py — run this on the PC connected to the ELM327 via Bluetooth
import asyncio
import serial
import websockets

COM_PORT = "COM11"  # change if needed
BAUD     = 9600

async def handler(websocket):
    ser = serial.Serial(
        COM_PORT,
        BAUD,
        timeout=0.1,
        bytesize=serial.EIGHTBITS,
        parity=serial.PARITY_NONE,
        stopbits=serial.STOPBITS_ONE
    )
    print(f"✅ Connected to ELM327 on {COM_PORT}")

    async def serial_to_ws():
        loop = asyncio.get_event_loop()
        buffer = ""
        while True:
            data = await loop.run_in_executor(
                None, lambda: ser.read(ser.in_waiting or 1)
            )
            if data:
                chunk = data.decode(errors="ignore")
                buffer += chunk
                print(f"RAW: {repr(chunk)}")
                # Only send to websocket when full response is ready (ends with >)
                if ">" in buffer:
                    await websocket.send(buffer)
                    print(f"SENT TO WS: {repr(buffer)}")
                    buffer = ""

    async def ws_to_serial():
        async for msg in websocket:
            print(f"SENDING: {repr(msg)}")
            # Flush any leftover bytes in serial buffer before sending
            ser.reset_input_buffer()
            ser.write((msg + "\r").encode())
            await asyncio.sleep(0.3)

    await asyncio.gather(serial_to_ws(), ws_to_serial())

async def main():
    async with websockets.serve(handler, "0.0.0.0", 8765):
        print("🌐 Bridge running on ws://localhost:8765")
        await asyncio.Future()

asyncio.run(main())
