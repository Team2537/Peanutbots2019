package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Encoder;

public class Hazelnut2 extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  private TalonSRX leftTalon, rightTalon;
  private DifferentialDrive drive;
  private Ultrasonic frontUltrasonic, rearUltrasonic;
  private XboxController xbox;
  private Timer timer;
  private Encoder leftEnc, rightEnc;
  private Boolean startPressed; 

  private int stopLeft, stopRight;
  private double leftSpeed, rightSpeed;


  public class driveCommand extends Thread {
    
    public void run() {
      driveS(4000);
      driveRight();
      driveS(5000);
      driveRight(); 
      driveS(4000);
      driveRight();
      driveS(5000);
      driveRight();
      startPressed = false; 
    }
  }
  @Override
  public void robotInit() {
    System.out.println("Starting Brandi's code");
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    leftTalon = new TalonSRX(0);
    rightTalon = new TalonSRX(1);
    //drive = new DifferentialDrive(leftTalon, rightTalon);

    frontUltrasonic = new Ultrasonic(8, 9);

    rearUltrasonic = new Ultrasonic(4, 5);
    rearUltrasonic.setAutomaticMode(true);

    xbox = new XboxController(0);

    CameraServer.getInstance().startAutomaticCapture();

    timer = new Timer();

    leftEnc = new Encoder(0, 1);
    rightEnc = new Encoder(2, 3, true);
    leftEnc.reset();
    rightEnc.reset();

    startPressed = false;
  }

  @Override
  public void robotPeriodic() {
  }

  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    timer.reset();
    timer.start();
  }

  @Override
  public void autonomousPeriodic() {
    if (timer.get() < 2.0) {
      drive.curvatureDrive(0.1, 0.0, false);
    } else {
      drive.curvatureDrive(0.0, 0.0, false);
    }

    switch (m_autoSelected) {
    case kCustomAuto:

      break;
    case kDefaultAuto:
    default:

      break;
    }
  }

  @Override
  public void teleopPeriodic() {
    if (xbox.getStartButtonPressed()) {
      startPressed = true; 
      driveCommand d = new driveCommand();
      d.start();   
    } else if (xbox.getYButtonPressed()) {
      leftEnc.reset();
      rightEnc.reset();

      leftSpeed = 0.45;
      rightSpeed = 0.5;

      stopLeft = 1000;
      stopRight = 1000;
    }

    else if (xbox.getBButtonPressed()) {
      leftEnc.reset();
      rightEnc.reset();

      leftSpeed = 0.45;
      rightSpeed = -0.5;

      stopLeft = 650;
      stopRight = 650;
    }

    else if (xbox.getAButtonPressed()) {
      leftEnc.reset();
      rightEnc.reset();

      leftSpeed = -0.45;
      rightSpeed = -0.5;

      stopLeft = 1000; 
      stopRight = 1000; 

    } else if (xbox.getXButtonPressed()) {

      leftEnc.reset();
      rightEnc.reset();

      leftSpeed = -0.45;
      rightSpeed = 0.5;

      stopLeft = 600;
      stopRight = 600;
    }

    if (stopLeft == 0 && stopRight == 0) {
      joystickDrive();
    } else if (startPressed == false) {
      driveEncoder(stopLeft, stopRight, leftSpeed, rightSpeed);
    }
  }

  public void joystickDrive() {
    leftSpeed = -0.5 * xbox.getY(Hand.kLeft);
    rightSpeed = -0.5 * xbox.getY(Hand.kRight);

    if (safetyStop(15.0, frontUltrasonic) && (leftSpeed > 0.0) && (rightSpeed > 0.0)) {
      // System.out.println("front stop");
      drive.stopMotor();
    } else if (safetyStop(15.0, rearUltrasonic) && (leftSpeed < 0.0) && (rightSpeed < 0.0)) {
      drive.stopMotor();
      // System.out.println("back stop");
    } else {
      drive.tankDrive(leftSpeed, rightSpeed);
    }
  }

  @Override
  public void testPeriodic() {
  }

  @Override
  public void testInit() {
    
  }

  public boolean safetyStop(double safeDistance, Ultrasonic sensor) {

    if (sensor == null)
      return false;
    double distance = sensor.getRangeInches();
    // System.out.println("Distance = " + distance);

    if (distance < safeDistance)
      return true;
    else
      return false;
  }

  public void driveS(int distance){
    leftEnc.reset();
    rightEnc.reset();

    leftSpeed = 0.5;
    rightSpeed = 0.5;
    driveEncoderInThread(distance, distance, leftSpeed, rightSpeed);
  }
  public void driveRight(){
    leftEnc.reset();
    rightEnc.reset();

    leftSpeed = 0.5;
    rightSpeed = -0.5;
    driveEncoderInThread(250, -250, leftSpeed, rightSpeed);
  }



  void driveEncoderInThread(int leftDistance, int rightDistance, double leftSpeed, double rightSpeed) {
    while ((Math.abs(leftEnc.getRaw()) < Math.abs(leftDistance))
        || (Math.abs(rightEnc.getRaw()) < Math.abs(rightDistance))) {
      //System.out.println("left" + leftEnc.getRaw() + "right" + rightEnc.getRaw());
   //   if (Math.abs(leftEnc.getRaw()) >= Math.abs(leftDistance)) {
   //     drive.tankDrive(0.0, rightSpeed);
   //   } else if (Math.abs(rightEnc.getRaw()) >= Math.abs(rightDistance)) {
   //     drive.tankDrive(leftSpeed, 0.0);
   //   } else {
        drive.tankDrive(leftSpeed, rightSpeed);
   //   }
    }
      drive.tankDrive(0.0, 0.0);
      this.stopLeft = 0;
      this.stopRight = 0;

  }
  void driveEncoder(int leftDistance, int rightDistance, double leftSpeed, double rightSpeed) {
    if ((Math.abs(leftEnc.getRaw()) < Math.abs(leftDistance))
        || (Math.abs(rightEnc.getRaw()) < Math.abs(rightDistance))) {
    //  System.out.println("left" + leftEnc.getRaw() + "right" + rightEnc.getRaw());
   //   if (Math.abs(leftEnc.getRaw()) >= Math.abs(leftDistance)) {
   //     drive.tankDrive(0.0, rightSpeed);
   //   } else if (Math.abs(rightEnc.getRaw()) >= Math.abs(rightDistance)) {
   //     drive.tankDrive(leftSpeed, 0.0);
   //   } else {
        drive.tankDrive(leftSpeed, rightSpeed);
   //   }
    } else {
      drive.tankDrive(0.0, 0.0);
      this.stopLeft = 0;
      this.stopRight = 0;
    }

  }

};