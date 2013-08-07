/*
    GPSJake - a J2ME app which allows a user to display their position
	on an Ordnance Survey map image and provides various navigation functionality.
    Copyright (C) 2013  Mike Glynn www.gt140.co.uk

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
	
*/

/*
 * author: mglynn
 *
 * classname: CoordSys
 *
 * desc: CoordSys performs WGS84 - OS Grid and reverse calulation.
 * Implements trig functions that are not available in J2ME e.g. arctan.
 * Uses public domain code for this.  See comments below.
 * WGS84 - OS Grid conversion formula based on documents on Ordnance Survey website.
 */

package geo;

public class CoordSys {
    
    private static final double EARTH_RADIUS = 6371000;
    
    private static final double WGS84_SEMI_MAJOR = 6378137;
    private static final double WGS84_SEMI_MINOR = 6356752.3141;
    private static final double WGS84_ECCENTRICITY_SQ =
            ((WGS84_SEMI_MAJOR * WGS84_SEMI_MAJOR) - (WGS84_SEMI_MINOR * WGS84_SEMI_MINOR)) /
            (WGS84_SEMI_MAJOR * WGS84_SEMI_MAJOR);
    
    private static final double AIRY1830_SEMI_MAJOR = 6377563.396;
    private static final double AIRY1830_SEMI_MINOR = 6356256.910;
    private static final double AIRY1830_ECCENTRICITY_SQ =
            ((AIRY1830_SEMI_MAJOR * AIRY1830_SEMI_MAJOR) - (AIRY1830_SEMI_MINOR * AIRY1830_SEMI_MINOR)) /
            (AIRY1830_SEMI_MAJOR * AIRY1830_SEMI_MAJOR);
    
    //WGS84 to Airy 1830 helmert transformation parameters
    //Rotation angles in degrees (will need converting to radians before use)
    private static final double WGS84_Airy1830Rx = -0.00004172222;
    private static final double WGS84_Airy1830Ry = -0.00006861111;
    private static final double WGS84_Airy1830Rz = -0.00023391666;  
    //Translation paramters in metres
    private static final double WGS84_Airy1830Tx = -446.448;
    private static final double WGS84_Airy1830Ty = 125.157;
    private static final double WGS84_Airy1830Tz = -542.060;
    //Scale factor - sometimes stated in parts per million but in this case.
    //When in ppm the s will need to be divided by 1 million before use.
    private static final double WGS84_Airy1830s = 0.0000204894;
    
    //Airy 1830 helmert transformation parameters
    //The inverse of the above.
    //Rotation angles in degrees (will need converting to radians before use)
    private static final double Airy1830_WGS84Rx = 0.00004172222;
    private static final double Airy1830_WGS84Ry = 0.00006861111;
    private static final double Airy1830_WGS84Rz = 0.00023391666;  
    //Translation paramters in metres
    private static final double Airy1830_WGS84Tx = 446.448;
    private static final double Airy1830_WGS84Ty = -125.157;
    private static final double Airy1830_WGS84Tz = 542.060;
    //Scale factor - sometimes stated in parts per million but in this case.
    //When in ppm the s will need to be divided by 1 million before use.
    private static final double Airy1830_WGS84s = -0.0000204894;    
    
    private static final double OSGB_ORIGIN_E = 400000;
    private static final double OSGB_ORIGIN_N = -100000;
    private static final double OSGB_SCALE_FACTOR = 0.9996012717;
    private static final double OSGB_ORIGIN_LAT = 49;
    private static final double OSGB_ORIGIN_LNG = -2;
    
