import java.util.ArrayList;

import edu.wpi.first.wpilibj.networktables.*;
import edu.wpi.first.wpilibj.tables.*;
import edu.wpi.cscore.*;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

	public class Main {
		public static void main(String[] args) {
			// Loads our OpenCV library. This MUST be included
			System.loadLibrary("opencv_java310");
			
					//Defined Variables
					ArrayList<MatOfPoint> G_Contours = new ArrayList<MatOfPoint>();
					ArrayList<MatOfPoint> S_Contours = new ArrayList<MatOfPoint>();
					NetworkTable table = NetworkTable.getTable("vision");
					//Image Stream
					CvSink GearSink = new CvSink("Gear Grabber");
					CvSink ShootSink = new CvSink("Shoot Grabber");
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
					MjpegServer GearStream = new MjpegServer("MJPEG Server", 1184);
					MjpegServer GearStreamAlt = new MjpegServer("MJPEG Server", 1185);
					MjpegServer ShootStream = new MjpegServer("MJPEG Server", 1186);
					MjpegServer ShootStreamAlt = new MjpegServer("MJPEG Server", 1187);
					//Color Constants
					Scalar Black = new Scalar(0,0,0);
					Scalar Blue = new Scalar(255,0,0);
					Scalar Green = new Scalar(0,255,0);
					Scalar Red = new Scalar(0,0,255);
					Scalar Yellow = new Scalar(255,255,0);
					//Camera Variable
					UsbCamera GearCam = setUsbCamera(0, GearStream);
					UsbCamera ShootCam = setUsbCamera(1,ShootStream);
					//Undefined Variables
					Double TargetSpeed = 0;
					Double Distance = 0;
					Rect GearTarget = new Rect();
					Rect ShootTarget = new Rect();
					Thread ProcessGear = new Thread(()->{
						while(true){
							G_Contours.clear();
							GearSink.grabFrame(S_SOURCE);
							Imgproc.cvtColor(G_SOURCE, G_HSV, Imgproc.COLOR_BGR2RGB);
							Imgproc.threshold(G_HSV, G_BINARY, 180, 190, Imgproc.THRESH_BINARY_INV);	
							Imgproc.cvtColor(G_BINARY, G_THRESH, Imgproc.COLOR_HSV2BGR);
							Imgproc.cvtColor(G_THRESH, G_CLUSTERS, Imgproc.COLOR_BGR2GRAY);
							Mat G_GRAY = G_CLUSTERS;
							Imgproc.Canny(G_GRAY, G_HEIRARCHY, 2, 4);
							Imgproc.findContours(G_HEIRARCHY, G_Contours, new Mat(),Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
							for(MatOfPoint mop :S_Contours){
								Rect rec = Imgproc.boundingRect(mop);
								Imgproc.rectangle(G_SOURCE, rec.br(), rec.tl(), Red);
							}
							for(Iterator<MatOfPoint> iterator = G_Contours.iterator();iterator.hasNext();){
								MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
								Rect rec = Imgproc.boundingRect(matOfPoint);
								//float aspect = (float)rec.width/(float)rec.height;
								if( rec.height < 20||rec.height>100){
									iterator.remove();
									continue;
								}
							}
							table.putNumber("Gear Height", ShootTarget.height);
							table.putNumber("Gear Width", ShootTarget.width);
							table.putNumber("Gear X", ShootTarget.x);
							table.putNumber("Gear Y", ShootTarget.y);
						}
					});
					Thread ProcessShoot = new Thread(()->{
						while(true){
							S_Contours.clear();
							ShootSink.grabFrame(S_SOURCE);
							Imgproc.cvtColor(S_SOURCE, S_HSV, Imgproc.COLOR_BGR2RGB);
							Imgproc.threshold(S_HSV, S_BINARY, 180, 190, Imgproc.THRESH_BINARY_INV);	
							Imgproc.cvtColor(S_BINARY, S_THRESH, Imgproc.COLOR_HSV2BGR);
							Imgproc.cvtColor(S_THRESH, S_CLUSTERS, Imgproc.COLOR_BGR2GRAY);
							Mat S_GRAY = S_CLUSTERS;
							Imgproc.Canny(S_GRAY, S_HEIRARCHY, 2, 4);
							Imgproc.findContours(S_HEIRARCHY, S_Contours, new Mat(),Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
							for(MatOfPoint mop :S_Contours){
								Rect rec = Imgproc.boundingRect(mop);
								Imgproc.rectangle(S_SOURCE, rec.br(), rec.tl(), Red);
							}
							for(Iterator<MatOfPoint> iterator = S_Contours.iterator();iterator.hasNext();){
								MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
								Rect rec = Imgproc.boundingRect(matOfPoint);
								//float aspect = (float)rec.width/(float)rec.height;
								if( rec.height < 20||rec.height>100){
									iterator.remove();
									continue;
								}
							}
							table.putNumber("Shoot Height", ShootTarget.height);
							table.putNumber("Shoot Width", ShootTarget.width);
							table.putNumber("Shoot X", ShootTarget.x);
							table.putNumber("Shoot Y", ShootTarget.y);
						}
					});
			
			
			NetworkTable.setClientMode();
			NetworkTable.setTeam(2509);
			NetworkTable.initialize();
			
			GearSink.setSource(GearCam);
			ShootSink.setSource(ShootCam);
			GearStream.setSource(GearSource);
			ShootStream.setSource(ShootSource);
			
			GearCam.setResolution(640, 480);
			ShootCam.setResolution(640, 480);
			
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