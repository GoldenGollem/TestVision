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
		private MjpegServer GearStream = new MjpegServer("MJPEG Server", 1184);
		private	MjpegServer GearStreamAlt = new MjpegServer("MJPEG Server", 1185);
		private	MjpegServer ShootStream = new MjpegServer("MJPEG Server", 1186);
		private	MjpegServer ShootStreamAlt = new MjpegServer("MJPEG Server", 1187);
		//USB Camera
		private	UsbCamera GearCam = setUsbCamera(0, GearStream);
		private	UsbCamera ShootCam = setUsbCamera(1,ShootStream);
		//Image Stream
		private	CvSink GearSink = new CvSink("Gear Grabber");
		private	CvSink ShootSink = new CvSink("Shoot Grabber");
		//Defined Variables
		private	ArrayList<MatOfPoint> G_Contours = new ArrayList<MatOfPoint>();
		private	ArrayList<MatOfPoint> S_Contours = new ArrayList<MatOfPoint>();
		private	NetworkTable table = NetworkTable.getTable("vision");
					
		private	CvSource GearSource = new CvSource("Gear Source",VideoMode.PixelFormat.kMJPEG,640,480,30);
		private	CvSource ShootSource = new CvSource("Shoot Source",VideoMode.PixelFormat.kMJPEG,640,480,30);
		//Image Constants
		private	Mat S_BINARY = new Mat();
		private	Mat	S_CLUSTERS = new Mat();
		private	Mat S_HEIRARCHY = new Mat();
		private	Mat	S_HSV = new Mat();
		private	Mat	S_SOURCE = new Mat();
		private	Mat	S_THRESH = new Mat();
		private	Mat G_BINARY = new Mat();
		private	Mat	G_CLUSTERS = new Mat();
		private	Mat G_HEIRARCHY = new Mat();
		private	Mat	G_HSV = new Mat();
		private	Mat	G_SOURCE = new Mat();
		private	Mat	G_THRESH = new Mat();

		//Color Constants
		private	Scalar Black = new Scalar(0,0,0);
		private	Scalar Blue = new Scalar(255,0,0);
		private	Scalar Green = new Scalar(0,255,0);
		private	Scalar Red = new Scalar(0,0,255);
		private	Scalar Yellow = new Scalar(255,255,0);
		//Camera Variable
		//Undefined Variables
		private	Double TargetSpeed = 0;
		private	Double Distance = 0;
		private	Rect GearTarget = new Rect();
		private	Rect ShootTarget = new Rect();
		
		public static void main(String[] args) {
			// Loads our OpenCV library. This MUST be included
			System.loadLibrary("opencv_java310");
			
			NetworkTable.setClientMode();
			NetworkTable.setTeam(2509);
			NetworkTable.initialize();
			GearCam.setResolution(640, 480);
			ShootCam.setResolution(640, 480);
			GearSink.setSource(GearCam);
			ShootSink.setSource(ShootCam);
			GearStream.setSource(GearSource);
			ShootStream.setSource(ShootSource);
			
			ProcessGear.start();
			ProcessShooter.start();
		}

		private Thread ProcessGear = new Thread(()->{
			while(true){
				GearSource.putFrame(G_SOURCE);
			}
		});
		
		private Thread ProcessShoot = new Thread(()->{
			while(true){
				ShootSource.putFrame(S_SOURCE);
			}
		});

		private static UsbCamera setUsbCamera(int cameraId, MjpegServer server) {
			// This gets the image from a USB camera 
			// Usually this will be on device 0, but there are other overloads
			// that can be used
			UsbCamera camera = new UsbCamera("CoprocessorCamera", cameraId);
			server.setSource(camera);
			return camera;
		  }
	}