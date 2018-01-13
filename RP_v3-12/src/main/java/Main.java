import java.util.ArrayList;

import edu.wpi.first.wpilibj.networktables.*;
import edu.wpi.first.wpilibj.tables.*;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class Main {
  public static void main(String[] args) {
	// Loads our OpenCV library. This MUST be included
	System.loadLibrary("opencv_java310");
	NetworkTable.setClientMode();
	NetworkTable.setTeam(2509);
	NetworkTable.initialize();
	
	MjpegServer GearStream = new MjpegServer("Gear Server", 1184);		
	MjpegServer ShootStream = new MjpegServer("Shoot Server", 1186);

	/***********************************************/

	UsbCamera GearCam = setUsbCamera(0, GearStream);
	GearCam.setResolution(640, 480);
	UsbCamera ShootCam = setUsbCamera(1,ShootStream);
	ShootCam.setResolution(640, 480);
	CvSink GearSink = new CvSink("Gear Grabber");
	GearSink.setSource(GearCam);
	CvSink ShootSink = new CvSink("Shoot Grabber");
	ShootSink.setSource(ShootCam);
	// This creates a CvSource to use. This will take in a Mat image that has had OpenCV operations
	// operations
	CvSource GearSource = new CvSource("Gear Source",VideoMode.PixelFormat.kMJPEG,640,480,30);
	MjpegServer GearStreamAlt = new MjpegServer("Alt Gear Server", 1185);
	GearStreamAlt.setSource(GearSource);
	CvSource ShootSource = new CvSource("Shoot Source",VideoMode.PixelFormat.kMJPEG,640,480,30);
	MjpegServer ShootStreamAlt = new MjpegServer("MJPEG Server", 1187);
	ShootStreamAlt.setSource(ShootSource);
	// All Mats and Lists should be stored outside the loop to avoid allocations
	// as they are expensive to create
	Mat G_BINARY = new Mat();
	Mat	G_CLUSTERS = new Mat();
	Mat G_HEIRARCHY = new Mat();
	Mat	G_HSV = new Mat();
	Mat	G_SOURCE = new Mat();
	Mat	G_THRESH = new Mat();
	Mat S_BINARY = new Mat();
	Mat	S_CLUSTERS = new Mat();
	Mat S_HEIRARCHY = new Mat();
	Mat	S_HSV = new Mat();
	Mat	S_SOURCE = new Mat();
	Mat	S_THRESH = new Mat();
	//Color Constants
	Scalar Black = new Scalar(0,0,0);
	Scalar Blue = new Scalar(255,0,0);
	Scalar Green = new Scalar(0,255,0);
	Scalar Red = new Scalar(0,0,255);
	Scalar Yellow = new Scalar(255,255,0);				

	// Infinitely process image
	while (true) {
	  // Grab a frame. If it has a frame time of 0, there was an error.
	  // Just skip and continue
//    long frameTime = imageSink.grabFrame(inputImage);
//    if (frameTime == 0) continue;

	  // Below is where you would do your OpenCV operations on the provided image
	  // The sample below just changes color source to HSV
		Imgproc.cvtColor(G_SOURCE, G_HSV, Imgproc.COLOR_BGR2HSV);
		Imgproc.cvtColor(S_SOURCE, S_HSV, Imgproc.COLOR_BGR2HSV);

	  // Here is where you would write a processed image that you want to restreams
	  // This will most likely be a marked up image of what the camera sees
	  // For now, we are just going to stream the HSV image
	  GearSource.putFrame(G_HSV);
	  ShootSource.putFrame(S_HSV);
	}
  }
  
  private static HttpCamera setHttpCamera(String cameraName, MjpegServer server) {
	// Start by grabbing the camera from NetworkTables
	NetworkTable publishingTable = NetworkTable.getTable("CameraPublisher");
	// Wait for robot to connect. Allow this to be attempted indefinitely
	while (true) {
	  try {
		if (publishingTable.getSubTables().size() > 0) {
		  break;
		}
		Thread.sleep(500);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	HttpCamera camera = null;
	if (!publishingTable.containsSubTable(cameraName)) {
	  return null;
	}
	ITable cameraTable = publishingTable.getSubTable(cameraName);
	String[] urls = cameraTable.getStringArray("streams", null);
	if (urls == null) {
	  return null;
	}
	ArrayList<String> fixedUrls = new ArrayList<String>();
	for (String url : urls) {
	  if (url.startsWith("mjpg")) {
		fixedUrls.add(url.split(":", 2)[1]);
	  }
	}
	camera = new HttpCamera("CoprocessorCamera", fixedUrls.toArray(new String[0]));
	server.setSource(camera);
	return camera;
  }

  	private static UsbCamera setUsbCamera(int cameraId, MjpegServer server) {
	  	// This gets the image from a USB camera 
	  	// Usually this will be on device 0, but there are other overloads
	  	// that can be used
	  	UsbCamera camera = new UsbCamera("CoprocessorCamera"+cameraId, cameraId);
		server.setSource(camera);
		return camera;
  	}
}