RobotOfAwesome
==============
The Robot of Awesome project was done during the course Software Engineering Project (DAT255) at Chalmers University of Technology.
The reader will notice that we actually did not only do software, but also hardware and embedded firmware.

##Basic structure

The project in its whole is divided into three parts:
* The android app, as presented in this repository.
* Embedded firmware, available in the [atnon/RoAC-Firmware](https://github.com/atnon/RoAC-Firmware) repository
* Hardware, schematics available at https://db.tt/oud67eU9

Below follows a diagram describes the basic strucure of the app and the purpose of the different files.
```
 Robot of Awesome App                                                               
+--------------------------------------------------------------+                    
|                                                              |                    
|      +-----------------+     +-----------------------------+ |  +----------------+
|      | User Interface  +----->          Bluetooth          | |  |  Other phone   |
|      | Interfacet.java |     | TheBluetoothConnection.java +---->  with same app |
|      +--------+--------+     |   DeviceListActivity.java   | |  |                |
|               |              +-----------------------------+ |  +----------------+
|               |                                              |                    
|    +----------v----------+                                   |                    
|    |  MotorControl.java  |                                   |
|    +----------+----------+                                   |                    
|               |                                              |                    
|               |                                              |                    
|     +---------v---------+                                    |                    
|     | USB Communication |                                    |                    
|     |  Sendstring.java  |                                    |                    
|     +---------+---------+                                    |                    
|               |                                              |                    
+---------------|----------------------------------------------+                    
                |                                                                   
      +---------v---------+                                                         
      |  Robot firmware   |                                                         
      |atnon/RoAC-Firmware|                                                         
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