    // Constants for arctan calculation - taken from
    // http://discussion.forum.nokia.com/forum/showthread.php?t=72840
    // posted by Richard Carless.  Public domain code.
    static final double sq2p1 = 2.414213562373095048802e0;
    static final double sq2m1  = .414213562373095048802e0;
    static final double p4  = .161536412982230228262e2;
    static final double p3  = .26842548195503973794141e3;
    static final double p2  = .11530293515404850115428136e4;
    static final double p1  = .178040631643319697105464587e4;
    static final double p0  = .89678597403663861959987488e3;
    static final double q4  = .5895697050844462222791e2;
    static final double q3  = .536265374031215315104235e3;
    static final double q2  = .16667838148816337184521798e4;
    static final double q1  = .207933497444540981287275926e4;
    static final double q0  = .89678597403663861962481162e3;
    static final double PIO2 = 1.5707963267948966135E0;
    static final double nan = (0.0/0.0);
    
    /** Creates a new instance of CoordSys */
    public CoordSys() {
    }
    
    public LatLong getWGS84LatLong(double eastings, double northings) {
        double h = 0;  //OS orthometric height - might do something with this.....
        double n;
        double v;
        double rho;
        double eta_sq;
        double M;
        double p;        
        double VII, VIII, IX, X, XI, XII, XIIA;
        double rx_rad, ry_rad, rz_rad;        
        double airy1830_phi, airy1830_lamda;
        double wgs84_phi, wgs84_new_phi, wgs84_lamda;        
        double osgb_origin_phi, osgb_origin_lamda;
        double wgs84_x;
        double wgs84_y;
        double wgs84_z;
        double airy1830_x;
        double airy1830_y;
        double airy1830_z;
        double d_phi;        
        
        LatLong latLongWGS84 = new LatLong();
        
        //Convert OSGB36 origin latitude and longitude to radians.
        osgb_origin_phi = Math.toRadians(OSGB_ORIGIN_LAT);
        osgb_origin_lamda = Math.toRadians(OSGB_ORIGIN_LNG);
        
        airy1830_phi = ((northings - OSGB_ORIGIN_N) / (AIRY1830_SEMI_MAJOR * 0.9996012717))
        + osgb_origin_phi;
        
        n = (AIRY1830_SEMI_MAJOR - AIRY1830_SEMI_MINOR) / (AIRY1830_SEMI_MAJOR + AIRY1830_SEMI_MINOR);
        
        M = AIRY1830_SEMI_MINOR * OSGB_SCALE_FACTOR *
                (
                (1 + n + (5.0/4.0 * pow(n, 2)) + (5.0/4.0 * pow(n, 3))) * (airy1830_phi - osgb_origin_phi) -
                ((3 * n) + (3 * pow(n, 2)) + (21.0/8.0 * pow(n, 3))) * (Math.sin(airy1830_phi - osgb_origin_phi) * Math.cos(airy1830_phi + osgb_origin_phi)) +
                ((15.0/8.0 * pow(n, 2)) + (15.0/8.0 * pow(n, 3))) * (Math.sin(2 * (airy1830_phi - osgb_origin_phi)) * Math.cos(2 * (airy1830_phi + osgb_origin_phi))) -
                (35.0/24.0 * pow(n, 3)) * (Math.sin(3 * (airy1830_phi - osgb_origin_phi)) * Math.cos(3 * (airy1830_phi + osgb_origin_phi)))
                );
        
        //Iterate 10 times - should iterate until: northings - OSGB_ORIGIN_N - M < 0.01mm.
        for(int i = 0; i < 10; i++) {
            airy1830_phi = ((northings - OSGB_ORIGIN_N - M) / (AIRY1830_SEMI_MAJOR * 0.9996012717))
            + airy1830_phi;
            M = AIRY1830_SEMI_MINOR * OSGB_SCALE_FACTOR *
                    (
                    (1 + n + (5.0/4.0 * pow(n, 2)) + (5.0/4.0 * pow(n, 3))) * (airy1830_phi - osgb_origin_phi) -
                    ((3 * n) + (3 * pow(n, 2)) + (21.0/8.0 * pow(n, 3))) * (Math.sin(airy1830_phi - osgb_origin_phi) * Math.cos(airy1830_phi + osgb_origin_phi)) +
                    ((15.0/8.0 * pow(n, 2)) + (15.0/8.0 * pow(n, 3))) * (Math.sin(2 * (airy1830_phi - osgb_origin_phi)) * Math.cos(2 * (airy1830_phi + osgb_origin_phi))) -
                    (35.0/24.0 * pow(n, 3)) * (Math.sin(3 * (airy1830_phi - osgb_origin_phi)) * Math.cos(3 * (airy1830_phi + osgb_origin_phi)))
                    );
        }
        
        v = AIRY1830_SEMI_MAJOR * OSGB_SCALE_FACTOR *
                pow_neg_pt_five(1 - AIRY1830_ECCENTRICITY_SQ * pow(Math.sin(airy1830_phi), 2));
        rho = AIRY1830_SEMI_MAJOR * OSGB_SCALE_FACTOR * (1 - AIRY1830_ECCENTRICITY_SQ) *
                pow_neg_one_pt_five(1 - AIRY1830_ECCENTRICITY_SQ * pow(Math.sin(airy1830_phi), 2));
        eta_sq = v / rho - 1;
        
        VII = Math.tan(airy1830_phi) / (2.0 * rho * v);
        
        VIII = (Math.tan(airy1830_phi) / (24.0 * rho * pow(v, 3))) *
                (5 + (3 * pow(Math.tan(airy1830_phi), 2)) + eta_sq
                - (9 * pow(Math.tan(airy1830_phi), 2) * eta_sq));
        
        IX = (Math.tan(airy1830_phi) / (720.0 * rho * pow(v, 5))) *
                (61 + (90 * pow(Math.tan(airy1830_phi), 2))
                + (45 * pow(Math.tan(airy1830_phi), 4)));
        
        X = 1 / (v * (Math.cos(airy1830_phi)));
        
        XI = (1 / (6.0 * pow(v, 3) * Math.cos(airy1830_phi))) *
                (v / rho + (2 * pow(Math.tan(airy1830_phi), 2)));
        
        XII = (1 / (120.0 * pow(v, 5) * Math.cos(airy1830_phi))) *
                (5 + (28 * pow(Math.tan(airy1830_phi), 2)) + (24 * pow(Math.tan(airy1830_phi), 4)));
        
        XIIA = (1 / (5040.0 * pow(v, 7) * Math.cos(airy1830_phi))) *
                (61 + (662 * pow(Math.tan(airy1830_phi), 2)) + (1320 * pow(Math.tan(airy1830_phi), 4))
                + (720 * pow(Math.tan(airy1830_phi), 6)));
        
        airy1830_phi = airy1830_phi - (VII * pow(eastings - OSGB_ORIGIN_E, 2))
                + (VIII * pow(eastings - OSGB_ORIGIN_E, 4))
                - (IX * pow(eastings - OSGB_ORIGIN_E, 6));
        
        airy1830_lamda = osgb_origin_lamda + X * (eastings - OSGB_ORIGIN_E)
                - (XI * pow(eastings - OSGB_ORIGIN_E, 3))
                + (XII * pow(eastings - OSGB_ORIGIN_E, 5))
                - (XIIA * pow(eastings - OSGB_ORIGIN_E, 7));
        
        v = AIRY1830_SEMI_MAJOR / (Math.sqrt(1 - (AIRY1830_ECCENTRICITY_SQ * sin_sq(airy1830_phi))));

        //Convert airy1830 latitude and longitude to cartesian coordinates.
        airy1830_x = (v + h) * Math.cos(airy1830_phi) * Math.cos(airy1830_lamda);
        airy1830_y = (v + h) * Math.cos(airy1830_phi) * Math.sin(airy1830_lamda);
        airy1830_z = (((1 - AIRY1830_ECCENTRICITY_SQ) * v) + h) * Math.sin(airy1830_phi);
        
        //Convert airy1830 to wgs84 cartesian.
        //1st convert angle measures to radians.
        rx_rad = Math.toRadians(Airy1830_WGS84Rx);
        ry_rad = Math.toRadians(Airy1830_WGS84Ry);
        rz_rad = Math.toRadians(Airy1830_WGS84Rz);
            
        wgs84_x = Airy1830_WGS84Tx + airy1830_x + (Airy1830_WGS84s * airy1830_x) - (rz_rad * airy1830_y) + (ry_rad * airy1830_z);
        wgs84_y = Airy1830_WGS84Ty + (rz_rad * airy1830_x) + airy1830_y + (Airy1830_WGS84s * airy1830_y) - (rx_rad * airy1830_y);
        wgs84_z = Airy1830_WGS84Tz - (ry_rad * airy1830_x) + (rx_rad * airy1830_y) + airy1830_z + (Airy1830_WGS84s * airy1830_z);
        
        //Convert wgs84 cartesian coordinates to ellipsoidal latitude and longitude
        wgs84_lamda = atan(wgs84_y / wgs84_x);
        p = Math.sqrt((wgs84_x * wgs84_x) + (wgs84_y * wgs84_y));
        wgs84_phi = atan(wgs84_z / (p - p * WGS84_ECCENTRICITY_SQ));

        d_phi = 1;

        //while (d_phi > 0.001) {
        //Iterate 10 times - could also examine d_phi until below a certain threshold.
        for(int i = 0; i < 10; i++) {
            v = WGS84_SEMI_MAJOR / (Math.sqrt(1 - (WGS84_ECCENTRICITY_SQ * sin_sq(wgs84_phi))));
            wgs84_new_phi = atan((wgs84_z + (WGS84_ECCENTRICITY_SQ * v * Math.sin(wgs84_phi))) / p);
            d_phi = Math.abs(wgs84_phi - wgs84_new_phi);
            wgs84_phi = wgs84_new_phi;
        }
            
        wgs84_phi = Math.toDegrees(wgs84_phi);
        wgs84_lamda = Math.toDegrees(wgs84_lamda);
        
        latLongWGS84.Latitude = wgs84_phi;
        latLongWGS84.Longitude = wgs84_lamda;
        
        return latLongWGS84;
    }
    
