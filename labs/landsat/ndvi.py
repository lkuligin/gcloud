
import sys
import osgeo.gdal as gdal
import os
import os.path
import subprocess
import struct
import numpy as np
import tempfile

class LandsatReader():
    def __init__(self, gsdir, band, destdir):
        basename = os.path.basename(gsdir)
        self.gsfile = "{0}/{1}_{2}.TIF".format(gsdir, basename, band)
        self.dest = os.path.join(destdir, os.path.basename(self.gsfile))

    def __enter__(self):
        print 'Getting {0} to {1} '.format(self.gsfile, self.dest)
        ret = subprocess.check_call(["gsutil", "cp", self.gsfile, self.dest])
        if ret == 0:
            dataset = gdal.Open(self.dest, gdal.GA_ReadOnly)
            return dataset
        else:
            return None

    def __exit__(self, exc_type = None, exc_val = None, exc_tb = None):
        os.remove(self.dest)

def computeNdvi(gs_baseurl, outdir, instrument):
    if instrument is 'LANDSAT_7':
        band1 = 'B3'
        band2 = 'B4'
    else:
        band1 = 'B4'
        band2 = 'B5'


    with LandsatReader(gs_baseurl, band1, '.') as red_ds, LandsatReader(gs_baseurl, band2, '.') as nir_ds:
        outdtype = gdal.GDT_Float32

        tmpfilename = os.path.join(tempfile.gettempdir(), '{0}_ndvi.TIF'.format(os.path.basename(gs_baseurl)) )
        driver = gdal.GetDriverByName('GTiff')
        outds = driver.Create(tmpfilename, red_ds.RasterXSize, red_ds.RasterYSize, 1, outdtype)
        outds.SetGeoTransform(red_ds.GetGeoTransform())
        outds.SetProjection(red_ds.GetProjection())

        red = red_ds.GetRasterBand(1)
        nir = nir_ds.GetRasterBand(1)
        packformat = 'f' * red.XSize
        for line in xrange(0, red.YSize):
            red_data = struct.unpack(packformat, red.ReadRaster(0, line, red.XSize, 1, red.XSize, 1, gdal.GDT_Float32))
            nir_data = struct.unpack(packformat, nir.ReadRaster(0, line, nir.XSize, 1, nir.XSize, 1, gdal.GDT_Float32))
            ndvi_scaled = np.array([255] * red.XSize, dtype=np.float32)   # mask value

            for i in xrange(0, len(red_data)):
                ndvi_denom = nir_data[i] + red_data[i]
                ndvi_num = nir_data[i] - red_data[i]
                if ndvi_denom != 0:
                    ndvi = (ndvi_num/ndvi_denom)  # -1 to 1
                    ndvi_scaled[i] = int(0.5 + 100*ndvi) if ndvi > 0 else 0  # 0-100
            outline = struct.pack(packformat, *ndvi_scaled)
            outds.GetRasterBand(1).WriteRaster(0, line, red.XSize, 1, outline, buf_xsize=red.XSize, buf_ysize=1, buf_type=outdtype)
            del outline
        outds = None # close

        outfilename = os.path.join(outdir, '{0}_ndvi.TIF'.format(os.path.basename(gs_baseurl)) )
        ret = subprocess.check_call(['gsutil', 'mv', tmpfilename, outfilename])
        print 'Wrote {0} ...'.format(outfilename)
