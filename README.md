OpenGL tutorials with LWJGL
===========================

This project is a port of McKesson's excellent OpenGL tutorials to Java using LWJGL and it is distributed in the hope that it will be useful.

[Learning Modern 3D Graphics Programming](http://www.arcsynthesis.org/gltut/index.html) is the book where to start; you can find the original code
[here](https://bitbucket.org/alfonse/gltut/wiki/Home). The author occasionally updates his book by adding new tutorials:
we will update this project in the same manner as soon as we can.

N° of original tutorials:    16  
N° of ported tutorials so far:    15

If you find any issues or difficulties in using this project, please tell us.  
Happy coding! 

Francesco & Marco



General information
-------------------
The project is developed with Java 1.7 and LWJGL 2.8.3, and may not work with older versions.  
To download and install LWJGL visit [the official site](http://www.lwjgl.org/). 

- "rosick/jglsdk" 	: package with the glsdk port.
- "rosick/mckesson" : package with the tutorials port.

To test it out, it is recommended to use [Eclipse](http://www.eclipse.org/). Open Eclipse, copy or link the "rosick" package into an existing project (in the "src" folder) and, if you haven't already done, 
add and configure the LWJGL library in the project build path. Open any tutorial and run it by clicking the 'play' button.



Porting notes
-------------
Every tutorial class inherits from the base class GLWindow: in this class the main loop is executed, which in order calculates the values of 'elapsedTime' 
and 'lastFrameDuration', call update() and display(). The function reshape() is called once before the main loop starts and every time the window get resized. 
The two variables 'elapsedTime' and 'lastFrameDuration' can be accessed by calling their respective getters; update(), display() and reshape() 
are overrided in every tutorial class. 

The code was ported in a manner as similar as possible as the original project (which is written in c++), but not all the features it uses are implemented in Java 
(eg. function pointers), so i had to make some changes. This is the major changes list:

* In the original tutorials, Glut manage keyboard inputs by calling keyboard(char c). I choose to create a general purpose method called update(), which is executed 
once per loop, and here check if the keyboard keys that the tutorial need are pressed or not. In addition, to obtain a more accurate movement (eg. the camera controlled with the keyboard) i computed the simplest 
possible integration by multiplying the deltaMovement by "lastFrameDuration". The value of lastFrameDuration obtained with getLastFrameDuration() is often multiplied by a constant (e.g 5 / 1000.0), which
is simply a convenient value to make the calculations.
* PushStack : the original author decided to implement a MatrixStack with a push/pop organization based on PushStack's constructor and destructor. 
Unfortunately, in Java objects destructor aren't called at the end of the object's scope, but the destruction is decided by the garbage collector.
I decided to create the push() and pop() methods in MatrixStack.java, and call them when needed.



Distribution
------------

This project is released under MIT license. See LICENSE.txt for further information.