    public OSGridRef getOSGridRef(double wgs84_lat, double wgs84_lng) {
        
        final double h = 0;  //Ellipsoidal height - might do something with this.....
        double v;
        double wgs84_phi, wgs84_lamda;
        double rx_rad, ry_rad, rz_rad;
        double airy1830_phi, airy1830_new_phi, airy1830_lamda;
        double d_phi;
        double p;
        double n;
        double rho;
        double eta_sq;
        double I, II, III, IIIA, IV, V, VI, M;
        double osgb_origin_phi, osgb_origin_lamda;
        double wgs84_x;
        double wgs84_y;
        double wgs84_z;
        double airy1830_x;
        double airy1830_y;
        double airy1830_z;        
        
        OSGridRef osGridRef = new OSGridRef();
        
        try {
            
            wgs84_phi = Math.toRadians(wgs84_lat);
            wgs84_lamda = Math.toRadians(wgs84_lng);
            
            //Convert latitude and longitude into cartesian coordinates.
            
            v = WGS84_SEMI_MAJOR / (Math.sqrt(1 - (WGS84_ECCENTRICITY_SQ * sin_sq(wgs84_phi))));
            
            wgs84_x = (v + h) * Math.cos(wgs84_phi) * Math.cos(wgs84_lamda);
            wgs84_y = (v + h) * Math.cos(wgs84_phi) * Math.sin(wgs84_lamda);
            wgs84_z = (((1 - WGS84_ECCENTRICITY_SQ) * v) + h) * Math.sin(wgs84_phi);
            
            //Convert wgs84 to airy1830 cartesian.
            
            //1st convert angle measures to radians.
            rx_rad = Math.toRadians(WGS84_Airy1830Rx);
            ry_rad = Math.toRadians(WGS84_Airy1830Ry);
            rz_rad = Math.toRadians(WGS84_Airy1830Rz);
            
            airy1830_x = WGS84_Airy1830Tx + wgs84_x + (WGS84_Airy1830s * wgs84_x) - (rz_rad * wgs84_y) + (ry_rad * wgs84_z);
            airy1830_y = WGS84_Airy1830Ty + (rz_rad * wgs84_x) + wgs84_y + (WGS84_Airy1830s * wgs84_y) - (rx_rad * wgs84_y);
            airy1830_z = WGS84_Airy1830Tz - (ry_rad * wgs84_x) + (rx_rad * wgs84_y) + wgs84_z + (WGS84_Airy1830s * wgs84_z);
            
            //Convert airy1830 cartesian to ellipsoidal longitude and latitude.
            airy1830_lamda = atan(airy1830_y / airy1830_x);
            p = Math.sqrt((airy1830_x * airy1830_x) + (airy1830_y * airy1830_y));
            airy1830_phi = atan(airy1830_z / (p - p * AIRY1830_ECCENTRICITY_SQ));
            
            d_phi = 1;
            
            //while (d_phi > 0.001) {
            //Iterate 10 times - could also examine d_phi until below a certain threshold.
            for(int i = 0; i < 10; i++) {
                v = AIRY1830_SEMI_MAJOR / (Math.sqrt(1 - (AIRY1830_ECCENTRICITY_SQ * sin_sq(airy1830_phi))));
                airy1830_new_phi = atan((airy1830_z + (AIRY1830_ECCENTRICITY_SQ * v * Math.sin(airy1830_phi))) / p);
                d_phi = Math.abs(airy1830_phi - airy1830_new_phi);
                airy1830_phi = airy1830_new_phi;
            }
            
            //Convert Airy 1830 lat long into grid eastings and northings
            n = (AIRY1830_SEMI_MAJOR - AIRY1830_SEMI_MINOR) / (AIRY1830_SEMI_MAJOR + AIRY1830_SEMI_MINOR);
            v = AIRY1830_SEMI_MAJOR * OSGB_SCALE_FACTOR *
                    pow_neg_pt_five(1 - AIRY1830_ECCENTRICITY_SQ * pow(Math.sin(airy1830_phi), 2));
            rho = AIRY1830_SEMI_MAJOR * OSGB_SCALE_FACTOR * (1 - AIRY1830_ECCENTRICITY_SQ) *
                    pow_neg_one_pt_five(1 - AIRY1830_ECCENTRICITY_SQ * pow(Math.sin(airy1830_phi), 2));
            eta_sq = v / rho - 1;
            
            //Convert OSGB36 origin latitude and longitude to radians.
            osgb_origin_phi = Math.toRadians(OSGB_ORIGIN_LAT);
            osgb_origin_lamda = Math.toRadians(OSGB_ORIGIN_LNG);
            
            M = AIRY1830_SEMI_MINOR * OSGB_SCALE_FACTOR *
                    (
                    (1 + n + (5.0/4.0 * pow(n, 2)) + (5.0/4.0 * pow(n, 3))) * (airy1830_phi - osgb_origin_phi) -
                    ((3 * n) + (3 * pow(n, 2)) + (21.0/8.0 * pow(n, 3))) * (Math.sin(airy1830_phi - osgb_origin_phi) * Math.cos(airy1830_phi + osgb_origin_phi)) +
                    ((15.0/8.0 * pow(n, 2)) + (15.0/8.0 * pow(n, 3))) * (Math.sin(2 * (airy1830_phi - osgb_origin_phi)) * Math.cos(2 * (airy1830_phi + osgb_origin_phi))) -
                    (35.0/24.0 * pow(n, 3)) * (Math.sin(3 * (airy1830_phi - osgb_origin_phi)) * Math.cos(3 * (airy1830_phi + osgb_origin_phi)))
                    );
            
            I = M + OSGB_ORIGIN_N;
            II = (v / 2.0) * Math.sin(airy1830_phi) * Math.cos(airy1830_phi);
            III = (v / 24.0) * Math.sin(airy1830_phi) * pow(Math.cos(airy1830_phi), 3) *
                    (5 - pow(Math.tan(airy1830_phi), 2) + (9 * eta_sq));
            IIIA = (v / 720.0) * Math.sin(airy1830_phi) * pow(Math.cos(airy1830_phi), 5) *
                    (61 - (58 * pow(Math.tan(airy1830_phi), 2)) + pow(Math.tan(airy1830_phi), 4));
            IV = v * Math.cos(airy1830_phi);
            V = (v / 6.0) * pow(Math.cos(airy1830_phi), 3) * ((v / rho) - pow(Math.tan(airy1830_phi), 2));
            VI = (v / 120.0) * pow(Math.cos(airy1830_phi), 5) *
                    (5 - (18 * pow(Math.tan(airy1830_phi), 2)) + pow(Math.tan(airy1830_phi), 4) + (14 * eta_sq) - (58 * pow(Math.tan(airy1830_phi), 2) * eta_sq));
            
            osGridRef.Eastings = OSGB_ORIGIN_E + (IV * (airy1830_lamda - osgb_origin_lamda)) + (V * pow(airy1830_lamda - osgb_origin_lamda, 3)) +
                    (VI * pow(airy1830_lamda - osgb_origin_lamda, 5));
            osGridRef.Northings = I + (II * pow(airy1830_lamda - osgb_origin_lamda, 2)) + (III * pow(airy1830_lamda - osgb_origin_lamda, 4)) +
                    (IIIA * pow(airy1830_lamda - osgb_origin_lamda, 6));
            
            return osGridRef;
            
        } catch (Exception e) {
            osGridRef.Eastings = -9999.99;
            osGridRef.Northings = -9999.99;
            return osGridRef;
        }
    }
    
