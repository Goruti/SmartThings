import requests
import xml.etree.ElementTree as etree
import socket

def getSessionid(ip):
    session_id = None
    url = "http://"+ip+":8080/roap/api/auth"
    payload = "<auth><type>AuthReq</type><value>202887</value></auth>"
    headers = {
        'content-type': "application/atom+xml",
    }
    try:
        r = requests.request("POST", url, data=payload, headers=headers)
    except requests.exceptions.RequestException as e:
        print e
    else:
        if r.status_code == 200:
            tree = etree.XML(r.text)
            session_id = tree.find('session').text
    return session_id


def TurnTvOff(ip):
    error = "OK"
    code = 200
    session_id = getSessionid(ip)
    if session_id:
        url = "http://"+ip+":8080/roap/api/command"
        headers = { 'content-type': "application/atom+xml" }
        payload = "<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\"?><command>\"<name>HandleKeyInput</name>" \
                  "<value>1</value></command>"
        try:
            r = requests.request("POST", url, data=payload, headers=headers)
        except requests.exceptions.RequestException as e:
            error = e
            code = 500
        else:
            if r.status_code != 200:
                error = r.text
                code = r.status_code
    else:
        code = 500
        error = "Cannot get a Session ID"

    return code, error


def ControlAction(ip, action):
    error = "OK"
    code = 200
    session_id = getSessionid(ip)
    if session_id:
        url = "http://"+ip+":8080/roap/api/command"
        headers = { 'content-type': "application/atom+xml" }
        payload = "<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\"?><command>\"<name>HandleKeyInput</name>" \
                  "<value>"+action+"</value></command>"
        try:
            r = requests.request("POST", url, data=payload, headers=headers)
        except requests.exceptions.RequestException as e:
            error = e
            code = 500
        else:
            if r.status_code != 200:
                error = r.text
                code = r.status_code
    else:
        code = 500
        error = "Cannot get a Session ID"

    return code, error


def vol_up(UDP_IP):
    UDP_PORT = 9741
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(3)
    MESSAGE = "080312002a080801120408181002"
    sock.sendto(bytes(bytearray.fromhex(MESSAGE)), (UDP_IP, UDP_PORT))
    MESSAGE = "080312002a080801120408181001"
    sock.sendto(bytes(bytearray.fromhex(MESSAGE)), (UDP_IP, UDP_PORT))
    sock.close()

    return 200, "ok"


def vol_down(UDP_IP):
    UDP_PORT = 9741
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(3)
    MESSAGE = "080312002a080801120408191002"
    sock.sendto(bytes(bytearray.fromhex(MESSAGE)), (UDP_IP, UDP_PORT))
    MESSAGE = "080312002a080801120408191001"
    sock.sendto(bytes(bytearray.fromhex(MESSAGE)), (UDP_IP, UDP_PORT))
    sock.close()

    return 200, "ok"


def vol_mute(UDP_IP):
    UDP_PORT = 9741
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(3)
    MESSAGE = "080312002a0808011204081a1002"
    sock.sendto(bytes(bytearray.fromhex(MESSAGE)), (UDP_IP, UDP_PORT))
    MESSAGE = "080312002a0808011204081a1001"
    sock.sendto(bytes(bytearray.fromhex(MESSAGE)), (UDP_IP, UDP_PORT))
    sock.close()

    return 200, "ok"
