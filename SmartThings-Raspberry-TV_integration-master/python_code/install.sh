#!/bin/bash

sudo apt-get install -y cec-utils python-pip nmap
sudo pip2 install flask_httpauth python-nmap

sudo sed -i -e '$i echo "Starting Flask Server"' /etc/rc.local
sudo sed -i -e '$i python /home/pi/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/flask_restful_server.py  2>&1 | logger &\n' /etc/rc.local

echo "Please set you passwords in \
'/home/pi/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/conf.py' file"

echo "Starting Flask Server"
python /home/pi/git/SmartThings/SmartThings-Raspberry-TV_integration-master/python_code/flask_restful_server.py  2>&1 | logger &


echo "GETTING ST IP"
ip=`sudo nmap -n 192.168.1.0/24 -p39500 --open | grep "Nmap scan report for"`
ST_IP="${ip/'Nmap scan report for '/}"

if [ "$ST_IP" ]; then
    sudo sed -i -e '$i echo "Starting ping to ST hub"' /etc/rc.local
    sudo sed -i -e "$i sleep 20; /bin/ping $ST_IP > /dev/null 2>&1 &\n" /etc/rc.local;
    echo "Starting ping to ST hub";
    /bin/ping $ST_IP > /dev/null 2>&1 &
else
    echo "Smartthings Hub is not UP"
fi