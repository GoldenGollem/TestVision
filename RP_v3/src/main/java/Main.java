import java.util.ArrayList;

import edu.wpi.first.wpilibj.networktables.*;
import edu.wpi.first.wpilibj.tables.*;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

	public class Main {
		public static void main(String[] args) {
			// Loads our OpenCV library. This MUST be included
			System.loadLibrary("opencv_java310");
			
			NetworkTable.setClientMode();
			NetworkTable.setTeam(2509);
			NetworkTable.initialize();
			
			MjpegServer GearStream = new MjpegServer("MJPEG Server", 1184);
			MjpegServer GearStreamAlt = new MjpegServer("MJPEG Server", 1185);
			MjpegServer ShootStream = new MjpegServer("MJPEG Server", 1186);
			MjpegServer ShootStreamAlt = new MjpegServer("MJPEG Server", 1187);
			
			UsbCamera GearCam = setUsbCamera(0, GearStream);
			UsbCamera ShootCam = setUsbCamera(1,ShootStream);
			GearCam.setResolution(640, 480);
			ShootCam.setResolution(640, 480);
			//Image Stream
			CvSink GearSink = new CvSink("Gear Grabber");
			CvSink ShootSink = new CvSink("Shoot Grabber");
			
			GearSink.setSource(GearCam);
			ShootSink.setSource(ShootCam);
			GearStream.setSource(GearSource);
			ShootStream.setSource(ShootSource);
			//Defined Variables
					ArrayList<MatOfPoint> G_Contours = new ArrayList<MatOfPoint>();
					ArrayList<MatOfPoint> S_Contours = new ArrayList<MatOfPoint>();
					NetworkTable table = NetworkTable.getTable("vision");
					
					CvSource GearSource = new CvSource("Gear Source",VideoMode.PixelFormat.kMJPEG,640,480,30);
					CvSource ShootSource = new CvSource("Shoot Source",VideoMode.PixelFormat.kMJPEG,640,480,30);
					//Image Constants
					Mat S_BINARY = new Mat();
					Mat	S_CLUSTERS = new Mat();
					Mat S_HEIRARCHY = new Mat();
					Mat	S_HSV = new Mat();
					Mat	S_SOURCE = new Mat();
					Mat	S_THRESH = new Mat();
					Mat G_BINARY = new Mat();
					Mat	G_CLUSTERS = new Mat();
					Mat G_HEIRARCHY = new Mat();
					Mat	G_HSV = new Mat();
					Mat	G_SOURCE = new Mat();
					Mat	G_THRESH = new Mat();
					//Stream Variables
					
					//Color Constants
					Scalar Black = new Scalar(0,0,0);
					Scalar Blue = new Scalar(255,0,0);
					Scalar Green = new Scalar(0,255,0);
					Scalar Red = new Scalar(0,0,255);
					Scalar Yellow = new Scalar(255,255,0);
					//Camera Variable
					//Undefined Variables
					Double TargetSpeed = 0;
					Double Distance = 0;
					Rect GearTarget = new Rect();
					Rect ShootTarget = new Rect();
					Thread ProcessGear = new Thread(()->{
						GearSource.putFrame(G_SOURCE);
					});
					Thread ProcessShoot = new Thread(()->{
						ShootSource.putFrame(S_SOURCE)
					});
			
			
			
			
			
			ProcessGear.start();
			ProcessShooter.start();
		}

		private static UsbCamera setUsbCamera(int cameraId, MjpegServer server) {
			// This gets the image from a USB camera 
			// Usually this will be on device 0, but there are other overloads
			// that can be used
			UsbCamera camera = new UsbCamera("CoprocessorCamera", cameraId);
			server.setSource(camera);
			return camera;
		  }
	}