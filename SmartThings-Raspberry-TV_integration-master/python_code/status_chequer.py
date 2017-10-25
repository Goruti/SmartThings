import flask_restful_server
import conf
import json
import time


def main():
    TV_IP = conf.TV_IP
    TV_STATUS = ''

    while True:
        answer = json.loads(flask_restful_server.check_status(TV_IP))
        if TV_STATUS != answer.get("status"):
            TV_STATUS = answer.get("status")
            flask_restful_server.send_event(json.dumps(answer))
        time.sleep(60)


if __name__ == '__main__':
    main()