import csv
import numpy as np
import matplotlib as mpl
mpl.use('Agg')
import matplotlib.pyplot as plt
from mpl_toolkits.basemap import Basemap

FILE_PATH = "earthquakes.csv"

class EarthQuake:
  def __init__(self, row):
    self.timestamp = row[0]
    self.lat = float(row[1])
    self.lon = float(row[2])
    try:
      self.magnitude = float(row[4])
    except ValueError:
      self.magnitude = 0


def load_earthquake_data():
  with open(FILE_PATH, "r") as f:
    reader = csv.reader(f, delimiter=",")
    next(reader, None) #ignore headers
    return [el for el in [EarthQuake(row) for row in reader] if el.magnitude > 0]

def build_marker(magnitude):
  markersize = magnitude * 2.5;
  if magnitude < 1.0:
    return ('bo'), markersize
  elif magnitude < 3.0:
    return ('go'), markersize
  elif magnitude < 5.0:
    return ('yo'), markersize
  else:
    return ('ro'), markersize

def main():
  data = load_earthquake_data()

  mpl.rcParams['figure.figsize'] = '16, 12'
  m = Basemap(projection='kav7', lon_0=-90, resolution = 'l', area_thresh = 1000.0)
  m.drawcoastlines()
  m.drawcountries()
  m.drawmapboundary(fill_color='0.3')
  m.drawparallels(np.arange(-90.,99.,30.))
  junk = m.drawmeridians(np.arange(-180.,180.,60.))
  
  data.sort(key=lambda q: q.magnitude, reverse=True)

  # add earthquake info to the plot
  for earthquake in data:
    x,y = m(earthquake.lon, earthquake.lat)
    mcolor, msize = build_marker(earthquake.magnitude)
    m.plot(x, y, mcolor, markersize=msize)

  # add a title
  plt.title("Earthquakes {0} to {1}".format(data[-1].timestamp[:10],
                                          data[0].timestamp[:10]))

  plt.savefig('earthquakes.png', bbox_inches='tight', dpi = 300)
  plt.clf()

if __name__ == "__main__":
  main()
