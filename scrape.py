"""
Author: David Pape Jr
Summary: A simple web scraping script for the patco transit organization's schedules page

"""
from urllib.request import urlopen
from datetime import date
import re

url = "http://www.ridepatco.org/schedules/schedules.asp"
page = urlopen(url)
html_bytes = page.read()
html = html_bytes.decode("utf-8")

today = date.today()

srch_date = f", {today.month}/{today.day}"

finding = re.findall(srch_date, html)

if len(finding) > 0:
    print(f"{len(finding)} special schedules found for today.")
    act = input("Downoad special schedules? [y/n]: ")
    if act == 'y':
        print("Visit www.ridepatco.org/ for more info.")
else:
    print("No special schedules found for today.")
    act = input("Download schedule? [y/n]: ")
    if act == 'y':
        print("Visit www.ridepatco.org/ for more info.") 

