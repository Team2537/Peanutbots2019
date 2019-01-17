package frc.robot;

// Import the classes of objects you will use
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import com.ctre.phoenix.motorcontrol.can.*;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.cameraserver.CameraServer;

//import edu.wpi.first.wpilibj.livewindow.LiveWindow;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Peanut2 extends TimedRobot {
  // Create instances of each object
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private XboxController xbox;
  private Timer timer;
  private WPI_TalonSRX m_rearLeft;
  private WPI_TalonSRX m_rearRight;
  private DifferentialDrive m_drive;
  private boolean    f_safetyStop;
  private Ultrasonic f_ultrasonic;
  private Ultrasonic r_ultrasonic;


  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    // initialize the objects and connect them to their underlying hardware
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    xbox = new XboxController(0);
    timer = new Timer();
    m_rearLeft = new WPI_TalonSRX(4);
    m_rearRight = new WPI_TalonSRX(3);
    m_drive = new DifferentialDrive(m_rearLeft, m_rearRight);
    /*f_ultrasonic = new Ultrasonic(4,5); // ping, echo
    f_ultrasonic.setAutomaticMode(true);
    f_ultrasonic.setEnabled(true);
    r_ultrasonic = new Ultrasonic(6,7); // ping, echo
    r_ultrasonic.setAutomaticMode(true);
    r_ultrasonic.setEnabled(true);*/
    CameraServer.getInstance().startAutomaticCapture();
}

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    timer.reset();
    timer.start();
  }

  /*public void teleopInit() {

  }*/

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    if (timer.get() < 2.0) {
      m_drive.curvatureDrive(0.1, 0.0, false);
    }
    else
    {
      m_drive.curvatureDrive(0.0, 0.0,false);
    }
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {

    // Use front-mounted ultrasonic sensor to stop robot
    // if it gets too close to an obstacle
    double f_range = f_ultrasonic.getRangeInches();
    if (f_ultrasonic.isRangeValid()) {
       if ((f_range < 15.0) && !f_safetyStop) {
          System.out.println("Safety stopped due to front obstacle");
          f_safetyStop = true;
       } else if (f_range >= 15.0 && f_safetyStop) {
          System.out.println("Resuming...");
          f_safetyStop = false;
       }
    }

    // Use controller joysticks to set drive speed, but
    // safety stop if too close to an obstacle
    double leftSpeed  = -0.5*xbox.getY(Hand.kLeft);
    double rightSpeed = -0.5*xbox.getY(Hand.kRight);
    // If there's an obstacle in front of us, don't
    // allow any more forward motion
    if (f_safetyStop && 
        (leftSpeed > 0.0) && (rightSpeed > 0.0)) {
       m_drive.stopMotor();
    } else {
      // otherwise, set motors according to joysticks
       m_drive.tankDrive(leftSpeed, rightSpeed);
    }
    Timer.delay(0.01);
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
    // LiveWindow.run();
  }
}
