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

def isInternetConnected():
    """ Function which utilizes urllib and a default ping address to check for
        a valid internet connection.
    """
    try:
        response = urlopen('http://www.ridepatco.org/schedules/schedules.asp', timeout=1)
        return True
    except:
        return False

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

    finding1 = re.findall(srch_date, html)
    finding2 = re.findall(f"({today.month}/{today.day})", html)

    if len(finding1) > 0 or len(finding2) > 0:
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
    now, converted = currentTime(), []
    for part in now:
        part = str(part)
        if len(part) == 1:
            part = "0"+part
        converted.append(part)
    return f"{converted[0]}:{converted[1]}:00"

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
    if weekday <= 4:
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
    trip_ids, stopID = trip_id(source, destination), stop_id(source)
    arrivalTime = showFromTime(source)

    # open stop_times dataset and search for arrival times
    temp, allTimes, result = [], [], []
    append = False
    f = open("stop_times.txt", "r")
    line = f.readline()
    while line != '':
        for i in trip_ids:
            if line[:4] == i:
                temp.append(line)
        line = f.readline()
    f.close()

    # extract arrival time from strings
    for i in temp:
        if i[23:25] == str(stopID) or i[23] == str(stopID):
            allTimes.append(i[5:13])
    # extract arrival times beginning at current time from allTimes
    for i in allTimes:
        if int(i[:2]) >= int(arrivalTime[:2]): # check for next train
            if int(i[3:5]) >= int(arrivalTime[3:5]):
                append = True
        if append: # append from next train onward
            result.append(i)
    return result


###   DIRECT CALL   ###
if __name__ == '__main__':
    doWhat = input("Run default program? [y/n]: ")
    if doWhat == "y":
        internet = isInternetConnected()
        if internet:
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
            if direction == 1:
                print(f"Eastbound schedule from {beginTime} selected.")
                print("__________________________")
            else:
                print(f"Westbound schedule from {beginTime} selected.")
                print("__________________________")
            allSchedules = listSchedules(stopCodes[source-1], stopCodes[destination-1])
            print(f"Arrival times".center(26))
            for i in allSchedules:
                print(i)
            print("__________________________")
        else:
            print("ERROR: No Internet Connection!")
    

