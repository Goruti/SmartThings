import psutil
import os


def get_send_rpi_stats():
    cpu = psutil.cpu_percent()
    temp = float(os.popen('vcgencmd measure_temp').readline().replace("temp=", "").replace("'C\n", ""))
    memory = psutil.virtual_memory()
    disk = psutil.disk_usage('/')

    evt = {
        "type": "rpi_status",
        "body": {
            "temperature": temp,
            "cpuPercentage": cpu,
            "memory": memory.percent,
            "diskUsage": disk.percent,
            #"hubInfo": "online"
        }
    }

    return evt
