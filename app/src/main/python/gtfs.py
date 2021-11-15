"""
Author: David Pape Jr
Summary: A simple web scraping script for the patco transit organization's schedules page

"""
from datetime import datetime
from datetime import date
from io import BytesIO
from urllib.request import urlopen
from zipfile import ZipFile
import re

def isSpecial():
    url = "http://www.ridepatco.org/schedules/schedules.asp"
    page = urlopen(url)
    html_bytes = page.read()
    html = html_bytes.decode("utf-8")

    today = date.today()

    srch_date = f", {today.month}/{today.day}"

    finding = re.findall(srch_date, html)

    if len(finding) > 0:
        return True
    return False

def readGTFS():
    resp = urlopen("http://www.ridepatco.org/developers/PortAuthorityTransitCorporation.zip")
    zipfile = ZipFile(BytesIO(resp.read()))
    return zipfile

def currentTime():
    now = datetime.now().time()
    hour, minute, second = now.hour, now.minute, now.second
    return (hour, minute, second)

def showFromTime( source ):
    stopCodes = ["Lindenwold", "Ashland", "Woodcrest", "Haddonfield", "Westmont",
             "Collingswood", "Ferry Avenue", "Broadway", "City Hall", "8th and Market",
             "9-10th and Locust", "12-13th and Locust", "15-16th and Locust"]
    stop_id = stopCodes.index(source)+1
    now = currentTime()
    if now[1] - 30 < 0:
        if now[0] - 1 < 0:
            showFrom = (11, 0)
        else:
            showFrom = (now[0]-1, 0)
    elif now[1] - 30 >= 0:
        showFrom = (now[0], 30)
    fromTime = f"{showFrom[0]}:{showFrom[1]}:00"
    return fromTime


###   DIRECT CALL   ###
if __name__ == '__main__':
    status = isSpecial()
    if status == True:
        print("Special schedules found for today.")
        act = input("Downoad special schedules? [y/n]: ")
        if act == 'y':
            print("Visit www.ridepatco.org/ for more info.")
    elif status == False:
        print("No special schedules found for today.")