    private double sin_sq(double x) {
        return Math.sin(x) * Math.sin(x);
    }
    
    private double pow(double base, int exponent) {
        double result = base;
        for (int i = 1; i < exponent; i++) {
            result = result * base;
        }
        return result;
    }
    
    private double pow_neg_one_pt_five(double base) {
        return(1.0 / (base * Math.sqrt(base)));
    }
    
    private double pow_neg_pt_five(double base) {
        return(1.0 / Math.sqrt(base));
    }
    
    // arctan calculation - from http://discussion.forum.nokia.com/forum/showthread.php?t=72840
    // Public domain code
    private static double mxatan(double arg) {
        double argsq, value;
        
        argsq = arg*arg;
        value = ((((p4*argsq + p3)*argsq + p2)*argsq + p1)*argsq + p0);
        value = value/(((((argsq + q4)*argsq + q3)*argsq + q2)*argsq + q1)*argsq + q0);
        return value*arg;
    }
    
    // reduce
    private static double msatan(double arg) {
        if(arg < sq2m1)
            return mxatan(arg);
        if(arg > sq2p1)
            return PIO2 - mxatan(1/arg);
        return PIO2/2 + mxatan((arg-1)/(arg+1));
    }
    
    // implementation of atan
    private static double atan(double arg) {
        if(arg > 0)
            return msatan(arg);
        return -msatan(-arg);
    }
    
    // implementation of asin
    private static double asin(double arg)
    {
        double temp;
        int sign;

        sign = 0;
        if(arg < 0)
        {
            arg = -arg;
            sign++;
        }
        if(arg > 1)
            return nan;
        temp = Math.sqrt(1 - arg*arg);
        if(arg > 0.7)
            temp = PIO2 - atan(temp/arg);
        else
            temp = atan(arg/temp);
        if(sign > 0)
            temp = -temp;
        return temp;
    }

    // implementation of acos
    private static double acos(double arg)
    {
        if(arg > 1 || arg < -1)
            return nan;
        return PIO2 - asin(arg);
    }
    
}
