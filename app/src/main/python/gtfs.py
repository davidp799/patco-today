"""
Author: David Pape Jr
Summary: A simple web scraping script for the patco transit organization's schedules page

"""
from datetime import datetime
from datetime import date
from io import BytesIO
from urllib.request import urlopen
from zipfile import ZipFile
from os.path import dirname, join
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
    
class Schedules:
    """ Class which utilizes urllib and ZipFile to download the latest
        PortAuthorityTransitCorporation (PATCO) GTFS package.
        Returns: ZipFile object
    """
    def __init__(self, source_id, destination_id):
        # get data from source
        self.resp = urlopen("http://www.ridepatco.org/developers/PortAuthorityTransitCorporation.zip")
        self.zipfile = ZipFile(BytesIO(self.resp.read()))
        # get weekday index
        self.weekday = datetime.today().weekday()
        # initialize variables
        self.src, self.dsn = stopCodes[source_id-1], stopCodes[destination_id-1]
        self.stop_id = stopCodes.index(self.src)+1
        self.src_id = source_id
        self.dsn_id = destination_id
        # determine route_id
        if self.dsn_id < self.src_id: self.route_id = 1
        else: self.route_id = 2
        
    def time(self):
        """ Function which utilizes urllib to determine if a special schedule is
            present for the current date. """
        now = datetime.now().time()
        hour, minute, second = now.hour, now.minute, now.second
        return [hour, minute, second]

    def startTime(self):
        """ Function which uses the current time to determine where to begin
            fetching train arrival times.
            Returns: string object
        """
        stop_id = stopCodes.index(self.src)+1
        now, converted = self.time(), []
        for part in now:
            part = str(part)
            if len(part) == 1:
                part = "0"+part
            converted.append(part)
        return f"{converted[0]}:{converted[1]}:00"

    def service_id(self):
        """ Function which utilizes currentWkday() function to determine the service_id
            based on day of week.
            Returns: int object
        """
        stdIdx, dateCode, storage = 0, '', []
        filename = join(dirname(__file__), "calendar.txt")
        # open calendar data and find service_ids
        f = open(filename, "r")
        line = f.readline()
        while line != '':
            stdIdx = line.index(',')+1
            dateCode = line[stdIdx:stdIdx+13] # date code
            if self.weekday <= 4 and dateCode == '1,1,1,1,1,0,0':
                storage.append(int(line[:stdIdx-1]))
            elif self.weekday == 5 and dateCode == '0,0,0,0,0,1,0':
                storage.append(int(line[:stdIdx-1]))
            elif self.weekday == 6 and dateCode == '0,0,0,0,0,0,1':
                storage.append(int(line[:stdIdx-1]))
            line = f.readline()
        f.close()
        return storage
            
    def trip_id(self):
        """ Function which determines the trip_id based on the given route_id and service_id
            by reading the trips data file.
            Returns: list object
        """
        # initialize variables
        serviceIDs = self.service_id()
        trips = []
        filename = join(dirname(__file__), "trips.txt")

        # open trips.txt file as read-only data
        f = open(filename, "r")

        # read data line-by-line and append relevant data to a list
        line = f.readline()
        while line != '':
            for i in serviceIDs:
                if line[:5] == f"{self.route_id},{i},":
                    splitLine = line.split(',')
                    trips.append(splitLine[2])
                line = f.readline()
        f.close()
        return trips

    def listSchedules(self):
        """ Function which utilizes urllib to determine if a special schedule is
            present for the current date. """
        # initialize variables
        trip_ids = self.trip_id()
        arrivalTime = self.startTime()

        # open stop_times dataset and search for arrival times
        temp, allTimes, result = [], [], []
        append = False
        filename = join(dirname(__file__), "stop_times.txt")
        f = open(filename, "r")
        line = f.readline()
        while line != '':
            for i in trip_ids:
                if line[:4] == i:
                    temp.append(line)
            line = f.readline()
        f.close()

        # extract arrival time from strings
        for i in temp:
            if i[23:25] == str(self.stop_id) or i[23] == str(self.stop_id):
                allTimes.append(i[5:13])
        # extract arrival times beginning at current time from allTimes
        for i in allTimes:
            if int(i[:2]) >= int(arrivalTime[:2]): # check for next train
                if int(i[3:5]) >= int(arrivalTime[3:5]):
                    append = True
            if append: # append from next train onward
                result.append(i)
        return result

    def sortedSchedules(self):
        """ Function which sorts list of trip by calling trip_id() and utilizes
            RADIX sort method to sort arrival times in ascending order.
        """
        return sorted(self.listSchedules())


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
            s = Schedules(source, destination)
            
            beginTime = s.startTime()
            direction = s.route_id
            if direction == 1:
                print(f"Eastbound schedule from {beginTime} selected.")
                print("__________________________")
            else:
                print(f"Westbound schedule from {beginTime} selected.")
                print("__________________________")
            allSchedules = s.sortedSchedules()
            print(f"Arrival times".center(26))
            for i in allSchedules:
                print(i)
            print("__________________________")
        else:
            print("ERROR: No Internet Connection!")
    
