"""
AUTHOR: David Pape
GTFS Reader Script for PATCO Transit API
"""
import gtfs_kit as gk # import the gtfs-kit module
import os
from pathlib import Path

# Declare directory path for GTFS zip file
path = Path(f"{os.getcwd()}/PortAuthorityTransitCorporation.zip")

# Read the feed with gtfs-kit
feed = (gk.read_feed(path, dist_units="mi"))

# Search for errors and warnings in the feed
feed.validate()
