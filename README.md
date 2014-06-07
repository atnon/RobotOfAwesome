RobotOfAwesome
==============
The Robot Of Awesome app is intended for makers/hackers and others interested in the fusion between hardware and Android. The end user should be able to use his/her Android device as a platform for controlling a robot remotely, connecting to the robot hardware through the use of some sort of hardware interface.

The Robot of Awesome project was done during the course Software Engineering Project (DAT255) at Chalmers University of Technology.
The reader will notice that we actually did not only do software, but also hardware and embedded firmware.

A lot of documentation such as user stories can be found in the [project wiki](https://github.com/atnon/RobotOfAwesome/wiki).

## Dependencies
* IDE supporting Android, we recommend the [ADT bundle](http://developer.android.com/sdk/index.html) provided by Google.
* Android SDK v12 or higher (this version introduces support for Android Open Accessories).
* JUnit 4 för unittesting.
* Two android phones (and the robot hardware if you want to do the fun stuff.)

##Basic structure

The project in its whole is divided into three parts:
* The android app, as presented in this repository.
* Embedded firmware, available in the [atnon/RoAC-Firmware](https://github.com/atnon/RoAC-Firmware) repository
* Hardware, schematics available at https://db.tt/oud67eU9

Below follows a diagram describes the basic strucure of the app and the purpose of the different files.
```
 Robot of Awesome App                                                               
+------------------------------------------------------------------+                    
|                                                                  |                    
|      +-----------------+     +-----------------------------+     |  +----------------+
|      | User Interface  +----->          Bluetooth          |     |  |  Other phone   |
|      | Interfacet.java |     | TheBluetoothConnection.java +-------->  with same app |
|      +--------+--------+     |   DeviceListActivity.java   |     |  |                |
|               |              +-----------------------------+     |  +----------------+
|               |                                                  |                    
|    +----------v----------+                                       |                    
|    |  MotorControl.java  |                                       |
|    +----------+----------+                                       |                    
|               |                                                  |                    
|               |                                                  |                    
|     +---------v---------+                                        |                    
|     | USB Communication |                                        |                    
|     |  Sendstring.java  |                                        |                    
|     +---------+---------+                                        |                    
|               |                                                  |     
|               | <- Sends a byte array containg a control string. |
+---------------|--------------------------------------------------+                    
                |                                                                   
      +---------v---------+                                                         
      |  Robot firmware   |                                                         
      |atnon/RoAC-Firmware|  Parses the control string and executes command.   
      |    on Github      |                                                         
      +---------+---------+                                                  
                |                                                                   
    +-----------v------------+                                                      
    |     Robot Hardware     |                                                      
    |     Schematics on      |                                                      
    | https://db.tt/oud67eU9 |                                                      
    +------------------------+                                                      

```
## 
