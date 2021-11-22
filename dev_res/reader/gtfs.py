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

# List of stations (trip_id = idx+1)
stopCodes = ["Lindenwold", "Ashland", "Woodcrest", "Haddonfield", "Westmont",
             "Collingswood", "Ferry Avenue", "Broadway", "City Hall", "8th and Market",
             "9-10th and Locust", "12-13th and Locust", "15-16th and Locust"]

def isSpecial():
    """ Function which utilizes urllib to determine if a special schedule is
        present for the current date.
        Returns: boolean value of True / False
    """
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
    """ Function which utilizes urllib and ZipFile to download the latest
        PortAuthorityTransitCorporation (PATCO) GTFS package.
        Returns: ZipFile object
    """
    resp = urlopen("http://www.ridepatco.org/developers/PortAuthorityTransitCorporation.zip")
    zipfile = ZipFile(BytesIO(resp.read()))
    return zipfile

def currentWkday():
    return datetime.today().weekday()

def currentTime():
    """ Function which utilizes urllib to determine if a special schedule is
        present for the current date. """
    now = datetime.now().time()
    hour, minute, second = now.hour, now.minute, now.second
    return [hour, minute, second]

def showFromTime( source ):
    """ Function which uses the current time to determine where to begin
        fetching train arrival times.
        Returns: string object
    """
    stop_id = stopCodes.index(source)+1
    now = currentTime()
    if now[1] - 15 < 0:
        if now[0] - 1 < 0:
            showFrom = ["11", str(now[1]-15)]
        else:
            showFrom = [str(now[0]-1), "00"]
    elif now[1] - 15 >= 0:
        showFrom = [str(now[0]), str(now[1]-15)]
    fromTime = f"{showFrom[0]}:{showFrom[1]}:00"
    return fromTime

def route_id( source, destination ):
    """ Function which determines the route_id based on the source stop_id
        and the destination stop_id.
        Returns: int object
    """
    sourceID, destinationID = stopCodes.index(source)+1, stopCodes.index(destination)+1
    if destinationID < sourceID:
        return 1
    else: return 2

def service_id():
    """ Function which utilizes currentWkday() function to determine the service_id
        based on day of week.
        Returns: int object
    """
    weekday = currentWkday()
    if weekday < 4:
        return 66
    elif weekday == 5:
        return 67
    else: return 68

def stop_id( source ):
    """ Function which determines the stop_id based on provided stop name and
        index relative to stopCodes list.
        Returns: int object
    """
    return stopCodes.index(source)+1
    
        
def trip_id( source, destination ):
    """ Function which determines the trip_id based on the given route_id and service_id
        by reading the trips data file.
        Returns: list object
    """
    # initialize variables
    routeID, serviceID = route_id(source, destination), service_id()
    term = f"{routeID},{serviceID},"
    trips = []

    # open trips.txt file as read-only data
    f = open("trips.txt", "r")

    # read data line-by-line and append relevant data to a list
    line = f.readline()
    while line != '':
        if line[:5] == term:
            splitLine = line.split(',')
            trips.append(splitLine[2])
        line = f.readline()
    f.close()

    return trips

def listSchedules( source, destination ):
    """ Function which utilizes urllib to determine if a special schedule is
        present for the current date. """
    # initialize variables
    trips, stopID = trip_id(source, destination), stop_id(source)
    arrivalTime = showFromTime(source)
    # concatenate serach term from data
    term = f"{tripID},{arrivalTime},{arrivalTime},{stopID},"


###   DIRECT CALL   ###
if __name__ == '__main__':
    status = isSpecial()
    if status == True:
        print("Special schedules found for today.")
        act = input("Download special schedules? [y/n]: ")
        if act == 'y':
            print("Visit www.ridepatco.org/ for more info.")
    elif status == False:
        print("No special schedules found for today.")
    print("\nStations:" + " "*12 + "ID:")
    print("__________________________")
    for stop in stopCodes:
        leftString = f"{stop} "
        rightString = f": {stopCodes.index(stop)+1}"
        spacing = " "*(18-len(stop))
        print(leftString + spacing + rightString)
    print("__________________________\n")
    source = int(input("Enter source ID: "))
    destination = int(input("Enter destination ID: "))
    beginTime = showFromTime(stopCodes[source-1])
    direction = route_id(stopCodes[source-1], stopCodes[destination-1])
    if direction == 1: print(f"Eastbound schedules from {beginTime} selected.")
    else: print(f"Westbound schedules from {beginTime} selected.")
        

