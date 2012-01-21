OpenGL tutorials with LWJGL
=================
Hi all!

We are OpenGL beginners like a lot of you, and we know how frustrating is not find any good resources on modern OpenGL programming techniques, so
we are proud to start this project for porting McKesson's tutorials to Java with LWJGL.

[Learning Modern 3D Graphics Programming](http://www.arcsynthesis.org/gltut/index.html) is the book where to start. 
We decided to put online our work, although it is far from complete, to give to all LWJGL users a nice starting point for the understanding of OpenGL 3.3.
We are dedicating to this project most of our free time and it will update frequently.
Best regards,

Francesco & Marco



Project information
-------------------
The project is developed with Java 1.7 and LWJGL 2.8.2, and may not work with older versions.
* "rosick/framework" : package with common classes.
* "rosick/glm"       : package with math classes.
* "rosick/glutil"    : package with opengl-related classes.
* "rosick/mckesson"  : package with LWJGL porting of Jason McKesson's tutorials.
To try our work, we recommend using Eclipse ide. Open Eclipse, copy or link the "rosick" package into an existing project (in the "src" folder) and, if you haven't already done, 
add and configure the LWJGL library (visit [LWJGL home](http://www.lwjgl.org/) for further information) in the project build path. Open any tutorial and just click the play button.



Porting notes
-------------
We decided to port the tutorials in a manner as similar as possible to the original, but not all c++ features are implemented in Java (eg. function pointers). 
This is the changes list:

* Main consideration : in our project each tutorial class inherits from the base class GLWindow: in this class is executed the main loop (in c++ this is managed by Glut), 
which in order updates the values of "elapsedTime" and "lastFrameDuration", call update() and display(). When the window get resized the main loop call reshape().
The two variables "elapsedTime" and "lastFrameDuration" are declared as protected so every subclass can use their value; update(),display() and reshape() are 
overrided in every tutorial class.
* In addition to the original tutorials, to obtain a more accurate input method (eg. the camera movement controlled with the keyboard) we computed the simplest 
possible integration by multiplying the deltaMovement by "lastFrameDuration".
* "PushStack" : the author decided to implement a MatrixStack with a push/pop organization based on PushStack's constructor and destructor. 
Unfortunately, in Java objects destructor aren't called at the end of the object's scope, and the destruction is decided by the garbage collector.
We choose to create the push() and pop() methods in MatrixStack.java, and call them manually when needed.


Github users
------------

I kindly ask github users not to pull requests at this time.
We want to develop this project with our hands and if possibly to translate every tutorial of the book.
If we are not be able to continue working on this project, then we will be happy to let each user contributes with their code.
On the other hand, we ask you to communicate us issues or difficulties that you have found using this project.
Thanks in advance.



Distribution
------------

This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.